package com.danielurbina.booknest

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.danielurbina.booknest.databinding.FragmentBookDetailsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class BookDetailsFragment : Fragment() {
    private var _binding: FragmentBookDetailsBinding? = null
    private val binding get() = _binding!!

    private val languageMap = mapOf(
        "en" to "English",
        "es" to "Spanish",
        "fr" to "French",
        "de" to "German",
        "it" to "Italian",
        "pt" to "Portuguese",
        "nl" to "Dutch",
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailsBinding.inflate(inflater, container, false)

        arguments?.let { args ->
            val book = getBookFromArguments(args)

            book?.let { populateBookData(it) }
                ?: Log.e(TAG, "Error: Book object is null or invalid")
        } ?: Log.e(TAG, "Error: Arguments are null")

        return binding.root
    }

    private fun setupReadingListSpinner(bookId: String, book: Books) {
        val readingListCategories = resources.getStringArray(R.array.reading_list_categories)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, readingListCategories)
        binding.readingListCategorySpinner.adapter = spinnerAdapter

        var hasInitialized = false

        val defaultOption = "Select Reading List"
        val modifiedReadingListCategories = listOf(defaultOption) + readingListCategories
        val modifiedSpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, modifiedReadingListCategories)
        binding.readingListCategorySpinner.adapter = modifiedSpinnerAdapter
        binding.readingListCategorySpinner.setSelection(0)

        binding.readingListCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (hasInitialized) {
                    val selectedReadingListCategory = modifiedReadingListCategories[position]
                    if (selectedReadingListCategory != defaultOption) {
                        showConfirmationDialog(bookId, book, selectedReadingListCategory)
                    }
                } else {
                    hasInitialized = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun showConfirmationDialog(bookId: String, book: Books, readingListCategory: String) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add to $readingListCategory")
            .setMessage("Are you sure you want to add this book to the $readingListCategory list?")
            .setPositiveButton("Yes") { _, _ ->
                saveToFirestore(bookId, book, readingListCategory)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun saveToFirestore(bookId: String, book: Books, readingList: String) {
        Log.d(TAG, "saveToFirestore called for Book ID: $bookId, Reading List: $readingList")
        val volumeInfo = book.volumeInfo ?: return
        val saleInfo = book.saleInfo
        val firestore = Firebase.firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val readingListRef = firestore.collection("users").document(userId).collection("reading_list").document(bookId)

        readingListRef.get().addOnSuccessListener { document ->
            val existingTotalPages = document?.getLong("totalPages")?.toInt()
                ?: book.volumeInfo?.pageCount ?: 1

            val bookData = hashMapOf(
                "id" to bookId,
                "title" to volumeInfo.title,
                "authors" to volumeInfo.authors,
                "publisher" to volumeInfo.publisher,
                "publishedDate" to volumeInfo.publishedDate,
                "description" to volumeInfo.description,
                "thumbnailUrl" to volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
                "previewLink" to volumeInfo.previewLink,
                "buyLink" to saleInfo?.buyLink,
                "infoLink" to volumeInfo.infoLink,
                "canonicalVolumeLink" to volumeInfo.canonicalVolumeLink,
                "category" to (volumeInfo.categories?.firstOrNull() ?: "Unknown Category"),
                "language" to volumeInfo.language,
                "isbn10" to volumeInfo.industryIdentifiers?.firstOrNull { it.type == "ISBN_10" }?.identifier,
                "isbn13" to volumeInfo.industryIdentifiers?.firstOrNull { it.type == "ISBN_13" }?.identifier,
                "pageCount" to volumeInfo.pageCount,
                "averageRating" to volumeInfo.averageRating,
                "reading_list" to readingList, // Assign reading list
                "timestamp" to FieldValue.serverTimestamp(),
                "pagesRead" to 0,
                "totalPages" to existingTotalPages
            )

            Log.d(TAG, "Saving Book Data: $bookData")

            readingListRef.set(bookData)
                .addOnSuccessListener {
                    Log.d(TAG, "Book added successfully with ID: $bookId")
                    Toast.makeText(requireContext(), "Book added to $readingList list", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding book to reading list: ${e.message}")
                    Toast.makeText(requireContext(), "Failed to add book to $readingList list", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getBookFromArguments(args: Bundle): Books? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            args.getParcelable("book", Books::class.java)
        } else {
            @Suppress("DEPRECATION")
            args.getParcelable("book") as? Books
        }
    }

    private fun populateBookData(book: Books) {
        val volumeInfo = book.volumeInfo
        val bookId = book.id

        if (volumeInfo == null || bookId == null) {
            Log.e(TAG, "Error: VolumeInfo or Book ID is null")
            return
        }

        binding.apply {
            populateBookDetails(volumeInfo, book.saleInfo)
        }

        setupReadingListSpinner(bookId, book)
    }


    private fun populateBookDetails(volumeInfo: VolumeInfo, saleInfo: SaleInfo?) {
        binding.apply {
            bookTitle.text = volumeInfo.title ?: getString(R.string.no_title)
            bookAuthors.text = volumeInfo.authors?.joinToString(", ") ?: getString(R.string.unknown_author)
            bookPublisher.text = volumeInfo.publisher ?: getString(R.string.unknown_publisher)
            bookYear.text = extractYear(volumeInfo.publishedDate) ?: getString(R.string.unknown_date)
            bookPages.text = volumeInfo.pageCount?.toString() ?: getString(R.string.unknown_pages)
            bookRating.text = volumeInfo.averageRating?.toString() ?: getString(R.string.no_rating)
            bookCategory.text = volumeInfo.categories?.joinToString(", ") ?: getString(R.string.unknown_category)
            bookLanguages.text = getLanguageName(volumeInfo.language)
            bookIsbn10.text = volumeInfo.industryIdentifiers?.firstOrNull { it.type == "ISBN_10" }?.identifier
                ?: getString(R.string.unknown_isbn)
            bookIsbn13.text = volumeInfo.industryIdentifiers?.firstOrNull { it.type == "ISBN_13" }?.identifier
                ?: getString(R.string.unknown_isbn)

            bookDescription.text = volumeInfo.description ?: getString(R.string.no_description)

            loadImage(volumeInfo.imageLinks?.thumbnail)
            setupButton(previewButton, volumeInfo.previewLink)
            setupButton(buyButton, saleInfo?.buyLink)
        }
    }

    private fun getLanguageName(code: String?): String {
        return languageMap[code] ?: getString(R.string.unknown_language)
    }

    private fun extractYear(date: String?): String? {
        return date?.split("-")?.firstOrNull()
    }

    private fun loadImage(url: String?) {
        Glide.with(this)
            .load(url?.replace("http://", "https://"))
            .placeholder(R.drawable.ic_book_placeholder)
            .fitCenter()
            .override(600, 900)
            .into(binding.bookImage)
    }

    private fun setupButton(button: View, link: String?) {
        button.apply {
            visibility = if (link.isNullOrEmpty()) View.GONE else View.VISIBLE
            setOnClickListener { openUrl(link) }
        }
    }

    private fun openUrl(url: String?) {
        url?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "BookDetailsFragment"
    }
}