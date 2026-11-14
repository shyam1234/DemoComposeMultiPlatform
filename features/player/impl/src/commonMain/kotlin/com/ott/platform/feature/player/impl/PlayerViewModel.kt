package com.ott.platform.feature.player.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.usecase.GetChannelDetailsUseCase
import com.ott.platform.domain.playback.MediaItem
import com.ott.platform.domain.playback.PlayerController
import com.ott.platform.domain.playback.PlayerState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val contentId: String,
    private val getChannelDetails: GetChannelDetailsUseCase,
    private val playerController: PlayerController,
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerController.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), playerController.state.value)

    init {
        viewModelScope.launch {
            when (val result = getChannelDetails(ContentId(contentId))) {
                is OttResult.Success -> {
                    val channel = result.data
                    playerController.prepare(
                        MediaItem(
                            id = channel.id.value,
                            title = channel.title,
                            streamUrl = channel.streamUrl,
                            posterUrl = channel.logoUrl,
                            referrer = channel.referrer,
                            userAgent = channel.userAgent,
                        ),
                    )
                    playerController.play()
                }
                else -> Unit
            }
        }
    }

    fun togglePlayPause() = playerController.togglePlayPause()
    fun seekTo(positionMs: Long) = playerController.seekTo(positionMs)

    override fun onCleared() {
        playerController.release()
        super.onCleared()
    }
}
