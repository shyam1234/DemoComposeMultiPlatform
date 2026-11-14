package com.ott.platform.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ott.platform.designsystem.OttColors
import com.ott.platform.shared.generated.resources.Res
import com.ott.platform.shared.generated.resources.splash_ott
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashRoute(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    holdFor: kotlin.time.Duration = 3.seconds,
) {
    LaunchedEffect(Unit) {
        delay(holdFor)
        onFinished()
    }
    SplashScreen(modifier = modifier)
}

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OttColors.Background)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.splash_ott),
                contentDescription = "OTT splash",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth(0.7f)
                    .testTag("splash_image"),
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "OTT Platform",
                style = MaterialTheme.typography.headlineLarge,
                color = OttColors.OnSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Streaming demo",
                style = MaterialTheme.typography.bodyLarge,
                color = OttColors.AccentSoft,
            )
        }
    }
}
