package com.ott.platform.feature.player.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors
import com.ott.platform.domain.playback.PlayerStatus
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val ControlsHideDelay = 5.seconds

@Composable
fun PlayerRoute(
    contentId: String,
    onBack: () -> Unit,
    isTv: Boolean = false,
    viewModel: PlayerViewModel = koinViewModel(parameters = { parametersOf(contentId) }),
) {
    val state by viewModel.playerState.collectAsState()
    PlayerScreen(
        title = state.media?.title ?: "Player",
        status = state.status,
        positionMs = state.positionMs,
        durationMs = state.durationMs,
        error = state.errorMessage,
        streamUrl = state.media?.streamUrl,
        onBack = onBack,
        onToggle = viewModel::togglePlayPause,
        onSeek = viewModel::seekTo,
        isTv = isTv,
    )
}

@Composable
fun PlayerScreen(
    title: String,
    status: PlayerStatus,
    positionMs: Long,
    durationMs: Long,
    error: String?,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    streamUrl: String? = null,
    modifier: Modifier = Modifier,
    isTv: Boolean = false,
) {
    val chromePad = if (isTv) 28.dp else 12.dp
    val isLive = durationMs <= 0L
    val showVodControls = durationMs > 0L
    val keepControlsPinned = status == PlayerStatus.Error ||
        status == PlayerStatus.Paused ||
        status == PlayerStatus.Ended ||
        status == PlayerStatus.Idle

    var controlsVisible by remember { mutableStateOf(true) }
    var interactionEpoch by remember { mutableIntStateOf(0) }

    fun revealControls() {
        controlsVisible = true
        interactionEpoch += 1
    }

    LaunchedEffect(interactionEpoch, status, keepControlsPinned) {
        if (keepControlsPinned) {
            controlsVisible = true
            return@LaunchedEffect
        }
        delay(ControlsHideDelay)
        controlsVisible = false
    }

    val showChrome = controlsVisible || keepControlsPinned

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("player_screen"),
    ) {
        PlatformPlayerSurface(modifier = Modifier.fillMaxSize())

        // Tap empty area to show/hide chrome (drawn under controls so buttons still win hits).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    if (showChrome && !keepControlsPinned) {
                        controlsVisible = false
                    } else {
                        revealControls()
                    }
                },
        )

        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                            ),
                        ),
                    )
                    .statusBarsPadding()
                    .padding(
                        start = chromePad,
                        end = chromePad,
                        top = chromePad,
                        bottom = if (isTv) 48.dp else 24.dp,
                    ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            revealControls()
                            onBack()
                        },
                        modifier = Modifier.testTag("player_close"),
                    ) {
                        Text("← Close", color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = statusLabel(status, error, isLive),
                            color = OttColors.OnSurfaceMuted,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("player_status"),
                        )
                    }
                    if (isLive && status != PlayerStatus.Error) {
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .background(OttColors.Accent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showChrome && status != PlayerStatus.Error,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.88f),
                            ),
                        ),
                    )
                    .navigationBarsPadding()
                    .padding(
                        start = chromePad,
                        end = chromePad,
                        top = if (isTv) 40.dp else 24.dp,
                        bottom = chromePad,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (showVodControls) {
                    Slider(
                        value = positionMs.toFloat().coerceIn(0f, durationMs.toFloat()),
                        onValueChange = {
                            revealControls()
                            onSeek(it.toLong())
                        },
                        valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = OttColors.Accent,
                            activeTrackColor = OttColors.Accent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_seek"),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(formatTime(positionMs), color = OttColors.OnSurfaceMuted, style = MaterialTheme.typography.labelSmall)
                        Text(formatTime(durationMs), color = OttColors.OnSurfaceMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Button(
                    onClick = {
                        revealControls()
                        onToggle()
                    },
                    enabled = status != PlayerStatus.Error,
                    colors = ButtonDefaults.buttonColors(containerColor = OttColors.Accent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("player_toggle"),
                ) {
                    Text(if (status == PlayerStatus.Playing || status == PlayerStatus.Buffering) "Pause" else "Play")
                }
            }
        }

        if (status == PlayerStatus.Error) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .background(Color.Black.copy(alpha = 0.82f), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Playback failed", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error ?: "Stream unavailable",
                    color = OttColors.OnSurfaceMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!streamUrl.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = streamUrl,
                        color = OttColors.OnSurfaceMuted,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onBack) {
                    Text("Back to catalog", color = OttColors.AccentSoft)
                }
            }
        }
    }
}

private fun statusLabel(status: PlayerStatus, error: String?, isLive: Boolean): String =
    when (status) {
        PlayerStatus.Playing -> if (isLive) "Live" else "Playing"
        PlayerStatus.Paused -> "Paused"
        PlayerStatus.Buffering -> "Buffering…"
        PlayerStatus.Ready -> "Ready"
        PlayerStatus.Error -> error ?: "Error"
        PlayerStatus.Ended -> "Ended"
        PlayerStatus.Idle -> "Starting…"
    }

private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    val mm = m.toString().padStart(2, '0')
    val ss = s.toString().padStart(2, '0')
    return if (h > 0) "$h:$mm:$ss" else "$m:$ss"
}
