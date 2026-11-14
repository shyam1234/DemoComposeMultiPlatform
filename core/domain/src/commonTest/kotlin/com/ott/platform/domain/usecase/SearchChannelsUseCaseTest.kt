package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SearchChannelsUseCaseTest {

    private val sample = Channel(
        id = ContentId("news-1"),
        title = "World News Live",
        logoUrl = null,
        streamUrl = "https://example.com/live.m3u8",
        categoryId = "news",
        categoryName = "News",
    )

    private val repo = object : CatalogRepository {
        override suspend fun getCategories(): OttResult<List<Category>> =
            OttResult.Success(emptyList())

        override suspend fun getHomeFeed(limitPerShelf: Int): OttResult<HomeFeed> =
            OttResult.Success(HomeFeed(null, emptyList()))

        override suspend fun getChannelsByCategory(categoryId: String, limit: Int): OttResult<List<Channel>> =
            OttResult.Success(emptyList())

        override suspend fun search(query: String, limit: Int): OttResult<List<Channel>> {
            val hits = listOf(sample).filter { it.title.contains(query, ignoreCase = true) }
            return OttResult.Success(hits.take(limit))
        }

        override suspend fun getChannel(id: ContentId): OttResult<Channel> =
            OttResult.Success(sample)
    }

    private val useCase = SearchChannelsUseCase(repo)

    @Test
    fun `short query returns empty without hitting meaningful search`() = runTest {
        val result = useCase("a")
        assertTrue(result is OttResult.Success)
        assertEquals(emptyList(), (result as OttResult.Success).data)
    }

    @Test
    fun `query finds matching channel title`() = runTest {
        val result = useCase("world")
        assertTrue(result is OttResult.Success)
        assertEquals(1, (result as OttResult.Success).data.size)
        assertEquals("World News Live", result.data.first().title)
    }
}
