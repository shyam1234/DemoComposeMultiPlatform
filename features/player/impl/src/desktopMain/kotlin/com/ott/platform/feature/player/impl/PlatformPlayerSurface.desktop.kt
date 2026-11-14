package com.ott.platform.feature.player.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ott.platform.designsystem.OttColors

@Composable
actual fun PlatformPlayerSurface(modifier: Modifier) {
    Box(
        modifier = modifier.background(OttColors.SurfaceElevated),
        contentAlignment = Alignment.Center,
    ) {
        Text("Desktop demo player (no native surface)", color = Color.White)
    }
}
