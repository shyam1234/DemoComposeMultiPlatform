package com.ott.platform.data.iptv

/**
 * Free public IPTV catalog from [iptv-org](https://github.com/iptv-org/iptv).
 * No API key. Streams are third-party and may be geo-blocked or offline.
 */
object IptvOrgEndpoints {
    const val API_BASE = "https://iptv-org.github.io/api"
    const val PLAYLIST_BASE = "https://iptv-org.github.io/iptv"

    const val CATEGORIES_JSON = "$API_BASE/categories.json"

    fun categoryPlaylist(categoryId: String): String =
        "$PLAYLIST_BASE/categories/$categoryId.m3u"

    /** Home shelves used by the Android OTT demo. */
    val HOME_CATEGORY_IDS: List<String> = listOf(
        "news",
        "movies",
        "series",
        "sports",
        "music",
        "documentary",
        "entertainment",
        "kids",
    )
}
