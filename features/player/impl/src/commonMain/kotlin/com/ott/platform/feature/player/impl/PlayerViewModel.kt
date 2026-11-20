package com.ott.platform.feature.player.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.ImageSlot
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.playback.MediaItem
import com.ott.platform.domain.playback.PlayerController
import com.ott.platform.domain.playback.PlayerState
import com.ott.platform.domain.playback.PlayerStatus
import com.ott.platform.domain.usecase.GetChannelDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val contentId: String,
    private val getChannelDetails: GetChannelDetailsUseCase,
    private val playerController: PlayerController,
) : ViewModel() {

    private val missingStreamState = MutableStateFlow<PlayerState?>(null)

    val playerState: StateFlow<PlayerState> = combine(
        playerController.state,
        missingStreamState,
    ) { engineState, fallback ->
        fallback ?: engineState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), playerController.state.value)

    init {
        viewModelScope.launch {
            when (val result = getChannelDetails(ContentId(contentId))) {
                is OttResult.Success -> {
                    val channel = result.data
                    val playableUrl = channel.streamUrl.ifBlank { channel.trailerUrl.orEmpty() }
                    if (playableUrl.isBlank()) {
                        missingStreamState.value = PlayerState(
                            status = PlayerStatus.Error,
                            media = MediaItem(
                                id = channel.id.value,
                                title = channel.title,
                                streamUrl = "",
                                posterUrl = channel.imageFor(ImageSlot.DetailsBackdrop),
                            ),
                            errorMessage = "No playback stream URL published for \"${channel.title}\" yet.",
                        )
                    } else {
                        missingStreamState.value = null
                        playerController.prepare(
                            MediaItem(
                                id = channel.id.value,
                                title = channel.title,
                                streamUrl = playableUrl,
                                posterUrl = channel.imageFor(ImageSlot.DetailsBackdrop),
                                referrer = channel.referrer,
                                userAgent = channel.userAgent,
                            ),
                        )
                        playerController.play()
                    }
                }
                is OttResult.Error -> {
                    missingStreamState.value = PlayerState(
                        status = PlayerStatus.Error,
                        errorMessage = result.message,
                    )
                }
                OttResult.Loading -> Unit
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
