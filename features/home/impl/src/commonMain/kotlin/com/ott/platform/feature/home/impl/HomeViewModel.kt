package com.ott.platform.feature.home.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.usecase.GetHomeFeedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val feed: HomeFeed? = null,
    val error: String? = null,
)

class HomeViewModel(
    private val getHomeFeed: GetHomeFeedUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getHomeFeed(24)) {
                is OttResult.Success -> _state.update {
                    it.copy(isLoading = false, feed = result.data, error = null)
                }
                is OttResult.Error -> _state.update {
                    it.copy(isLoading = false, error = result.message)
                }
                OttResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }
}
