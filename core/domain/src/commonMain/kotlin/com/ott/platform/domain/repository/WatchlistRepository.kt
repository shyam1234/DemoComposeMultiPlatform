package com.ott.platform.domain.repository

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    val watchlist: Flow<List<Channel>>
    suspend fun add(channel: Channel)
    suspend fun remove(id: ContentId)
    suspend fun isInWatchlist(id: ContentId): Boolean
    suspend fun toggle(channel: Channel): Boolean
}
