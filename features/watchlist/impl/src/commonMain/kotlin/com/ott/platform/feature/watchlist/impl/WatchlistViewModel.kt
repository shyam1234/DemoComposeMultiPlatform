package com.ott.platform.feature.watchlist.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WatchlistViewModel(
    watchlistRepository: WatchlistRepository,
) : ViewModel() {
    val items: StateFlow<List<Channel>> = watchlistRepository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
