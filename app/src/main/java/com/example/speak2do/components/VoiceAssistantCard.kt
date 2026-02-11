package com.example.speak2do.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.ui.theme.PrimaryPurple
import com.example.speak2do.ui.theme.SecondaryIndigo
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
                    listOf(SecondaryIndigo, PrimaryPurple)
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
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(PrimaryPurple, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    WhiteText.copy(alpha = 0.1f),
                    RoundedCornerShape(16.dp)
                )
                .padding(vertical = 12.dp)
                .clickable { onMicClick() },
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
fun SiriWaveform(
    isRecording: Boolean,
    barCount: Int = 18,
    barWidth: Dp = 6.dp,
    barSpacing: Dp = 4.dp,
    maxBarHeight: Dp = 60.dp,
    minBarHeight: Dp = 12.dp,
    color: Color = Color(0xFF4FC3F7)
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
                    .background(color, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(barSpacing))
        }
    }
}
