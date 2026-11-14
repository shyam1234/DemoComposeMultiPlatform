package com.ott.platform.platform.android

import android.content.Context
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ott.platform.domain.playback.MediaItem
import com.ott.platform.domain.playback.PlayerEngine
import com.ott.platform.domain.playback.PlayerState
import com.ott.platform.domain.playback.PlayerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class Media3PlayerEngine(
    context: Context,
) : PlayerEngine {

    private val player = ExoPlayer.Builder(context.applicationContext).build()
    private val _state = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update {
                it.copy(status = if (isPlaying) PlayerStatus.Playing else if (player.playbackState == Player.STATE_READY) PlayerStatus.Paused else it.status)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val status = when (playbackState) {
                Player.STATE_BUFFERING -> PlayerStatus.Buffering
                Player.STATE_READY -> if (player.isPlaying) PlayerStatus.Playing else PlayerStatus.Ready
                Player.STATE_ENDED -> PlayerStatus.Ended
                else -> PlayerStatus.Idle
            }
            _state.update {
                it.copy(
                    status = status,
                    durationMs = player.duration.coerceAtLeast(0L),
                    positionMs = player.currentPosition.coerceAtLeast(0L),
                )
            }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _state.update {
                it.copy(status = PlayerStatus.Error, errorMessage = error.message ?: "Playback error")
            }
        }
    }

    init {
        player.addListener(listener)
    }

    override fun prepare(media: MediaItem) {
        _state.update { it.copy(media = media, status = PlayerStatus.Buffering, errorMessage = null) }
        player.setMediaItem(ExoMediaItem.fromUri(media.streamUrl))
        player.prepare()
    }

    override fun play() = player.play()

    override fun pause() = player.pause()

    override fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    override fun selectSubtitle(trackId: String?) = Unit

    override fun selectAudioTrack(trackId: String) = Unit

    override fun selectVideoQuality(qualityId: String) = Unit

    override fun release() {
        player.removeListener(listener)
        player.release()
        _state.value = PlayerState()
    }
}
