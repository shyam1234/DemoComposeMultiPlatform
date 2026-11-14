package com.ott.platform.data.repository

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryWatchlistRepository : WatchlistRepository {
    private val mutex = Mutex()
    private val state = MutableStateFlow<List<Channel>>(emptyList())

    override val watchlist: Flow<List<Channel>> = state.asStateFlow()

    override suspend fun add(channel: Channel) {
        mutex.withLock {
            if (state.value.none { it.id == channel.id }) {
                state.update { it + channel }
            }
        }
    }

    override suspend fun remove(id: ContentId) {
        mutex.withLock {
            state.update { list -> list.filterNot { it.id == id } }
        }
    }

    override suspend fun isInWatchlist(id: ContentId): Boolean =
        state.value.any { it.id == id }

    override suspend fun toggle(channel: Channel): Boolean = mutex.withLock {
        val exists = state.value.any { it.id == channel.id }
        if (exists) {
            state.update { list -> list.filterNot { it.id == channel.id } }
            false
        } else {
            state.update { it + channel }
            true
        }
    }
}
