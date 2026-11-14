package com.ott.platform.data.iptv

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId

data class ParsedM3uEntry(
    val tvgId: String?,
    val logoUrl: String?,
    val groupTitle: String?,
    val title: String,
    val streamUrl: String,
    val userAgent: String? = null,
)

/**
 * Minimal ExtM3U parser for iptv-org category playlists.
 */
object M3uPlaylistParser {

    private val attrRegex = Regex("""([\w-]+)="([^"]*)"""")

    fun parse(
        playlist: String,
        categoryId: String,
        categoryName: String,
        limit: Int = Int.MAX_VALUE,
    ): List<Channel> {
        val lines = playlist.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val channels = mutableListOf<Channel>()
        var i = 0
        while (i < lines.size && channels.size < limit) {
            val line = lines[i]
            if (!line.startsWith("#EXTINF", ignoreCase = true)) {
                i++
                continue
            }
            val title = line.substringAfterLast(',').trim().ifEmpty { "Unknown" }
            val attrs = attrRegex.findAll(line).associate { it.groupValues[1] to it.groupValues[2] }
            var userAgent: String? = attrs["http-user-agent"]
            i++
            while (i < lines.size && lines[i].startsWith("#") && !lines[i].startsWith("#EXTINF", ignoreCase = true)) {
                val opt = lines[i]
                if (opt.startsWith("#EXTVLCOPT:http-user-agent=", ignoreCase = true)) {
                    userAgent = opt.substringAfter('=')
                }
                i++
            }
            if (i >= lines.size) break
            val url = lines[i]
            i++
            if (!url.startsWith("http", ignoreCase = true)) continue

            val group = attrs["group-title"].orEmpty()
            val isGeoBlocked = title.contains("[Geo-blocked]", ignoreCase = true) ||
                group.contains("Geo-blocked", ignoreCase = true)
            val isNot247 = title.contains("[Not 24/7]", ignoreCase = true)
            val quality = Regex("""\((\d+p)\)""").find(title)?.groupValues?.getOrNull(1)
            val cleanTitle = title
                .replace(Regex("""\s*\[.*?]\s*"""), " ")
                .replace(Regex("""\s*\(\d+p\)\s*"""), " ")
                .trim()
            val tvgId = attrs["tvg-id"]?.takeIf { it.isNotBlank() }
            val idValue = buildId(categoryId, tvgId, cleanTitle, url)

            channels += Channel(
                id = ContentId(idValue),
                title = cleanTitle,
                logoUrl = attrs["tvg-logo"]?.takeIf { it.isNotBlank() },
                streamUrl = url,
                categoryId = categoryId,
                categoryName = categoryName,
                groupTitle = group.ifBlank { categoryName },
                isGeoBlocked = isGeoBlocked,
                isNot247 = isNot247,
                quality = quality,
                tvgId = tvgId,
            )
        }
        return channels
    }

    private fun buildId(categoryId: String, tvgId: String?, title: String, url: String): String {
        val base = tvgId?.replace('@', '-')?.replace('.', '-')
            ?: (title.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'))
        val urlHash = url.hashCode().toUInt().toString(16)
        return "$categoryId:$base:$urlHash"
    }
}
