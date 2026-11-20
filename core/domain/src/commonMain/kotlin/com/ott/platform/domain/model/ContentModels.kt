package com.ott.platform.domain.model

data class ContentId(val value: String)

data class MediaImage(
    val type: String,
    val url: String,
    val width: Int = 0,
    val height: Int = 0,
    val langCode: String = "",
)

data class AppMenuItem(
    val id: String,
    val title: String,
    val layoutId: String?,
    val deeplinkUrl: String = "",
    val deeplinkType: String = "Internal",
    val defaultIconUrl: String? = null,
    val focusedIconUrl: String? = null,
    val selectedIconUrl: String? = null,
    val children: List<AppMenuItem> = emptyList(),
)

data class Category(
    val id: String,
    val name: String,
    val description: String = "",
    val displayType: String = "Landscape",
    val contentType: String = "generic",
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
    val userAgent: String? = null,
    val referrer: String? = null,
    val images: List<MediaImage> = emptyList(),
    val backdropUrl: String? = null,
    val synopsis: String = "",
    val genres: List<String> = emptyList(),
    val releaseYear: String? = null,
    val runtimeMinutes: Int? = null,
    val parentalRating: String? = null,
    val imdbRating: Double? = null,
    val assetType: String? = null,
    val cast: List<String> = emptyList(),
    val director: String? = null,
    val tags: List<String> = emptyList(),
    val trailerUrl: String? = null,
    val language: String? = null,
) {
    val isPlayable: Boolean
        get() = streamUrl.isNotBlank()

    fun imageFor(slot: ImageSlot): String? =
        images.resolveUrl(slot = slot, fallbackUrl = backdropUrl ?: logoUrl)

    fun imageForRail(
        rawDisplayType: String?,
        contentType: RailContentType = RailContentType.Generic,
    ): String? {
        val slot = ImageSlot.forRail(displayType = rawDisplayType, contentType = contentType)
        return images.resolveUrl(slot = slot, fallbackUrl = logoUrl ?: backdropUrl)
    }
}

enum class ShelfCardStyle {
    /** 16:9 landscape tiles. */
    Landscape,
    /** 2:3 vertical poster tiles. */
    Portrait,
    /** 1:1 square artwork tiles (for future Square rails). */
    Square,
    /** Wide 21:9 banner/editorial tiles. */
    Banner,
    ;

    companion object {
        fun fromCms(
            displayType: String?,
            contentType: String? = null,
        ): ShelfCardStyle {
            val normalizedDisplay = displayType.orEmpty().trim().lowercase()
            val normalizedContent = contentType.orEmpty().trim().lowercase()
            return when {
                normalizedDisplay in setOf("portrait", "vertical", "poster") -> Portrait
                normalizedDisplay in setOf("square", "1:1") -> Square
                normalizedDisplay in setOf("banner", "hero", "billboard", "editorial_banner") -> Banner
                normalizedDisplay == "landscape" && normalizedContent == "editorial" -> Banner
                else -> Landscape
            }
        }
    }
}

enum class RailContentType {
    Editorial,
    Top10,
    New,
    Mosaic,
    Grid,
    Generic,
    ;

    companion object {
        fun fromCms(raw: String?): RailContentType = when (raw.orEmpty().trim().lowercase()) {
            "editorial", "hero", "featured" -> Editorial
            "top_10", "top10", "trending_top10" -> Top10
            "new", "new_releases" -> New
            "mosaic" -> Mosaic
            "grid" -> Grid
            else -> Generic
        }
    }
}

data class ContentShelf(
    val id: String,
    val title: String,
    val items: List<Channel>,
    val cardStyle: ShelfCardStyle = ShelfCardStyle.Landscape,
    val contentType: RailContentType = RailContentType.Generic,
    val rawDisplayType: String = cardStyle.name,
    val rawContentType: String = contentType.name.lowercase(),
)

data class HomeFeed(
    val heroes: List<Channel> = emptyList(),
    val shelves: List<ContentShelf> = emptyList(),
    val layoutId: String = "",
    val layoutName: String = "Home",
    val themeUrl: String? = null,
) {
    /** First featured item — kept for older call sites/tests. */
    val hero: Channel? get() = heroes.firstOrNull()

    constructor(hero: Channel?, shelves: List<ContentShelf>) : this(
        heroes = listOfNotNull(hero),
        shelves = shelves,
    )
}

sealed class OttResult<out T> {
    data class Success<T>(val data: T) : OttResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : OttResult<Nothing>()
    data object Loading : OttResult<Nothing>()
}
