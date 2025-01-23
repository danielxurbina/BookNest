package com.danielurbina.booknest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ReadingListAdapter(
    private val books: MutableList<BookFirestoreModel>,
    private val onBookClick: (BookFirestoreModel) -> Unit
) : RecyclerView.Adapter<ReadingListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reading_list_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookImage: ImageView = itemView.findViewById(R.id.book_image)
        private val bookTitle: TextView = itemView.findViewById(R.id.book_title)
        private val bookAuthors: TextView = itemView.findViewById(R.id.book_authors)

        fun bind(book: BookFirestoreModel) {
            bookTitle.text = book.title ?: "Unknown Title"
            bookAuthors.text = book.authors?.joinToString(", ") ?: "Unknown Author"
            Glide.with(itemView.context)
                .load(book.thumbnailUrl)
                .centerCrop()
                .into(bookImage)

            itemView.setOnClickListener { onBookClick(book) }
        }
    }
}