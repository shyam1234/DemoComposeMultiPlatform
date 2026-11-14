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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.delay

/**
 * Multi-item billboard with auto-advance + page dots.
 * TV keeps left-copy / right-art leanback layout; phone uses full-width stacked copy.
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

    LaunchedEffect(channels.size, pagerState.settledPage) {
        if (channels.size <= 1) return@LaunchedEffect
        delay(autoAdvanceMs)
        val next = (pagerState.settledPage + 1) % channels.size
        pagerState.animateScrollToPage(next)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OttColors.Surface),
    ) {
        RemoteImage(
            url = channel.logoUrl,
            contentDescription = channel.title,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (isTv) 280.dp else 16.dp,
                    end = if (isTv) 24.dp else 16.dp,
                    top = if (isTv) 24.dp else 12.dp,
                    bottom = if (isTv) 24.dp else 72.dp,
                ),
            contentScale = ContentScale.Fit,
        )
        if (isTv) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black.copy(alpha = 0.92f),
                                0.42f to Color.Black.copy(alpha = 0.55f),
                                0.75f to Color.Transparent,
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
                                0.55f to Color.Transparent,
                                1.0f to Color.Black,
                            )
                        } else {
                            arrayOf(
                                0.0f to Color.Black.copy(alpha = 0.35f),
                                0.35f to Color.Transparent,
                                0.55f to Color.Black.copy(alpha = 0.45f),
                                1.0f to Color.Black.copy(alpha = 0.92f),
                            )
                        },
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(if (isTv) 0.55f else 1f)
                .padding(
                    start = horizontal,
                    end = horizontal,
                    bottom = if (isTv) 36.dp else 34.dp,
                ),
        ) {
            Text(
                text = channel.categoryName.uppercase(),
                style = if (isTv) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                color = OttColors.AccentSoft,
                fontWeight = FontWeight.Medium,
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
            if (isTv) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Live from iptv-org · free public stream",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OttColors.OnSurfaceMuted,
                )
            }
            Spacer(Modifier.height(if (isTv) 22.dp else 12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FocusablePillButton(
                    label = "Watch",
                    onClick = onPlay,
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
