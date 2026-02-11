package com.example.speak2do.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.ui.theme.PrimaryCyan
import com.example.speak2do.ui.theme.LightCyan
import com.example.speak2do.ui.theme.WhiteText
import com.example.speak2do.util.formatTime

@Composable
fun VoiceAssistantCard(
    isRecording: Boolean,
    recordingTime: Int,
    spokenText: String,
    onMicClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(LightCyan, PrimaryCyan)
                ),
                RoundedCornerShape(28.dp)
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PulsingMicIcon(isRecording = isRecording)

            Spacer(Modifier.width(16.dp))

            Column {
                Text("How can I help?", color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "\"Hey Speak2Do, add a meeting...\"",
                    color = WhiteText.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SiriWaveform(isRecording)

        if (isRecording) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = WhiteText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formatTime(recordingTime),
                    color = WhiteText,
                    fontSize = 14.sp
                )
            }
        }

        if (spokenText.isNotEmpty()) {
            Text(
                text = "\"$spokenText\"",
                color = WhiteText,
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Glassmorphism "Tap to Speak" button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(WhiteText.copy(alpha = 0.15f))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            WhiteText.copy(alpha = 0.4f),
                            WhiteText.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onMicClick() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = WhiteText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isRecording) "Listening..." else "Tap to Speak",
                    color = WhiteText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PulsingMicIcon(isRecording: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isRecording) 0.3f else 0.1f,
        targetValue = if (isRecording) 0.8f else 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isRecording) 600 else 1500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.25f else 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isRecording) 600 else 1500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size((60 * glowScale).dp)
                .background(
                    WhiteText.copy(alpha = glowAlpha * 0.3f),
                    CircleShape
                )
        )

        // Inner glow ring
        Box(
            modifier = Modifier
                .size((60 * ((glowScale - 1f) * 0.5f + 1f)).dp)
                .background(
                    WhiteText.copy(alpha = glowAlpha * 0.5f),
                    CircleShape
                )
        )

        // Mic icon box
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = (8 * glowAlpha).dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = LightCyan,
                    spotColor = LightCyan
                )
                .background(PrimaryCyan, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = "Microphone",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun SiriWaveform(
    isRecording: Boolean,
    barCount: Int = 18,
    barWidth: Dp = 6.dp,
    barSpacing: Dp = 4.dp,
    maxBarHeight: Dp = 60.dp,
    minBarHeight: Dp = 12.dp
) {
    if (!isRecording) return

    val transition = rememberInfiniteTransition(label = "siriWave")

    val bars = List(barCount) { index ->
        transition.animateFloat(
            initialValue = minBarHeight.value,
            targetValue = maxBarHeight.value,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 500 + index * 35,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$index"
        )
    }

    Row(
        modifier = Modifier
            .height(maxBarHeight)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        bars.forEach { height ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(height.value.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            listOf(WhiteText, LightCyan)
                        )
                    )
            )
            Spacer(modifier = Modifier.width(barSpacing))
        }
    }
}
