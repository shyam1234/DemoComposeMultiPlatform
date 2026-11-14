package com.ott.platform.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ott.platform.domain.model.ShelfCardStyle

@Composable
fun ChannelCard(
    channel: Channel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focused: Boolean = false,
    style: ShelfCardStyle = ShelfCardStyle.Landscape,
    isTv: Boolean = false,
) {
    val (width, height) = when (style) {
        ShelfCardStyle.Landscape ->
            if (isTv) OttDimens.TvCardWidth to OttDimens.TvCardHeight
            else OttDimens.CardWidth to OttDimens.CardHeight
        ShelfCardStyle.Portrait ->
            if (isTv) OttDimens.TvPortraitCardWidth to OttDimens.TvPortraitCardHeight
            else OttDimens.PortraitCardWidth to OttDimens.PortraitCardHeight
    }
    ChannelCard(
        channel = channel,
        onClick = onClick,
        modifier = modifier,
        focused = focused,
        width = width,
        height = height,
        style = style,
        isTv = isTv,
    )
}

/**
 * Leanback-style card: no border when idle; bright blue outer ring when focused.
 */
@Composable
fun ChannelCard(
    channel: Channel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focused: Boolean = false,
    width: Dp,
    height: Dp,
    style: ShelfCardStyle = ShelfCardStyle.Landscape,
    isTv: Boolean = false,
) {
    var hasFocus by remember { mutableStateOf(false) }
    val isFocused = focused || hasFocus
    val ringGap = 3.dp
    val shape = RoundedCornerShape(OttDimens.CardCorner)
    val outerShape = RoundedCornerShape(OttDimens.CardCorner + 2.dp)
    val imageScale = when (style) {
        ShelfCardStyle.Portrait -> ContentScale.Crop
        ShelfCardStyle.Landscape -> ContentScale.Fit
    }
    val interaction = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .width(width)
            .onFocusChanged { hasFocus = it.isFocused }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .testTag("channel_card_${channel.id.value}"),
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .then(
                    if (isFocused) {
                        Modifier.border(OttDimens.FocusBorder, OttColors.Focus, outerShape)
                    } else {
                        Modifier
                    },
                )
                .padding(if (isFocused) ringGap else 0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(Color.Black),
            ) {
                RemoteImage(
                    url = channel.logoUrl,
                    contentDescription = channel.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (style == ShelfCardStyle.Landscape) {
                                Modifier.padding(if (isTv) 12.dp else 6.dp)
                            } else {
                                Modifier
                            },
                        ),
                    contentScale = imageScale,
                )
                if (style == ShelfCardStyle.Portrait) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                ),
                            ),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = channel.title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(width)
                .height(OttDimens.CardTitleHeight),
        )
    }
}
