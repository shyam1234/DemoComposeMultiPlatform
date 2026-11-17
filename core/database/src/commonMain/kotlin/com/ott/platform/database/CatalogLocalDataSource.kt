package com.ott.platform.database

import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface CatalogLocalDataSource {
    suspend fun getCategories(): List<Category>
    suspend fun getCategoriesUpdatedAtMs(): Long?
    suspend fun replaceCategories(categories: List<Category>)

    suspend fun getChannelsByCategory(categoryId: String, limit: Int): List<Channel>
    suspend fun getCategoryUpdatedAtMs(categoryId: String): Long?
    suspend fun replaceChannelsForCategory(categoryId: String, channels: List<Channel>)

    suspend fun searchChannels(query: String, limit: Int): List<Channel>
    suspend fun getChannel(id: ContentId): Channel?
    suspend fun getChannelCount(): Long
}

@OptIn(ExperimentalTime::class)
class SqlDelightCatalogLocalDataSource(
    private val database: OttDatabase,
    private val ioDispatcher: CoroutineDispatcher = databaseIoDispatcher,
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : CatalogLocalDataSource {

    private val queries: OttDatabaseQueries
        get() = database.ottDatabaseQueries

    override suspend fun getCategories(): List<Category> = withContext(ioDispatcher) {
        queries.selectAllCategories().executeAsList().map { row ->
            Category(
                id = row.id,
                name = row.name,
                description = row.description,
            )
        }
    }

    override suspend fun getCategoriesUpdatedAtMs(): Long? = withContext(ioDispatcher) {
        queries.selectCategoriesUpdatedAt().executeAsOneOrNull()?.oldest_updated_at
    }

    override suspend fun replaceCategories(categories: List<Category>) = withContext(ioDispatcher) {
        val timestamp = nowMs()
        database.transaction {
            queries.deleteAllCategories()
            categories.forEachIndexed { index, category ->
                queries.upsertCategory(
                    id = category.id,
                    name = category.name,
                    description = category.description,
                    sort_order = index.toLong(),
                    updated_at_ms = timestamp,
                )
            }
        }
    }

    override suspend fun getChannelsByCategory(categoryId: String, limit: Int): List<Channel> =
        withContext(ioDispatcher) {
            queries.selectChannelsByCategory(
                category_id = categoryId,
                value_ = limit.toLong(),
            ).executeAsList().map { it.toDomain() }
        }

    override suspend fun getCategoryUpdatedAtMs(categoryId: String): Long? =
        withContext(ioDispatcher) {
            queries.selectCategoryUpdatedAt(categoryId).executeAsOneOrNull()?.oldest_updated_at
        }

    override suspend fun replaceChannelsForCategory(categoryId: String, channels: List<Channel>) =
        withContext(ioDispatcher) {
            val timestamp = nowMs()
            database.transaction {
                queries.deleteChannelsByCategory(categoryId)
                channels.forEachIndexed { index, channel ->
                    queries.upsertChannel(
                        id = channel.id.value,
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
                        shelf_order = index.toLong(),
                        updated_at_ms = timestamp,
                    )
                }
            }
        }

    override suspend fun searchChannels(query: String, limit: Int): List<Channel> =
        withContext(ioDispatcher) {
            queries.searchChannels(
                query = query,
                limit = limit.toLong(),
            ).executeAsList().map { it.toDomain() }
        }

    override suspend fun getChannel(id: ContentId): Channel? = withContext(ioDispatcher) {
        queries.selectChannelById(id = id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getChannelCount(): Long = withContext(ioDispatcher) {
        queries.countChannels().executeAsOne()
    }

    private fun Channels.toDomain(): Channel = Channel(
        id = ContentId(id),
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
    )
}
