package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.ContentShelf
import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetHomeFeedUseCaseTest {

    private val channel = Channel(
        id = ContentId("1"),
        title = "Demo",
        logoUrl = null,
        streamUrl = "https://example.com/a.m3u8",
        categoryId = "news",
        categoryName = "News",
    )

    @Test
    fun `rejects non positive shelf limit`() = runTest {
        val useCase = GetHomeFeedUseCase(object : CatalogRepository {
            override suspend fun getCategories(): OttResult<List<Category>> = error("unused")
            override suspend fun getHomeFeed(limitPerShelf: Int): OttResult<HomeFeed> = error("unused")
            override suspend fun getChannelsByCategory(categoryId: String, limit: Int): OttResult<List<Channel>> = error("unused")
            override suspend fun search(query: String, limit: Int): OttResult<List<Channel>> = error("unused")
            override suspend fun getChannel(id: ContentId): OttResult<Channel> = error("unused")
        })
        assertFailsWith<IllegalArgumentException> { useCase(0) }
    }

    @Test
    fun `returns repository home feed`() = runTest {
        val feed = HomeFeed(channel, listOf(ContentShelf("news", "News", listOf(channel))))
        val useCase = GetHomeFeedUseCase(object : CatalogRepository {
            override suspend fun getCategories(): OttResult<List<Category>> =
                OttResult.Success(emptyList())

            override suspend fun getHomeFeed(limitPerShelf: Int): OttResult<HomeFeed> =
                OttResult.Success(feed)

            override suspend fun getChannelsByCategory(categoryId: String, limit: Int): OttResult<List<Channel>> =
                OttResult.Success(emptyList())

            override suspend fun search(query: String, limit: Int): OttResult<List<Channel>> =
                OttResult.Success(emptyList())

            override suspend fun getChannel(id: ContentId): OttResult<Channel> =
                OttResult.Success(channel)
        })
        val result = useCase(12)
        assertTrue(result is OttResult.Success)
        assertEquals("Demo", (result as OttResult.Success).data.hero?.title)
        assertEquals(1, result.data.shelves.size)
    }
}
