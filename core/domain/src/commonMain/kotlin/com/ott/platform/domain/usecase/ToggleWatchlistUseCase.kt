package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.repository.WatchlistRepository

class ToggleWatchlistUseCase(
    private val watchlistRepository: WatchlistRepository,
) {
    suspend operator fun invoke(channel: Channel): Boolean =
        watchlistRepository.toggle(channel)
}
