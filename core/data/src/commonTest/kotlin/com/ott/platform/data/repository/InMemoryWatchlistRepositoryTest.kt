package com.ott.platform.data.repository

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class InMemoryWatchlistRepositoryTest {

    private val channel = Channel(
        id = ContentId("news:1"),
        title = "News One",
        logoUrl = null,
        streamUrl = "https://example.com/a.m3u8",
        categoryId = "news",
        categoryName = "News",
    )

    @Test
    fun `toggle adds then removes channel`() = runTest {
        val repo = InMemoryWatchlistRepository()
        assertTrue(repo.toggle(channel))
        assertTrue(repo.isInWatchlist(channel.id))
        assertFalse(repo.toggle(channel))
        assertFalse(repo.isInWatchlist(channel.id))
    }
}
