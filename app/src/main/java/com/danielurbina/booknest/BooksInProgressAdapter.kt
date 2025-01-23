package com.danielurbina.booknest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BooksInProgressAdapter(
    private val books: MutableList<BookFirestoreModel>, // Changed to MutableList
    private val onUpdate: (BookFirestoreModel, Int) -> Unit
) : RecyclerView.Adapter<BooksInProgressAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookTitle: TextView = view.findViewById(R.id.book_title)
        val bookProgressBar: ProgressBar = view.findViewById(R.id.book_progress_bar)
        val progressText: TextView = view.findViewById(R.id.progress_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_in_progress, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.bookTitle.text = "${book.title ?: "Unknown Title"} (${book.authors?.joinToString(", ") ?: "Unknown Author"})"
        holder.bookProgressBar.max = book.totalPages ?: 1
        holder.bookProgressBar.progress = book.pagesRead ?: 0
        holder.progressText.text = "${book.pagesRead ?: 0}/${book.totalPages ?: 1} pages read"

        holder.itemView.setOnClickListener {
            onUpdate(book, holder.bookProgressBar.progress)
        }
    }

    override fun getItemCount(): Int = books.size
}