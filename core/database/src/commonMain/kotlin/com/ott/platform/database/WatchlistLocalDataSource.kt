package com.ott.platform.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface WatchlistLocalDataSource {
    fun observeAll(): Flow<List<Channel>>
    suspend fun add(channel: Channel)
    suspend fun remove(id: ContentId)
    suspend fun exists(id: ContentId): Boolean
    suspend fun toggle(channel: Channel): Boolean
}

@OptIn(ExperimentalTime::class)
class SqlDelightWatchlistLocalDataSource(
    private val database: OttDatabase,
    private val ioDispatcher: CoroutineDispatcher = databaseIoDispatcher,
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : WatchlistLocalDataSource {

    private val queries: OttDatabaseQueries
        get() = database.ottDatabaseQueries

    override fun observeAll(): Flow<List<Channel>> =
        queries.selectAllWatchlist()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun add(channel: Channel) {
        withContext(ioDispatcher) {
            database.transaction {
                val alreadyExists = queries.existsInWatchlist(channel.id.value).executeAsOne()
                if (!alreadyExists) {
                    insertChannel(channel)
                }
            }
        }
    }

    override suspend fun remove(id: ContentId) {
        withContext(ioDispatcher) {
            queries.deleteFromWatchlist(id.value)
        }
    }

    override suspend fun exists(id: ContentId): Boolean = withContext(ioDispatcher) {
        queries.existsInWatchlist(id.value).executeAsOne()
    }

    override suspend fun toggle(channel: Channel): Boolean = withContext(ioDispatcher) {
        database.transactionWithResult {
            val currentlyInWatchlist = queries.existsInWatchlist(channel.id.value).executeAsOne()
            if (currentlyInWatchlist) {
                queries.deleteFromWatchlist(channel.id.value)
                false
            } else {
                insertChannel(channel)
                true
            }
        }
    }

    private fun insertChannel(channel: Channel) {
        queries.upsertWatchlist(
            channel_id = channel.id.value,
            title = channel.title,
            logo_url = channel.logoUrl,
            stream_url = channel.streamUrl,
            category_id = channel.categoryId,
            category_name = channel.categoryName,
            group_title = channel.groupTitle,
            is_geo_blocked = if (channel.isGeoBlocked) 1L else 0L,
            is_not_247 = if (channel.isNot247) 1L else 0L,
            quality = channel.quality,
            tvg_id = channel.tvgId,
            user_agent = channel.userAgent,
            referrer = channel.referrer,
            metadata_json = channel.encodeMetadataJson(),
            added_at_ms = nowMs(),
        )
    }

    private fun Watchlist.toDomain(): Channel {
        val meta = decodeChannelMetadata(metadata_json)
        return Channel(
            id = ContentId(channel_id),
            title = title,
            logoUrl = logo_url,
            streamUrl = stream_url,
            categoryId = category_id,
            categoryName = category_name,
            groupTitle = group_title,
            isGeoBlocked = is_geo_blocked != 0L,
            isNot247 = is_not_247 != 0L,
            quality = quality,
            tvgId = tvg_id,
            userAgent = user_agent,
            referrer = referrer,
            images = meta.images.map { it.toDomain() },
            backdropUrl = meta.backdropUrl,
            synopsis = meta.synopsis,
            genres = meta.genres,
            releaseYear = meta.releaseYear,
            runtimeMinutes = meta.runtimeMinutes,
            parentalRating = meta.parentalRating,
            imdbRating = meta.imdbRating,
            assetType = meta.assetType,
            cast = meta.cast,
            director = meta.director,
            tags = meta.tags,
            trailerUrl = meta.trailerUrl,
            language = meta.language,
        )
    }
}
