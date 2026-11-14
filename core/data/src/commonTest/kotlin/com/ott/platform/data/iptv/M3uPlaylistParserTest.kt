package com.ott.platform.data.iptv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class M3uPlaylistParserTest {

    private val sample = """
        #EXTM3U
        #EXTINF:-1 tvg-id="DemoNews.us@SD" tvg-logo="https://example.com/logo.png" group-title="News",Demo News (720p)
        https://example.com/news/index.m3u8
        #EXTINF:-1 tvg-id="Blocked.us@SD" tvg-logo="https://example.com/b.png" group-title="News",Blocked Channel (1080p) [Geo-blocked]
        https://example.com/blocked/index.m3u8
        #EXTINF:-1 tvg-id="UANews.us@SD" http-user-agent="OttDemo/1.0" group-title="News",UA News
        #EXTVLCOPT:http-user-agent=OttDemo/1.0
        https://example.com/ua/index.m3u8
    """.trimIndent()

    @Test
    fun `parses channels with logo title and stream url`() {
        val channels = M3uPlaylistParser.parse(sample, "news", "News", limit = 10)
        assertEquals(3, channels.size)
        val first = channels.first()
        assertEquals("Demo News", first.title)
        assertEquals("https://example.com/logo.png", first.logoUrl)
        assertEquals("https://example.com/news/index.m3u8", first.streamUrl)
        assertEquals("720p", first.quality)
        assertFalse(first.isGeoBlocked)
    }

    @Test
    fun `detects geo blocked flag and respects limit`() {
        val channels = M3uPlaylistParser.parse(sample, "news", "News", limit = 2)
        assertEquals(2, channels.size)
        assertTrue(channels.any { it.isGeoBlocked && it.title == "Blocked Channel" })
    }

    @Test
    fun `captures playlist user agent`() {
        val channels = M3uPlaylistParser.parse(sample, "news", "News", limit = 10)
        val uaChannel = channels.first { it.title == "UA News" }
        assertEquals("OttDemo/1.0", uaChannel.userAgent)
    }
}
