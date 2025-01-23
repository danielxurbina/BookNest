package com.danielurbina.booknest

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

private const val GOOGLE_BOOKS_API_KEY = BuildConfig.GOOGLE_BOOKS_API_KEY
private const val GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q="

class SearchFragment : Fragment() {
    private val books = mutableListOf<Books>()
    private lateinit var searchBar: EditText
    private lateinit var clearButton: ImageView
    private lateinit var noResultsText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false).apply {
        setupUI(this)
    }

    private fun setupUI(view: View) {
        searchBar = view.findViewById(R.id.search_bar)
        clearButton = view.findViewById(R.id.clear_button)
        noResultsText = view.findViewById(R.id.no_results)
        recyclerView = view.findViewById(R.id.search_results)

        setupRecyclerView()
        setupSearchInput()
        setupClearButton()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        bookAdapter = BookAdapter(requireContext(), books, ::navigateToBookDetails)
        recyclerView.adapter = bookAdapter
    }

    private fun setupSearchInput() {
        searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchBar.text.toString().trim()
                Log.d("SearchFragment", "Search triggered with query: $query")
                if (query.isEmpty()) {
                    toggleResultsVisibility(false)
                } else {
                    toggleResultsVisibility(true)
                    searchBooks(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupClearButton() {
        clearButton.setOnClickListener {
            searchBar.text.clear()
            toggleResultsVisibility(showResults = false, clearedBoolean = true)
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun toggleResultsVisibility(showResults: Boolean, clearedBoolean: Boolean = false) {
        if (clearedBoolean) {
            books.clear()
            bookAdapter.notifyDataSetChanged()
            noResultsText.visibility = View.GONE
            recyclerView.visibility = View.GONE
            return
        } else {
            noResultsText.visibility = if (showResults) View.GONE else View.VISIBLE
            recyclerView.visibility = if (showResults) View.VISIBLE else View.GONE
        }
    }

    private fun searchBooks(query: String) {
        val client = OkHttpClient()
        val url = "$GOOGLE_BOOKS_API_URL$query&key=$GOOGLE_BOOKS_API_KEY"
        val request = Request.Builder().url(url).build()
        Log.d("SearchFragment", "Request URL: $url")
        Log.d("SearchFragment", "Reequest: $request")
        Log.d("SearchFragment", "Request Headers: ${request.headers}")

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("SearchFragment", "Sending request...")
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("SearchFragment", "Raw Response Data: $responseData") // Log raw JSON response

                    val searchResponse = createJson().decodeFromString<SearchBooksResponse>(responseData!!)
                    Log.d("SearchFragment", "Parsed Response: $searchResponse") // Log parsed response

                    updateBookList(searchResponse.items ?: emptyList())
                } else {
                    Log.e("SearchFragment", "HTTP error ${response.code}: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("SearchFragment", "Exception: ${e.message}")
            }
        }
    }

    private fun updateBookList(newBooks: List<Books>) {
        Log.d("SearchFragment", "Updating books list with: $newBooks") // Log the new books
        books.clear()
        books.addAll(newBooks)
        CoroutineScope(Dispatchers.Main).launch {
            bookAdapter.notifyDataSetChanged()
        }
    }

    private fun navigateToBookDetails(book: Books) {
        val fragment = BookDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable("book", book)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun createJson() = Json {
        isLenient = true
        ignoreUnknownKeys = true
        useAlternativeNames = false
    }
}