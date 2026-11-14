package com.ott.platform.domain.model

data class ContentId(val value: String)

data class Category(
    val id: String,
    val name: String,
    val description: String = "",
)

data class Channel(
    val id: ContentId,
    val title: String,
    val logoUrl: String?,
    val streamUrl: String,
    val categoryId: String,
    val categoryName: String,
    val groupTitle: String = categoryName,
    val isGeoBlocked: Boolean = false,
    val isNot247: Boolean = false,
    val quality: String? = null,
    val tvgId: String? = null,
)

data class ContentShelf(
    val id: String,
    val title: String,
    val items: List<Channel>,
)

data class HomeFeed(
    val hero: Channel?,
    val shelves: List<ContentShelf>,
)

sealed class OttResult<out T> {
    data class Success<T>(val data: T) : OttResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : OttResult<Nothing>()
    data object Loading : OttResult<Nothing>()
}
