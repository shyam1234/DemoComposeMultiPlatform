package com.ott.platform.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors
import com.ott.platform.designsystem.OttDimens
import com.ott.platform.domain.model.Channel

@Composable
fun HeroBanner(
    channel: Channel,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = OttDimens.HeroHeight,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(OttColors.SurfaceElevated)
            .testTag("hero_banner"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            OttColors.Accent.copy(alpha = 0.35f),
                            OttColors.Background,
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(OttDimens.ScreenPadding),
        ) {
            Text(
                text = channel.categoryName.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = OttColors.AccentSoft,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = channel.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Live IPTV channel from iptv-org · free public stream",
                style = MaterialTheme.typography.bodyMedium,
                color = OttColors.OnSurfaceMuted,
            )
            Spacer(Modifier.height(16.dp))
            Row {
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = OttColors.Accent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("hero_play"),
                ) {
                    Text("Play")
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = onDetails,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("hero_details"),
                ) {
                    Text("Details")
                }
            }
        }
    }
}
