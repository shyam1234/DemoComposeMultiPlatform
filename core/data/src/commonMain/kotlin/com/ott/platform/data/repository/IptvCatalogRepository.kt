package com.ott.platform.data.repository

import com.ott.platform.data.iptv.IptvCatalogRemoteDataSource
import com.ott.platform.data.iptv.IptvOrgEndpoints
import com.ott.platform.database.CatalogLocalDataSource
import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.ContentShelf
import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.model.ShelfCardStyle
import com.ott.platform.domain.repository.CatalogRepository
import com.ott.platform.logging.OttLogger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(ExperimentalTime::class)
class IptvCatalogRepository(
    private val remote: IptvCatalogRemoteDataSource,
    private val logger: OttLogger,
    private val local: CatalogLocalDataSource? = null,
    private val cacheTtlMs: Long = DEFAULT_CACHE_TTL_MS,
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : CatalogRepository {

    private val mutex = Mutex()
    private var cachedCategories: List<Category>? = null
    private val channelIndex = LinkedHashMap<String, Channel>()
    private val shelfCache = LinkedHashMap<String, List<Channel>>()

    override suspend fun getCategories(): OttResult<List<Category>> = runCatchingResult {
        mutex.withLock {
            cachedCategories?.let { return@withLock it }

            val dbCategories = local?.getCategories().orEmpty()
            val dbUpdatedAt = if (dbCategories.isNotEmpty()) local?.getCategoriesUpdatedAtMs() else null
            if (dbCategories.isNotEmpty() && isFresh(dbUpdatedAt)) {
                cachedCategories = dbCategories
                return@withLock dbCategories
            }

            try {
                val fetched = remote.fetchCategories()
                runCatching { local?.replaceCategories(fetched) }
                    .onFailure { logger.w(TAG, "Failed to persist categories: ${it.message}", it) }
                cachedCategories = fetched
                fetched
            } catch (t: Throwable) {
                if (dbCategories.isNotEmpty()) {
                    logger.w(TAG, "Offline fallback: serving ${dbCategories.size} cached categories")
                    cachedCategories = dbCategories
                    dbCategories
                } else {
                    throw t
                }
            }
        }
    }

    override suspend fun getHomeFeed(limitPerShelf: Int): OttResult<HomeFeed> = runCatchingResult {
        val categories = when (val result = getCategories()) {
            is OttResult.Success -> result.data.associateBy { it.id }
            is OttResult.Error -> {
                val hasOfflineChannels = (local?.getChannelCount() ?: 0L) > 0L
                if (hasOfflineChannels) {
                    logger.w(TAG, "Categories unavailable offline; building feed from cached channels")
                    emptyMap()
                } else {
                    throw result.cause ?: IllegalStateException(result.message)
                }
            }
            OttResult.Loading -> emptyMap()
        }

        var firstFailure: Throwable? = null
        val shelves = coroutineScope {
            IptvOrgEndpoints.HOME_CATEGORY_IDS.map { categoryId ->
                async {
                    val name = categories[categoryId]?.name ?: categoryId.replaceFirstChar { it.uppercase() }
                    runCatching {
                        loadCategory(categoryId, name, limitPerShelf)
                            .filterNot { it.isGeoBlocked }
                    }.onFailure { error ->
                        if (firstFailure == null) firstFailure = error
                        logger.w(TAG, "Skipping shelf $categoryId: ${error.message}")
                    }.getOrNull()?.let { items ->
                        ContentShelf(
                            id = categoryId,
                            title = name,
                            items = items,
                            cardStyle = cardStyleFor(categoryId),
                        )
                    }
                }
            }.awaitAll().filterNotNull().filter { it.items.isNotEmpty() }
        }

        if (shelves.isEmpty()) {
            firstFailure?.let { throw it }
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
        val trimmed = query.trim()
        val fromDb = if (local != null && trimmed.isNotEmpty()) {
            local.searchChannels(trimmed, limit)
        } else {
            emptyList()
        }
        if (fromDb.isNotEmpty()) {
            mutex.withLock {
                fromDb.forEach { channelIndex[it.id.value] = it }
            }
            return@runCatchingResult fromDb
        }

        val q = trimmed.lowercase()
        mutex.withLock {
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
    }

    override suspend fun getChannel(id: ContentId): OttResult<Channel> = runCatchingResult {
        mutex.withLock { channelIndex[id.value] }
            ?: local?.getChannel(id)?.also { cached ->
                mutex.withLock { channelIndex[cached.id.value] = cached }
            }
            ?: run {
                ensureSearchIndex(limitPerCategory = 40)
                mutex.withLock { channelIndex[id.value] } ?: local?.getChannel(id)
            }
            ?: error("Channel not found: ${id.value}")
    }

    private suspend fun ensureSearchIndex(limitPerCategory: Int) {
        val memoryCount = mutex.withLock { channelIndex.size }
        val dbCount = local?.getChannelCount() ?: 0L
        if (memoryCount >= 80 || dbCount >= 80L) return

        IptvOrgEndpoints.HOME_CATEGORY_IDS.forEach { categoryId ->
            val name = categoryName(categoryId)
            runCatching {
                loadCategory(categoryId, name, limitPerCategory)
            }.onFailure { error ->
                logger.w(TAG, "Search index skip for $categoryId: ${error.message}")
            }
        }
    }

    private suspend fun loadCategory(categoryId: String, categoryName: String, limit: Int): List<Channel> {
        mutex.withLock {
            shelfCache[categoryId]?.let { cached ->
                return cached.take(limit)
            }
        }

        val fetchLimit = limit.coerceAtLeast(DEFAULT_CATEGORY_FETCH_LIMIT)
        val dbChannels = local?.getChannelsByCategory(categoryId, fetchLimit).orEmpty()
        val dbUpdatedAt = if (dbChannels.isNotEmpty()) local?.getCategoryUpdatedAtMs(categoryId) else null
        if (dbChannels.isNotEmpty() && isFresh(dbUpdatedAt)) {
            cacheChannelsInMemory(categoryId, dbChannels)
            return dbChannels.take(limit)
        }

        return try {
            val fetched = remote.fetchCategoryPlaylist(categoryId, categoryName, limit = fetchLimit)
            runCatching { local?.replaceChannelsForCategory(categoryId, fetched) }
                .onFailure { logger.w(TAG, "Failed to persist channels for $categoryId: ${it.message}", it) }
            cacheChannelsInMemory(categoryId, fetched)
            fetched.take(limit)
        } catch (t: Throwable) {
            if (dbChannels.isNotEmpty()) {
                logger.w(TAG, "Offline fallback: serving ${dbChannels.size} cached channels for $categoryId")
                cacheChannelsInMemory(categoryId, dbChannels)
                dbChannels.take(limit)
            } else {
                throw t
            }
        }
    }

    private suspend fun cacheChannelsInMemory(categoryId: String, channels: List<Channel>) {
        mutex.withLock {
            shelfCache[categoryId] = channels
            channels.forEach { channelIndex[it.id.value] = it }
        }
    }

    private fun isFresh(updatedAtMs: Long?): Boolean {
        if (updatedAtMs == null) return false
        return (nowMs() - updatedAtMs) in 0 until cacheTtlMs
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
        const val DEFAULT_CATEGORY_FETCH_LIMIT = 40
        const val DEFAULT_CACHE_TTL_MS = 6 * 60 * 60 * 1000L // 6 hours
    }
}
