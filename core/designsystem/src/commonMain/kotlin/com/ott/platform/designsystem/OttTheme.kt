package com.ott.platform.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Android OTT / lean-back inspired cinematic palette.
 * Dark-first: deep charcoal surfaces, crimson play accent, soft focus glow.
 */
object OttColors {
    val Background = Color(0xFF0B0D10)
    val Surface = Color(0xFF151922)
    val SurfaceElevated = Color(0xFF1E2430)
    val OnSurface = Color(0xFFF2F4F8)
    val OnSurfaceMuted = Color(0xFF9AA3B2)
    val Accent = Color(0xFFE50914)
    val AccentSoft = Color(0xFFFF6B6B)
    val Focus = Color(0xFFFFFFFF)
    val Success = Color(0xFF3DDC97)
    val Warning = Color(0xFFFFB020)
}

object OttDimens {
    val ScreenPadding = 16.dp
    val ShelfSpacing = 20.dp
    val CardWidth = 140.dp
    val CardHeight = 78.dp
    val TvCardWidth = 220.dp
    val TvCardHeight = 124.dp
    val HeroHeight = 280.dp
    val TvHeroHeight = 420.dp
    val FocusBorder = 3.dp
}

private val OttDarkColors = darkColorScheme(
    primary = OttColors.Accent,
    onPrimary = Color.White,
    secondary = OttColors.AccentSoft,
    background = OttColors.Background,
    surface = OttColors.Surface,
    onBackground = OttColors.OnSurface,
    onSurface = OttColors.OnSurface,
    surfaceVariant = OttColors.SurfaceElevated,
    onSurfaceVariant = OttColors.OnSurfaceMuted,
)

private val OttLightColors = lightColorScheme(
    primary = OttColors.Accent,
    onPrimary = Color.White,
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    onBackground = Color(0xFF121417),
    onSurface = Color(0xFF121417),
)

@Composable
fun OttTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val useDark = darkTheme || isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (useDark) OttDarkColors else OttLightColors,
        typography = OttTypography,
        content = content,
    )
}
