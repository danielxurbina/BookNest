package com.danielurbina.booknest

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProgressFragment : Fragment() {

    private val firestore by lazy { Firebase.firestore }
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid }
    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var inProgressRecyclerView: RecyclerView
    private lateinit var goalsRecyclerView: RecyclerView
    private lateinit var addGoalButton: Button
    private lateinit var noBooksInProgressCard: View
    private lateinit var noReadingGoalsCard: View

    private val booksInProgress = mutableListOf<BookFirestoreModel>()
    private val readingGoals = mutableListOf<ReadingGoal>()

    private lateinit var booksInProgressAdapter: BooksInProgressAdapter
    private lateinit var readingGoalsAdapter: ReadingGoalsAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        inProgressRecyclerView = view.findViewById(R.id.in_progress_recycler_view)
        goalsRecyclerView = view.findViewById(R.id.goals_recycler_view)
        addGoalButton = view.findViewById(R.id.add_goal_button)
        noBooksInProgressCard = view.findViewById(R.id.no_books_in_progress_card)
        noReadingGoalsCard = view.findViewById(R.id.no_reading_goals_card)

        booksInProgressAdapter = BooksInProgressAdapter(booksInProgress) { book, currentProgress ->
            showUpdateProgressDialog { updatedPagesRead ->
                if (updatedPagesRead != currentProgress) {
                    saveBookProgressToFirestore(
                        book.id,
                        updatedPagesRead,
                        book.title ?: "Unknown Title",
                        book.authors?.joinToString(", ") ?: "Unknown Author"
                    )
                }
            }
        }

        inProgressRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = booksInProgressAdapter
        }

        readingGoalsAdapter = ReadingGoalsAdapter(readingGoals,
            onDelete = { goal -> deleteGoal(goal.id) },
            onStatusChange = { goal, isCompleted -> updateGoalStatus(goal.id, isCompleted) }
        )

        goalsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = readingGoalsAdapter
        }

        sharedViewModel.refreshTrigger.observe(viewLifecycleOwner) {
            if (it) fetchBooksInProgress()
        }

        fetchBooksCompletedThisMonth()
        fetchPagesReadThisWeek()
        fetchCurrentStreak()
        fetchBooksInProgress()
        fetchReadingGoals()

        addGoalButton.setOnClickListener { showAddGoalDialog() }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchBooksCompletedThisMonth() {
        Log.d("FirestoreDebug", "Current user ID: $userId")
        if (userId == null) return

        val currentMonth = java.time.YearMonth.now()
        val startOfMonth = currentMonth.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        val endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant()

        firestore.collection("users")
            .document(userId!!)
            .collection("reading_list")
            .whereEqualTo("reading_list", "Completed")
            .whereGreaterThanOrEqualTo("timestamp", com.google.firebase.Timestamp(startOfMonth.epochSecond, 0))
            .whereLessThanOrEqualTo("timestamp", com.google.firebase.Timestamp(endOfMonth.epochSecond, 0))
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                val completedBooksCount = snapshots?.size() ?: 0
                view?.findViewById<TextView>(R.id.books_completed)?.text = "Books Completed This Month: $completedBooksCount"
            }
    }

    private fun fetchPagesReadThisWeek() {
        if (userId == null) return

        firestore.collection("users").document(userId!!).collection("progress").document("summary")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.e("FirestoreDebug", "Failed to fetch progress data for weekly pages read.", e)
                    return@addSnapshotListener
                }

                val weeklyPagesRead = snapshot.getLong("pages_read_this_week")?.toInt() ?: 0
                Log.d("FirestoreDebug", "Pages Read This Week: $weeklyPagesRead")
                view?.findViewById<TextView>(R.id.pages_read)?.text = "Pages Read This Week: $weeklyPagesRead"
            }
    }

    private fun fetchCurrentStreak() {
        if (userId == null) return

        firestore.collection("users").document(userId!!).collection("progress").document("summary")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.e("FirestoreDebug", "Failed to fetch progress data for current streak.", e)
                    return@addSnapshotListener
                }

                val streak = snapshot.getLong("current_streak")?.toInt() ?: 0
                Log.d("FirestoreDebug", "Current Streak: $streak days")
                view?.findViewById<TextView>(R.id.current_streak)?.text = "Current Streak: $streak days"
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchBooksInProgress() {
        Log.d("FirestoreDebug", "Current user ID: $userId")
        if (userId == null) return

        firestore.collection("users")
            .document(userId!!)
            .collection("reading_list")
            .whereEqualTo("reading_list", "In Progress")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                booksInProgress.clear()
                snapshots?.toObjects(BookFirestoreModel::class.java)?.let { booksInProgress.addAll(it) }
                updateRecyclerViewVisibility(inProgressRecyclerView, noBooksInProgressCard, booksInProgress)
                booksInProgressAdapter.notifyDataSetChanged()
            }
    }

    private fun fetchReadingGoals() {
        if (userId == null) return

        firestore.collection("users")
            .document(userId!!)
            .collection("reading_goals")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FirestoreDebug", "Failed to fetch reading goals.", e)
                    return@addSnapshotListener
                }

                readingGoals.clear()
                snapshots?.toObjects(ReadingGoal::class.java)?.let { readingGoals.addAll(it) }
                updateRecyclerViewVisibility(goalsRecyclerView, noReadingGoalsCard, readingGoals)
                readingGoalsAdapter.notifyDataSetChanged() // Efficiently update the adapter
            }
    }

    private fun updateRecyclerViewVisibility(recyclerView: RecyclerView, emptyCard: View, list: List<*>) {
        if (list.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyCard.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyCard.visibility = View.GONE
        }
    }


    private fun deleteGoal(goalId: String) {
        if (userId == null) return

        firestore.collection("users")
            .document(userId!!)
            .collection("reading_goals")
            .document(goalId)
            .delete()
            .addOnSuccessListener {
                Log.d("FirestoreDebug", "Goal deleted successfully.")
                readingGoals.removeAll { it.id == goalId }
                readingGoalsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "Failed to delete goal.", e)
            }
    }

    private fun updateGoalStatus(goalId: String, isCompleted: Boolean) {
        if (userId == null) return

        firestore.collection("users")
            .document(userId!!)
            .collection("reading_goals")
            .document(goalId)
            .update("completed", isCompleted)
            .addOnSuccessListener {
                Log.d("FirestoreDebug", "Goal status updated to $isCompleted successfully.")
                readingGoals.find { it.id == goalId }?.isCompleted = isCompleted
                readingGoalsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "Failed to update goal status to $isCompleted.", e)
            }
    }



    private fun showAddGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_goal, null)
        val goalInput = dialogView.findViewById<EditText>(R.id.goal_input)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Reading Goal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val goalText = goalInput.text.toString().trim()
                if (goalText.isNotEmpty()) addReadingGoal(goalText)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        dialog.show()
    }

    private fun addReadingGoal(description: String) {
        if (userId == null) return

        val newGoal = ReadingGoal(
            id = firestore.collection("users").document(userId!!)
                .collection("reading_goals").document().id,
            description = description,
            isCompleted = false
        )

        firestore.collection("users")
            .document(userId!!)
            .collection("reading_goals")
            .document(newGoal.id)
            .set(newGoal)
            .addOnSuccessListener { Log.d("FirestoreDebug", "Goal added successfully.") }
            .addOnFailureListener { e -> Log.e("FirestoreDebug", "Failed to add goal.", e) }
    }

    private fun showUpdateProgressDialog(onProgressUpdated: (Int) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_progress, null)
        val pagesInput = dialogView.findViewById<EditText>(R.id.pages_input)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Update Pages Read")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedPagesRead = pagesInput.text.toString().toIntOrNull() ?: return@setPositiveButton
                onProgressUpdated(updatedPagesRead)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveBookProgressToFirestore(bookId: String, pagesRead: Int, bookName: String, author: String) {
        if (userId == null) return

        val today = java.time.LocalDate.now()
        val bookDocRef = firestore.collection("users").document(userId!!).collection("reading_list").document(bookId)
        val progressDocRef = firestore.collection("users").document(userId!!).collection("progress").document("summary")

        bookDocRef.get().addOnSuccessListener { bookSnapshot ->
            if (bookSnapshot.exists()) {
                val previousPagesRead = bookSnapshot.getLong("pagesRead") ?: 0
                val additionalPagesRead = pagesRead - previousPagesRead.toInt()

                bookDocRef.update("pagesRead", pagesRead, "lastUpdated", com.google.firebase.Timestamp.now())

                progressDocRef.get().addOnSuccessListener { progressSnapshot ->
                    val progressData = progressSnapshot.data ?: mapOf(
                        "pages_read_this_week" to 0,
                        "current_streak" to 0,
                        "books" to mutableMapOf<String, Any>()
                    )

                    val currentPagesReadThisWeek = (progressData["pages_read_this_week"] as? Long ?: 0L).toInt()
                    val updatedPagesReadThisWeek = currentPagesReadThisWeek + additionalPagesRead

                    val currentStreak = (progressData["current_streak"] as? Long ?: 0L).toInt()
                    val lastUpdateDate = progressSnapshot.getString("lastUpdateDate")?.let { java.time.LocalDate.parse(it) }
                    val updatedStreak = if (lastUpdateDate == today) currentStreak else currentStreak + 1

                    val books = (progressData["books"] as? Map<*, *>)
                        ?.mapValues { (_, value) -> value as? Map<String, Any> ?: emptyMap() }
                        ?.filterKeys { it is String }
                        ?.mapKeys { it.key as String }
                        ?.toMutableMap() ?: mutableMapOf()

                    books[bookId] = mapOf(
                        "pages_read" to pagesRead,
                        "book_name" to bookName,
                        "author" to author
                    )

                    progressDocRef.set(
                        mapOf(
                            "pages_read_this_week" to updatedPagesReadThisWeek,
                            "current_streak" to updatedStreak,
                            "lastUpdateDate" to today.toString(),
                            "books" to books
                        )
                    ).addOnSuccessListener {
                        Log.d("FirestoreDebug", "Progress updated successfully.")
                        fetchCurrentStreak()
                        fetchPagesReadThisWeek()
                    }.addOnFailureListener { e -> Log.e("FirestoreDebug", "Failed to update progress document.", e) }
                }.addOnFailureListener { e -> Log.e("FirestoreDebug", "Failed to fetch progress document.", e) }
            } else {
                Log.w("FirestoreDebug", "Cannot update. Book document does not exist: $bookId")
            }
        }.addOnFailureListener { e -> Log.e("FirestoreDebug", "Failed to fetch book document.", e) }
    }
}

data class ReadingGoal(
    val id: String = "",
    val description: String = "",
    var isCompleted: Boolean = false
)