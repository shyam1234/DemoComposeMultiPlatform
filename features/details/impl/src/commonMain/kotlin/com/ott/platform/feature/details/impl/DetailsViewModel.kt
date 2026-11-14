package com.ott.platform.feature.details.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.WatchlistRepository
import com.ott.platform.domain.usecase.GetChannelDetailsUseCase
import com.ott.platform.domain.usecase.ToggleWatchlistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsUiState(
    val isLoading: Boolean = true,
    val channel: Channel? = null,
    val inWatchlist: Boolean = false,
    val error: String? = null,
)

class DetailsViewModel(
    private val contentId: String,
    private val getChannelDetails: GetChannelDetailsUseCase,
    private val toggleWatchlist: ToggleWatchlistUseCase,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailsUiState())
    val state: StateFlow<DetailsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getChannelDetails(ContentId(contentId))) {
                is OttResult.Success -> {
                    val inList = watchlistRepository.isInWatchlist(result.data.id)
                    _state.update {
                        it.copy(isLoading = false, channel = result.data, inWatchlist = inList)
                    }
                }
                is OttResult.Error -> _state.update {
                    it.copy(isLoading = false, error = result.message)
                }
                OttResult.Loading -> Unit
            }
        }
    }

    fun onToggleWatchlist() {
        val channel = _state.value.channel ?: return
        viewModelScope.launch {
            val added = toggleWatchlist(channel)
            _state.update { it.copy(inWatchlist = added) }
        }
    }
}
