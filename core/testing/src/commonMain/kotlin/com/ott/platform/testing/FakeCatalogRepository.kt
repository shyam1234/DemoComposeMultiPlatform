package com.ott.platform.testing

import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.ContentShelf
import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.model.ShelfCardStyle
import com.ott.platform.domain.repository.CatalogRepository

class FakeCatalogRepository(
    private val channels: List<Channel> = sampleChannels(),
) : CatalogRepository {

    override suspend fun getCategories(): OttResult<List<Category>> =
        OttResult.Success(
            channels.map { Category(it.categoryId, it.categoryName) }.distinctBy { it.id },
        )

    override suspend fun getHomeFeed(limitPerShelf: Int): OttResult<HomeFeed> {
        val shelves = channels
            .groupBy { it.categoryId }
            .map { (id, items) ->
                val style = if (id == "movies" || id == "series") {
                    ShelfCardStyle.Portrait
                } else {
                    ShelfCardStyle.Landscape
                }
                ContentShelf(id, items.first().categoryName, items.take(limitPerShelf), style)
            }
        return OttResult.Success(
            HomeFeed(
                heroes = channels.filter { !it.logoUrl.isNullOrBlank() }.ifEmpty { channels.take(1) },
                shelves = shelves,
            ),
        )
    }

    override suspend fun getChannelsByCategory(categoryId: String, limit: Int): OttResult<List<Channel>> =
        OttResult.Success(channels.filter { it.categoryId == categoryId }.take(limit))

    override suspend fun search(query: String, limit: Int): OttResult<List<Channel>> =
        OttResult.Success(
            channels.filter { it.title.contains(query, ignoreCase = true) }.take(limit),
        )

    override suspend fun getChannel(id: ContentId): OttResult<Channel> =
        channels.firstOrNull { it.id == id }?.let { OttResult.Success(it) }
            ?: OttResult.Error("not found")

    companion object {
        fun sampleChannels(): List<Channel> = listOf(
            Channel(
                id = ContentId("news:demo"),
                title = "Ott News 24",
                logoUrl = "https://example.com/news.png",
                streamUrl = "https://example.com/news.m3u8",
                categoryId = "news",
                categoryName = "News",
            ),
            Channel(
                id = ContentId("movies:demo"),
                title = "Classic Movies",
                logoUrl = "https://example.com/movies.png",
                streamUrl = "https://example.com/movies.m3u8",
                categoryId = "movies",
                categoryName = "Movies",
            ),
            Channel(
                id = ContentId("sports:demo"),
                title = "Sports Arena",
                logoUrl = null,
                streamUrl = "https://example.com/sports.m3u8",
                categoryId = "sports",
                categoryName = "Sports",
            ),
        )
    }
}
