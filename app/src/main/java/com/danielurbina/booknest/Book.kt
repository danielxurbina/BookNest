package com.danielurbina.booknest

import android.os.Parcelable
import android.support.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class SearchBooksResponse(
    @SerialName("items") val items: List<Books>? = emptyList()
)

@Keep
@Parcelize
@Serializable
data class Books(
    @SerialName("kind") val kind: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("selfLink") val selfLink: String? = null,
    @SerialName("volumeInfo") val volumeInfo: VolumeInfo? = null,
    @SerialName("saleInfo") val saleInfo: SaleInfo? = null,
    @SerialName("searchInfo") val searchInfo: SearchInfo? = null
) : Parcelable

@Keep
@Parcelize
@Serializable
data class VolumeInfo(
    @SerialName("title") val title: String? = null,
    @SerialName("authors") val authors: List<String>? = emptyList(),
    @SerialName("publisher") val publisher: String? = null,
    @SerialName("publishedDate") val publishedDate: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("industryIdentifiers") val industryIdentifiers: List<IndustryIdentifiers>? = emptyList(),
    @SerialName("pageCount") val pageCount: Int? = null,
    @SerialName("categories") val categories: List<String>? = emptyList(),
    @SerialName("averageRating") val averageRating: Double? = null,
    @SerialName("imageLinks") val imageLinks: ImageLinks? = null,
    @SerialName("language") val language: String? = null,
    @SerialName("previewLink") val previewLink: String? = null,
    @SerialName("infoLink") val infoLink: String? = null,
    @SerialName("canonicalVolumeLink") val canonicalVolumeLink: String? = null
) : Parcelable {
    val thumbnailUrl: String
        get() = imageLinks?.thumbnail?.replace("http://", "https://") ?: "https://via.placeholder.com/150"
}

@Keep
@Parcelize
@Serializable
data class IndustryIdentifiers(
    @SerialName("type") val type: String? = null,
    @SerialName("identifier") val identifier: String? = null
) : Parcelable

@Keep
@Parcelize
@Serializable
data class ImageLinks(
    @SerialName("smallThumbnail") val smallThumbnail: String? = null,
    @SerialName("thumbnail") val thumbnail: String? = null
) : Parcelable

@Keep
@Parcelize
@Serializable
data class SaleInfo(
    @SerialName("country") val country: String? = null,
    @SerialName("saleability") val saleability: String? = null,
    @SerialName("isEbook") val isEbook: Boolean? = null,
    @SerialName("listPrice") val listPrice: ListPrice? = null,
    @SerialName("retailPrice") val retailPrice: RetailPrice? = null,
    @SerialName("buyLink") val buyLink: String? = null
) : Parcelable

@Keep
@Parcelize
@Serializable
data class ListPrice(
    @SerialName("amount") val amount: Double? = null,
    @SerialName("currencyCode") val currencyCode: String? = null
) : Parcelable

@Keep
@Parcelize
@Serializable
data class RetailPrice(
    @SerialName("amount") val amount: Double? = null,
    @SerialName("currencyCode") val currencyCode: String? = null
) : Parcelable

@Keep
@Parcelize
@Serializable
data class SearchInfo(
    @SerialName("textSnippet") val textSnippet: String? = null
) : Parcelable