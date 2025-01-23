package com.danielurbina.booknest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookAdapter(
    private val context: Context,
    private val books: List<Books>,
    private val onBookClick: (Books) -> Unit
) : RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val bookImageView: ImageView = itemView.findViewById(R.id.book_image)
        private val titleTextView: TextView = itemView.findViewById(R.id.book_title)
        private val authorTextView: TextView = itemView.findViewById(R.id.book_author)

        fun bind(book: Books) {
            book.volumeInfo?.let {
                titleTextView.text = it.title ?: context.getString(R.string.no_title)
                authorTextView.text = it.authors?.joinToString(", ") ?: context.getString(R.string.unknown_author)
                Glide.with(context)
                    .load(it.imageLinks?.thumbnail?.replace("http://", "https://"))
                    .placeholder(R.drawable.ic_book_placeholder)
                    .into(bookImageView)
            }
            itemView.setOnClickListener { onBookClick(book) }
        }
    }
}