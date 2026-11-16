package com.ott.platform.data.repository

import com.ott.platform.data.iptv.IptvCatalogRemoteDataSource
import com.ott.platform.data.iptv.IptvOrgEndpoints
import com.ott.platform.database.CatalogLocalDataSource
import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.OttResult
import com.ott.platform.logging.OttLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class IptvCatalogRepositoryOfflineTest {

    private val noOpLogger = object : OttLogger {
        override fun d(tag: String, message: String) = Unit
        override fun i(tag: String, message: String) = Unit
        override fun w(tag: String, message: String, throwable: Throwable?) = Unit
        override fun e(tag: String, message: String, throwable: Throwable?) = Unit
    }

    @Test
    fun `persists fetched catalog and serves offline fallback when network fails`() = runTest {
        val local = InMemoryCatalogLocalDataSource()
        var currentTimeMs = 1_000L
        var onlineCalls = 0
        var isOffline = false

        val engine = MockEngine { request ->
            if (isOffline) {
                throw IllegalStateException("No internet connection")
            }
            onlineCalls++
            val url = request.url.toString()
            when {
                url == IptvOrgEndpoints.CATEGORIES_JSON -> respond(
                    content = """[{"id":"news","name":"News","description":"Global news"}]""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
                url.endsWith("/news.m3u") -> respond(
                    content = """
                        #EXTM3U
                        #EXTINF:-1 tvg-id="News24.us" tvg-logo="https://example.com/n.png" group-title="News",News 24
                        https://example.com/news24.m3u8
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                )
                else -> respond(content = "#EXTM3U\n", status = HttpStatusCode.OK)
            }
        }

        val remote = IptvCatalogRemoteDataSource(mockHttpClient(engine), noOpLogger)

        // 1. Initial online load populates local DB
        val onlineRepo = IptvCatalogRepository(
            remote = remote,
            logger = noOpLogger,
            local = local,
            cacheTtlMs = 60_000L,
            nowMs = { currentTimeMs },
        )
        val onlineFeed = onlineRepo.getHomeFeed(limitPerShelf = 10)
        assertIs<OttResult.Success<*>>(onlineFeed)
        assertEquals(1, onlineRepo.getHomeFeed(limitPerShelf = 10).let {
            (it as OttResult.Success).data.shelves.size
        })
        val callsAfterOnline = onlineCalls
        assertEquals(true, local.getChannelCount() > 0L)

        // 2. New repo instance while still within TTL reads local DB without network
        val freshRepo = IptvCatalogRepository(
            remote = remote,
            logger = noOpLogger,
            local = local,
            cacheTtlMs = 60_000L,
            nowMs = { currentTimeMs + 5_000L },
        )
        val cachedResult = freshRepo.getChannelsByCategory("news", limit = 10)
        assertIs<OttResult.Success<List<Channel>>>(cachedResult)
        assertEquals("News 24", cachedResult.data.single().title)
        assertEquals(callsAfterOnline, onlineCalls)

        // 3. After TTL expires AND network is offline, repo falls back to stale DB cache
        isOffline = true
        currentTimeMs += 120_000L
        val offlineRepo = IptvCatalogRepository(
            remote = remote,
            logger = noOpLogger,
            local = local,
            cacheTtlMs = 60_000L,
            nowMs = { currentTimeMs },
        )
        val offlineFeed = offlineRepo.getHomeFeed(limitPerShelf = 10)
        assertIs<OttResult.Success<*>>(offlineFeed)
        val newsShelf = (offlineFeed as OttResult.Success).data.shelves.single { it.id == "news" }
        assertEquals("News 24", newsShelf.items.single().title)

        val offlineSearch = offlineRepo.search("News", limit = 10)
        assertIs<OttResult.Success<List<Channel>>>(offlineSearch)
        assertEquals("News 24", offlineSearch.data.single().title)
    }

    private fun mockHttpClient(engine: MockEngine): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private class InMemoryCatalogLocalDataSource : CatalogLocalDataSource {
        private var categories: List<Category> = emptyList()
        private var categoriesUpdatedAt: Long? = null
        private val channelsByCategory = LinkedHashMap<String, List<Channel>>()
        private val categoryUpdatedAt = LinkedHashMap<String, Long>()
        private var time = 1_000L

        override suspend fun getCategories(): List<Category> = categories
        override suspend fun getCategoriesUpdatedAtMs(): Long? = categoriesUpdatedAt
        override suspend fun replaceCategories(categories: List<Category>) {
            this.categories = categories
            this.categoriesUpdatedAt = time
        }

        override suspend fun getChannelsByCategory(categoryId: String, limit: Int): List<Channel> =
            channelsByCategory[categoryId].orEmpty().take(limit)

        override suspend fun getCategoryUpdatedAtMs(categoryId: String): Long? =
            categoryUpdatedAt[categoryId]

        override suspend fun replaceChannelsForCategory(categoryId: String, channels: List<Channel>) {
            channelsByCategory[categoryId] = channels
            categoryUpdatedAt[categoryId] = time
        }

        override suspend fun searchChannels(query: String, limit: Int): List<Channel> =
            channelsByCategory.values.flatten()
                .filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.categoryName.contains(query, ignoreCase = true)
                }
                .take(limit)

        override suspend fun getChannel(id: ContentId): Channel? =
            channelsByCategory.values.flatten().firstOrNull { it.id == id }

        override suspend fun getChannelCount(): Long =
            channelsByCategory.values.sumOf { it.size }.toLong()
    }
}
