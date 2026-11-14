package com.ott.platform.platform.android

import android.content.Context
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
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

    private val appContext = context.applicationContext
    private val _state = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _state.asStateFlow()

    private var released = false

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val current = player
            _state.update {
                it.copy(
                    status = when {
                        isPlaying -> PlayerStatus.Playing
                        current.playbackState == Player.STATE_READY -> PlayerStatus.Paused
                        else -> it.status
                    },
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val current = player
            val status = when (playbackState) {
                Player.STATE_BUFFERING -> PlayerStatus.Buffering
                Player.STATE_READY -> if (current.isPlaying) PlayerStatus.Playing else PlayerStatus.Ready
                Player.STATE_ENDED -> PlayerStatus.Ended
                else -> PlayerStatus.Idle
            }
            _state.update {
                it.copy(
                    status = status,
                    durationMs = current.duration.coerceAtLeast(0L),
                    positionMs = current.currentPosition.coerceAtLeast(0L),
                )
            }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _state.update {
                it.copy(
                    status = PlayerStatus.Error,
                    errorMessage = error.cause?.message ?: error.message ?: "Playback error",
                )
            }
        }
    }

    // Must be after [listener] — createPlayer() registers it.
    private var player: ExoPlayer = createPlayer()

    private fun createPlayer(userAgent: String? = null): ExoPlayer {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent ?: DEFAULT_USER_AGENT)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)

        return ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
            .build()
            .also {
                it.addListener(listener)
                released = false
            }
    }

    private fun ensurePlayer(userAgent: String? = null): ExoPlayer {
        if (released) {
            player = createPlayer(userAgent)
        }
        return player
    }

    override fun prepare(media: MediaItem) {
        if (!released && media.userAgent != null) {
            try {
                player.removeListener(listener)
                player.release()
            } catch (_: Exception) {
                // ignore
            }
            released = true
        }
        val exo = ensurePlayer(media.userAgent)
        _state.update { it.copy(media = media, status = PlayerStatus.Buffering, errorMessage = null) }
        exo.setMediaItem(ExoMediaItem.fromUri(media.streamUrl))
        exo.prepare()
        exo.playWhenReady = true
    }

    override fun play() {
        ensurePlayer().play()
    }

    override fun pause() {
        ensurePlayer().pause()
    }

    override fun seekTo(positionMs: Long) {
        ensurePlayer().seekTo(positionMs)
    }

    override fun selectSubtitle(trackId: String?) = Unit

    override fun selectAudioTrack(trackId: String) = Unit

    override fun selectVideoQuality(qualityId: String) = Unit

    override fun release() {
        if (released) return
        try {
            player.removeListener(listener)
            player.release()
        } catch (_: Exception) {
            // ignore
        }
        released = true
        _state.value = PlayerState()
    }

    override fun nativePlayer(): Any = ensurePlayer()

    private companion object {
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
}
