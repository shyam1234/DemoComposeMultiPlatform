package com.ott.platform.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors

enum class PillStyle {
    /** White fill idle; blue fill when focused. */
    Primary,
    /** Outline idle; blue fill when focused. */
    Secondary,
}

@Composable
fun FocusablePillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.Primary,
    compact: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(50)
    val background = when {
        focused -> OttColors.Focus
        style == PillStyle.Primary -> Color.White
        else -> Color.Transparent
    }
    val contentColor = when {
        focused -> Color.White
        style == PillStyle.Primary -> Color.Black
        else -> Color.White
    }
    Box(
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(background, shape)
            .then(
                when {
                    focused -> Modifier
                    style == PillStyle.Secondary ->
                        Modifier.border(1.5.dp, Color.White.copy(alpha = 0.85f), shape)
                    else -> Modifier
                },
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(
                horizontal = if (compact) 16.dp else 22.dp,
                vertical = if (compact) 8.dp else 12.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = contentColor,
            style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
