package com.ott.platform.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors
import com.ott.platform.designsystem.OttDimens

/**
 * Compact phone app bar: brand only, respects status bar insets.
 */
@Composable
fun OttPhoneTopBar(
    modifier: Modifier = Modifier,
    brand: String = "OTT",
    subtitle: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OttColors.Background)
            .statusBarsPadding()
            .padding(
                horizontal = OttDimens.PhoneScreenPadding,
                vertical = 12.dp,
            )
            .testTag("ott_phone_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = brand,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("ott_brand"),
        )
        if (subtitle != null) {
            Text(
                text = "  ·  $subtitle",
                style = MaterialTheme.typography.bodyMedium,
                color = OttColors.OnSurfaceMuted,
            )
        }
    }
}
