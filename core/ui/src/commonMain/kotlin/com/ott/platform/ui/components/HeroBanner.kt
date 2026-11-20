package com.ott.platform.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors
import com.ott.platform.designsystem.OttDimens
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ImageSlot
import kotlinx.coroutines.delay

/**
 * Multi-item billboard with auto-advance + page dots.
 * Renders full-bleed CMS Hero/Landscape artwork with metadata & synopsis.
 */
@Composable
fun HeroBannerCarousel(
    channels: List<Channel>,
    onPlay: (Channel) -> Unit,
    onDetails: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = OttDimens.HeroHeight,
    isTv: Boolean = false,
    autoAdvanceMs: Long = 6_000L,
) {
    if (channels.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { channels.size })
    var heroHasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(channels.size, pagerState.settledPage, heroHasFocus) {
        if (channels.size <= 1 || heroHasFocus) return@LaunchedEffect
        delay(autoAdvanceMs)
        val next = (pagerState.settledPage + 1) % channels.size
        pagerState.animateScrollToPage(next)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .onFocusChanged { heroHasFocus = it.hasFocus }
            .testTag("hero_banner"),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val channel = channels[page]
            HeroBannerPage(
                channel = channel,
                onPlay = { onPlay(channel) },
                onDetails = { onDetails(channel) },
                isTv = isTv,
            )
        }
        if (channels.size > 1) {
            Row(
                modifier = Modifier
                    .align(if (isTv) Alignment.BottomEnd else Alignment.BottomCenter)
                    .padding(
                        end = if (isTv) OttDimens.ScreenPadding else 0.dp,
                        bottom = if (isTv) 20.dp else 10.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(channels.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) OttColors.Focus else OttColors.Focus.copy(alpha = 0.35f),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBanner(
    channel: Channel,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = OttDimens.HeroHeight,
    isTv: Boolean = false,
) {
    HeroBannerCarousel(
        channels = listOf(channel),
        onPlay = { onPlay() },
        onDetails = { onDetails() },
        modifier = modifier,
        height = height,
        isTv = isTv,
        autoAdvanceMs = Long.MAX_VALUE,
    )
}

@Composable
private fun HeroBannerPage(
    channel: Channel,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    isTv: Boolean,
) {
    val horizontal = if (isTv) OttDimens.ScreenPadding else OttDimens.PhoneScreenPadding
    val heroArtUrl = remember(channel.id, channel.images, channel.backdropUrl, channel.logoUrl) {
        channel.imageFor(ImageSlot.Hero)
    }
    val metaLine = remember(channel) {
        listOfNotNull(
            channel.releaseYear,
            channel.runtimeMinutes?.let { if (it >= 60) "${it / 60}h ${it % 60}m" else "${it}m" },
            channel.parentalRating,
            channel.imdbRating?.let { "IMDb $it" },
            channel.genres.take(2).takeIf { it.isNotEmpty() }?.joinToString(" · "),
        ).ifEmpty { listOf(channel.categoryName) }
            .joinToString("  ·  ")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OttColors.Surface),
    ) {
        RemoteImage(
            url = heroArtUrl,
            contentDescription = channel.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (isTv) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black.copy(alpha = 0.94f),
                                0.44f to Color.Black.copy(alpha = 0.65f),
                                0.80f to Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = if (isTv) {
                            arrayOf(
                                0.0f to Color.Transparent,
                                0.50f to Color.Transparent,
                                1.0f to Color.Black,
                            )
                        } else {
                            arrayOf(
                                0.0f to Color.Black.copy(alpha = 0.25f),
                                0.35f to Color.Transparent,
                                0.55f to Color.Black.copy(alpha = 0.58f),
                                1.0f to Color.Black.copy(alpha = 0.94f),
                            )
                        },
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(if (isTv) 0.56f else 1f)
                .padding(
                    start = horizontal,
                    end = horizontal,
                    bottom = if (isTv) 36.dp else 32.dp,
                ),
        ) {
            Text(
                text = metaLine.uppercase(),
                style = if (isTv) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                color = OttColors.AccentSoft,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(if (isTv) 8.dp else 4.dp))
            Text(
                text = channel.title,
                style = if (isTv) MaterialTheme.typography.displayLarge else MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = if (isTv) 2 else 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (channel.synopsis.isNotBlank()) {
                Spacer(Modifier.height(if (isTv) 10.dp else 4.dp))
                Text(
                    text = channel.synopsis,
                    style = if (isTv) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall,
                    color = OttColors.OnSurfaceMuted,
                    maxLines = if (isTv) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(if (isTv) 22.dp else 12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FocusablePillButton(
                    label = if (channel.isPlayable) "Watch Now" else "Explore",
                    onClick = if (channel.isPlayable) onPlay else onDetails,
                    style = PillStyle.Primary,
                    compact = !isTv,
                    modifier = Modifier.testTag("hero_play"),
                )
                Spacer(Modifier.width(10.dp))
                FocusablePillButton(
                    label = "Details",
                    onClick = onDetails,
                    style = PillStyle.Secondary,
                    compact = !isTv,
                    modifier = Modifier.testTag("hero_details"),
                )
            }
        }
    }
}
