package com.ott.platform.data.repository

import com.ott.platform.database.WatchlistLocalDataSource
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow

class PersistentWatchlistRepository(
    private val local: WatchlistLocalDataSource,
) : WatchlistRepository {

    override val watchlist: Flow<List<Channel>> = local.observeAll()

    override suspend fun add(channel: Channel) {
        local.add(channel)
    }

    override suspend fun remove(id: ContentId) {
        local.remove(id)
    }

    override suspend fun isInWatchlist(id: ContentId): Boolean =
        local.exists(id)

    override suspend fun toggle(channel: Channel): Boolean =
        local.toggle(channel)
}
