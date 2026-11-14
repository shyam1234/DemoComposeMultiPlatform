package com.ott.platform.feature.player.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors
import com.ott.platform.designsystem.OttDimens
import com.ott.platform.domain.playback.PlayerStatus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlayerRoute(
    contentId: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel(parameters = { parametersOf(contentId) }),
) {
    val state by viewModel.playerState.collectAsState()
    PlayerScreen(
        title = state.media?.title ?: "Player",
        status = state.status,
        positionMs = state.positionMs,
        durationMs = state.durationMs,
        error = state.errorMessage,
        onBack = onBack,
        onToggle = viewModel::togglePlayPause,
        onSeek = viewModel::seekTo,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(OttDimens.ScreenPadding)
            .testTag("player_screen"),
    ) {
        TextButton(onClick = onBack) { Text("Close", color = Color.White) }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(OttColors.SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when (status) {
                        PlayerStatus.Playing -> "Playing (demo engine / native when bound)"
                        PlayerStatus.Paused -> "Paused"
                        PlayerStatus.Buffering -> "Buffering…"
                        PlayerStatus.Ready -> "Ready"
                        PlayerStatus.Error -> error ?: "Error"
                        PlayerStatus.Ended -> "Ended"
                        PlayerStatus.Idle -> "Idle"
                    },
                    color = OttColors.OnSurfaceMuted,
                    modifier = Modifier.testTag("player_status"),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        if (durationMs > 0) {
            Slider(
                value = positionMs.toFloat().coerceAtMost(durationMs.toFloat()),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth().testTag("player_seek"),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(onClick = onToggle, modifier = Modifier.testTag("player_toggle")) {
                Text(if (status == PlayerStatus.Playing) "Pause" else "Play")
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Android uses Media3 when platform module is bound. Other targets use FakePlayerEngine for demo/tests.",
            color = OttColors.OnSurfaceMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
