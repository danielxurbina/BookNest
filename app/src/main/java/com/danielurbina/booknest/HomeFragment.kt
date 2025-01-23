package com.danielurbina.booknest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.danielurbina.booknest.databinding.FragmentHomeBinding
import com.google.firebase.firestore.ListenerRegistration

class HomeFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val toReadBooks = mutableListOf<BookFirestoreModel>()
    private val inProgressBooks = mutableListOf<BookFirestoreModel>()
    private val completedBooks = mutableListOf<BookFirestoreModel>()

    private lateinit var toReadAdapter: ReadingListAdapter
    private lateinit var inProgressAdapter: ReadingListAdapter
    private lateinit var completedAdapter: ReadingListAdapter

    private val firestore by lazy { Firebase.firestore }
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid }

    private var toReadListener: ListenerRegistration? = null
    private var inProgressListener: ListenerRegistration? = null
    private var completedListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedViewModel.refreshTrigger.observe(viewLifecycleOwner) { refresh ->
            if (refresh) attachSnapshotListeners()
        }

        setupRecyclerView(binding.toReadRecyclerView, toReadBooks, binding.noBooksToReadCard)
        setupRecyclerView(binding.inProgressRecyclerView, inProgressBooks, binding.noBooksInProgressCard)
        setupRecyclerView(binding.completedRecyclerView, completedBooks, binding.noBooksCompletedCard)

        attachSnapshotListeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        attachSnapshotListeners() // Reattach listeners when the fragment is visible
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        booksList: MutableList<BookFirestoreModel>,
        emptyCard: View
    ) {
        val adapter = ReadingListAdapter(booksList) { navigateToBookDetails(it) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        updateRecyclerViewVisibility(recyclerView, emptyCard, booksList)

        when (recyclerView.id) {
            R.id.to_read_recycler_view -> toReadAdapter = adapter
            R.id.in_progress_recycler_view -> inProgressAdapter = adapter
            R.id.completed_recycler_view -> completedAdapter = adapter
        }
    }

    private fun updateRecyclerViewVisibility(
        recyclerView: RecyclerView,
        emptyCard: View,
        list: List<*>
    ) {
        if (list.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyCard.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyCard.visibility = View.GONE
        }
    }

    private fun attachSnapshotListeners() {
        if (userId == null) {
            Log.e("HomeFragment", "User not logged in")
            return
        }

        firestore.collection("users").document(userId!!).collection("reading_list").apply {
            whereEqualTo("reading_list", "To Read").addSnapshotListener { snapshots, exception ->
                if (_binding == null) return@addSnapshotListener // Exit if the view is destroyed
                if (exception != null) {
                    Log.e("HomeFragment", "Error listening for To Read changes", exception)
                    return@addSnapshotListener
                }

                toReadBooks.clear()
                snapshots?.toObjects(BookFirestoreModel::class.java)?.let { toReadBooks.addAll(it) }
                updateRecyclerViewVisibility(
                    binding.toReadRecyclerView,
                    binding.noBooksToReadCard,
                    toReadBooks
                )
                toReadAdapter.notifyDataSetChanged()
            }

            whereEqualTo("reading_list", "In Progress").addSnapshotListener { snapshots, exception ->
                if (_binding == null) return@addSnapshotListener // Exit if the view is destroyed
                if (exception != null) {
                    Log.e("HomeFragment", "Error listening for In Progress changes", exception)
                    return@addSnapshotListener
                }

                inProgressBooks.clear()
                snapshots?.toObjects(BookFirestoreModel::class.java)?.let { inProgressBooks.addAll(it) }
                updateRecyclerViewVisibility(
                    binding.inProgressRecyclerView,
                    binding.noBooksInProgressCard,
                    inProgressBooks
                )
                inProgressAdapter.notifyDataSetChanged()
            }

            whereEqualTo("reading_list", "Completed").addSnapshotListener { snapshots, exception ->
                if (_binding == null) return@addSnapshotListener // Exit if the view is destroyed
                if (exception != null) {
                    Log.e("HomeFragment", "Error listening for Completed changes", exception)
                    return@addSnapshotListener
                }

                completedBooks.clear()
                snapshots?.toObjects(BookFirestoreModel::class.java)?.let { completedBooks.addAll(it) }
                updateRecyclerViewVisibility(
                    binding.completedRecyclerView,
                    binding.noBooksCompletedCard,
                    completedBooks
                )
                completedAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun detachSnapshotListeners() {
        toReadListener?.remove()
        inProgressListener?.remove()
        completedListener?.remove()

        toReadListener = null
        inProgressListener = null
        completedListener = null
    }

    private fun navigateToBookDetails(book: BookFirestoreModel) {
        val fragment = BookDetailsFragment()
        val bundle = Bundle().apply {
            putParcelable("book", Books(id = book.id, volumeInfo = book.toVolumeInfo(), saleInfo = book.toSaleInfo()))
        }
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        detachSnapshotListeners()
        _binding = null
    }
}