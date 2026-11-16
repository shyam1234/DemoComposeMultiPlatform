package com.ott.platform.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SqlDelightDataSourcesTest {

    private lateinit var driver: JdbcSqliteDriver
    private lateinit var database: OttDatabase
    private lateinit var catalogLocal: SqlDelightCatalogLocalDataSource
    private lateinit var watchlistLocal: SqlDelightWatchlistLocalDataSource
    private var fakeClockMs = 1_000L

    @BeforeTest
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        OttDatabase.Schema.create(driver)
        database = OttDatabase(driver)
        catalogLocal = SqlDelightCatalogLocalDataSource(
            database = database,
            ioDispatcher = Dispatchers.Unconfined,
            nowMs = { fakeClockMs },
        )
        watchlistLocal = SqlDelightWatchlistLocalDataSource(
            database = database,
            ioDispatcher = Dispatchers.Unconfined,
            nowMs = { fakeClockMs },
        )
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun `watchlist persists add toggle remove and emits flow`() = runTest {
        val news = sampleChannel(id = "news:1", title = "News 24", categoryId = "news")
        val movies = sampleChannel(id = "movies:1", title = "Cinema One", categoryId = "movies")

        assertTrue(watchlistLocal.observeAll().first().isEmpty())

        watchlistLocal.add(news)
        watchlistLocal.add(news) // duplicate add should be ignored
        fakeClockMs += 100L
        assertTrue(watchlistLocal.toggle(movies))

        val saved = watchlistLocal.observeAll().first()
        assertEquals(listOf(news.id, movies.id), saved.map { it.id })
        assertTrue(watchlistLocal.exists(news.id))
        assertTrue(watchlistLocal.exists(movies.id))

        assertFalse(watchlistLocal.toggle(news))
        assertFalse(watchlistLocal.exists(news.id))

        watchlistLocal.remove(movies.id)
        assertTrue(watchlistLocal.observeAll().first().isEmpty())
    }

    @Test
    fun `catalog stores categories channels search and preserves order`() = runTest {
        val categories = listOf(
            Category(id = "news", name = "News", description = "Live news"),
            Category(id = "movies", name = "Movies", description = "Films"),
        )
        catalogLocal.replaceCategories(categories)

        assertEquals(categories, catalogLocal.getCategories())
        assertEquals(1_000L, catalogLocal.getCategoriesUpdatedAtMs())

        val newsChannels = listOf(
            sampleChannel(id = "news:1", title = "World News", categoryId = "news", logoUrl = "https://img/1.png"),
            sampleChannel(id = "news:2", title = "City Desk", categoryId = "news", logoUrl = null),
        )
        val movieChannels = listOf(
            sampleChannel(id = "movies:1", title = "Sci-Fi Classics", categoryId = "movies"),
        )

        catalogLocal.replaceChannelsForCategory("news", newsChannels)
        catalogLocal.replaceChannelsForCategory("movies", movieChannels)

        assertEquals(newsChannels, catalogLocal.getChannelsByCategory("news", limit = 10))
        assertEquals(movieChannels, catalogLocal.getChannelsByCategory("movies", limit = 10))
        assertEquals(3L, catalogLocal.getChannelCount())

        val byId = catalogLocal.getChannel(ContentId("news:2"))
        assertNotNull(byId)
        assertEquals("City Desk", byId.title)

        val searchResults = catalogLocal.searchChannels("news", limit = 10)
        assertEquals(2, searchResults.size)
        assertEquals(ContentId("news:1"), searchResults.first().id)

        // Replacing one category leaves other categories intact
        val updatedNews = listOf(sampleChannel(id = "news:3", title = "Morning Briefing", categoryId = "news"))
        catalogLocal.replaceChannelsForCategory("news", updatedNews)
        assertEquals(updatedNews, catalogLocal.getChannelsByCategory("news", limit = 10))
        assertNull(catalogLocal.getChannel(ContentId("news:1")))
        assertNotNull(catalogLocal.getChannel(ContentId("movies:1")))
    }

    private fun sampleChannel(
        id: String,
        title: String,
        categoryId: String,
        logoUrl: String? = "https://example.com/logo.png",
    ) = Channel(
        id = ContentId(id),
        title = title,
        logoUrl = logoUrl,
        streamUrl = "https://example.com/$id.m3u8",
        categoryId = categoryId,
        categoryName = categoryId.replaceFirstChar { it.uppercase() },
    )
}
