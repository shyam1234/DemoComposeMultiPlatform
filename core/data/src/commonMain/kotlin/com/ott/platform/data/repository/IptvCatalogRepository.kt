package com.ott.platform.data.repository

import com.ott.platform.data.iptv.IptvCatalogRemoteDataSource
import com.ott.platform.data.iptv.IptvOrgEndpoints
import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.ContentShelf
import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.model.ShelfCardStyle
import com.ott.platform.domain.repository.CatalogRepository
import com.ott.platform.logging.OttLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class IptvCatalogRepository(
    private val remote: IptvCatalogRemoteDataSource,
    private val logger: OttLogger,
) : CatalogRepository {

    private val mutex = Mutex()
    private var cachedCategories: List<Category>? = null
    private val channelIndex = LinkedHashMap<String, Channel>()
    private val shelfCache = LinkedHashMap<String, List<Channel>>()

    override suspend fun getCategories(): OttResult<List<Category>> = runCatchingResult {
        mutex.withLock {
            cachedCategories ?: remote.fetchCategories().also { cachedCategories = it }
        }
    }

    override suspend fun getHomeFeed(limitPerShelf: Int): OttResult<HomeFeed> = runCatchingResult {
        val categories = when (val result = getCategories()) {
            is OttResult.Success -> result.data.associateBy { it.id }
            is OttResult.Error -> throw result.cause ?: IllegalStateException(result.message)
            OttResult.Loading -> emptyMap()
        }

        val shelves = coroutineScope {
            IptvOrgEndpoints.HOME_CATEGORY_IDS.map { categoryId ->
                async {
                    val name = categories[categoryId]?.name ?: categoryId.replaceFirstChar { it.uppercase() }
                    val items = loadCategory(categoryId, name, limitPerShelf)
                        .filterNot { it.isGeoBlocked }
                    ContentShelf(
                        id = categoryId,
                        title = name,
                        items = items,
                        cardStyle = cardStyleFor(categoryId),
                    )
                }
            }.awaitAll().filter { it.items.isNotEmpty() }
        }

        val heroes = shelves
            .asSequence()
            .flatMap { shelf -> shelf.items.asSequence().map { shelf to it } }
            .filter { (_, channel) ->
                !channel.logoUrl.isNullOrBlank() &&
                    channel.streamUrl.contains(".m3u8", ignoreCase = true)
            }
            .distinctBy { (_, channel) -> channel.id.value }
            .take(HERO_COUNT)
            .map { it.second }
            .toList()
            .ifEmpty { listOfNotNull(shelves.firstOrNull()?.items?.firstOrNull()) }

        HomeFeed(heroes = heroes, shelves = shelves)
    }

    private fun cardStyleFor(categoryId: String): ShelfCardStyle =
        when (categoryId) {
            "movies", "series", "documentary", "kids" -> ShelfCardStyle.Portrait
            else -> ShelfCardStyle.Landscape
        }

    override suspend fun getChannelsByCategory(categoryId: String, limit: Int): OttResult<List<Channel>> =
        runCatchingResult {
            val name = categoryName(categoryId)
            loadCategory(categoryId, name, limit)
        }

    override suspend fun search(query: String, limit: Int): OttResult<List<Channel>> = runCatchingResult {
        ensureSearchIndex(limitPerCategory = 30)
        val q = query.lowercase()
        channelIndex.values
            .asSequence()
            .filter { channel ->
                channel.title.lowercase().contains(q) ||
                    channel.categoryName.lowercase().contains(q) ||
                    channel.groupTitle.lowercase().contains(q)
            }
            .take(limit)
            .toList()
    }

    override suspend fun getChannel(id: ContentId): OttResult<Channel> = runCatchingResult {
        channelIndex[id.value]
            ?: run {
                ensureSearchIndex(limitPerCategory = 40)
                channelIndex[id.value]
            }
            ?: error("Channel not found: ${id.value}")
    }

    private suspend fun ensureSearchIndex(limitPerCategory: Int) {
        if (channelIndex.size >= 80) return
        IptvOrgEndpoints.HOME_CATEGORY_IDS.forEach { categoryId ->
            val name = categoryName(categoryId)
            loadCategory(categoryId, name, limitPerCategory)
        }
    }

    private suspend fun loadCategory(categoryId: String, categoryName: String, limit: Int): List<Channel> {
        mutex.withLock {
            shelfCache[categoryId]?.let { cached ->
                return cached.take(limit)
            }
        }
        val fetched = remote.fetchCategoryPlaylist(categoryId, categoryName, limit = limit.coerceAtLeast(40))
        mutex.withLock {
            shelfCache[categoryId] = fetched
            fetched.forEach { channelIndex[it.id.value] = it }
        }
        return fetched.take(limit)
    }

    private suspend fun categoryName(categoryId: String): String {
        val categories = when (val result = getCategories()) {
            is OttResult.Success -> result.data
            else -> emptyList()
        }
        return categories.firstOrNull { it.id == categoryId }?.name
            ?: categoryId.replaceFirstChar { it.uppercase() }
    }

    private suspend fun <T> runCatchingResult(block: suspend () -> T): OttResult<T> =
        try {
            OttResult.Success(block())
        } catch (t: Throwable) {
            logger.e(TAG, "Catalog error: ${t.message}", t)
            OttResult.Error(t.message ?: "Unknown catalog error", t)
        }

    private companion object {
        const val TAG = "IptvCatalogRepository"
        const val HERO_COUNT = 6
    }
}
