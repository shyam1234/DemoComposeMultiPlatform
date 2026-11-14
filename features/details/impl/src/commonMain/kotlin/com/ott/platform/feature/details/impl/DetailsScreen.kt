package com.ott.platform.feature.details.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.ott.platform.domain.model.Channel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DetailsRoute(
    contentId: String,
    onPlay: (Channel) -> Unit,
    onBack: () -> Unit,
    viewModel: DetailsViewModel = koinViewModel(parameters = { parametersOf(contentId) }),
) {
    val state by viewModel.state.collectAsState()
    DetailsScreen(
        state = state,
        onPlay = { state.channel?.let(onPlay) },
        onToggleWatchlist = viewModel::onToggleWatchlist,
        onBack = onBack,
    )
}

@Composable
fun DetailsScreen(
    state: DetailsUiState,
    onPlay: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.testTag("details_loading"))
        }
        state.error != null -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.error ?: "Error", modifier = Modifier.testTag("details_error"))
        }
        else -> {
            val channel = state.channel ?: return
            Column(
                modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(OttDimens.ScreenPadding)
                    .testTag("details_content"),
            ) {
                OutlinedButton(onClick = onBack) { Text("Back") }
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = OttColors.SurfaceElevated,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = channel.title.take(1).uppercase(),
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(channel.title, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${channel.categoryName} · Live IPTV",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OttColors.OnSurfaceMuted,
                )
                if (channel.quality != null) {
                    Text("Quality: ${channel.quality}", color = OttColors.OnSurfaceMuted)
                }
                if (channel.isGeoBlocked) {
                    Text("May be geo-blocked", color = OttColors.Warning)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Source: iptv-org free public playlist. Stream availability varies by region and upstream CDN.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OttColors.OnSurfaceMuted,
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = OttColors.Accent),
                        modifier = Modifier.testTag("details_play"),
                    ) { Text("Watch live") }
                    OutlinedButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier.testTag("details_watchlist"),
                    ) {
                        Text(if (state.inWatchlist) "In Watchlist" else "Add to Watchlist")
                    }
                }
            }
        }
    }
}
