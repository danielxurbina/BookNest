package com.danielurbina.booknest

import com.google.firebase.Timestamp

data class BookFirestoreModel(
    val id: String = "",
    val title: String? = null,
    val authors: List<String>? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val previewLink: String? = null,
    val buyLink: String? = null,
    val infoLink: String? = null,
    val canonicalVolumeLink: String? = null,
    val category: String? = null,
    val language: String? = null,
    val isbn10: String? = null,
    val isbn13: String? = null,
    val pageCount: Int? = null,
    val averageRating: Double? = null,
    val readingListCategory: String? = null,
    val timestamp: Timestamp? = null,
    val pagesRead: Int? = null,
    val totalPages: Int? = null,
) {
    /**
     * Converts this Firestore book model to a VolumeInfo instance
     * used by the application.
     */
    fun toVolumeInfo(): VolumeInfo {
        return VolumeInfo(
            title = title ?: "No Title",
            authors = authors ?: emptyList(),
            publisher = publisher ?: "Unknown Publisher",
            publishedDate = publishedDate ?: "Unknown Date",
            description = description ?: "No Description Available",
            imageLinks = ImageLinks(thumbnail = thumbnailUrl),
            previewLink = previewLink,
            language = language,
            industryIdentifiers = listOfNotNull(
                IndustryIdentifiers(type = "ISBN_10", identifier = isbn10),
                IndustryIdentifiers(type = "ISBN_13", identifier = isbn13)
            ),
            pageCount = pageCount ?: 0,
            averageRating = averageRating ?: 0.0,
            categories = listOfNotNull(category),
            infoLink = infoLink,
            canonicalVolumeLink = canonicalVolumeLink,
        )
    }

    fun toSaleInfo(): SaleInfo {
        return SaleInfo(
            buyLink = buyLink
        )
    }
}