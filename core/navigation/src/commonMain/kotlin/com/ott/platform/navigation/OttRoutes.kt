package com.ott.platform.navigation

object OttRoutes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val WATCHLIST = "watchlist"
    const val DETAILS = "details/{contentId}"
    const val PLAYER = "player/{contentId}"

    fun details(contentId: String): String = "details/${encode(contentId)}"
    fun player(contentId: String): String = "player/${encode(contentId)}"

    fun encode(contentId: String): String =
        contentId.replace("%", "%25").replace("/", "%2F").replace(":", "%3A")

    fun decode(contentId: String): String =
        contentId.replace("%3A", ":").replace("%2F", "/").replace("%25", "%")
}
