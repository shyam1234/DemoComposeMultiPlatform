package com.ott.platform.feature.search.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.usecase.SearchChannelsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Channel> = emptyList(),
    val error: String? = null,
)

class SearchViewModel(
    private val searchChannels: SearchChannelsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()
    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        if (query.trim().length < 2) {
            _state.update { it.copy(isLoading = false, results = emptyList(), error = null) }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        when (val result = searchChannels(query)) {
            is OttResult.Success -> _state.update {
                it.copy(isLoading = false, results = result.data, error = null)
            }
            is OttResult.Error -> _state.update {
                it.copy(isLoading = false, error = result.message)
            }
            OttResult.Loading -> Unit
        }
    }
}
