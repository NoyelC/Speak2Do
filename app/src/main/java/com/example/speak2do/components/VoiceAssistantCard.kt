package com.example.speak2do.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.ui.theme.*
import com.example.speak2do.util.formatTime

@Composable
fun VoiceAssistantCard(
    isRecording: Boolean,
    recordingTime: Int,
    spokenText: String,
    voiceLevel: Float,
    onMicClick: () -> Unit,
    onCancelRecording: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 340.dp)
            .background(
                Brush.linearGradient(listOf(LightCyan, PrimaryCyan)),
                RoundedCornerShape(Dimens.CardCornerRadius + 8.dp)
            )
            .padding(Dimens.SpacingXl)
            .semantics { contentDescription = "Voice assistant card" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                PulsingMicIcon(isRecording = isRecording)

                Spacer(Modifier.width(Dimens.SpacingLg))

                Column {
                    Text(
                        "How can I help?",
                        color = WhiteText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "\"Hey Speak2Do, add a meeting...\"",
                        color = WhiteText.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }

            // Cancel button during recording
            androidx.compose.animation.AnimatedVisibility(
                visible = isRecording,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = onCancelRecording,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            WhiteText.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .semantics { contentDescription = "Cancel recording" }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = WhiteText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpacingLg))

        SiriWaveform(isRecording, voiceLevel)

        // Recording timer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isRecording,
                enter = fadeIn(tween(300)) + expandVertically(),
                exit = fadeOut(tween(200)) + shrinkVertically()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.Timer,
                        contentDescription = "Recording time",
                        tint = WhiteText,
                        modifier = Modifier.size(Dimens.IconSizeSm)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formatTime(recordingTime),
                        color = WhiteText,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = spokenText.isNotEmpty(),
                enter = fadeIn(tween(400)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(200))
            ) {
                Text(
                    text = "\"$spokenText\"",
                    color = WhiteText,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpacingMd))

        // Action buttons
        if (isRecording) {
            // When recording: show Stop + Listening side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)
            ) {
                // Stop button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Dimens.ButtonCornerRadius))
                        .background(WhiteText.copy(alpha = 0.25f))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    WhiteText.copy(alpha = 0.5f),
                                    WhiteText.copy(alpha = 0.15f)
                                )
                            ),
                            shape = RoundedCornerShape(Dimens.ButtonCornerRadius)
                        )
                        .clickable { onCancelRecording() }
                        .padding(vertical = 14.dp)
                        .semantics { contentDescription = "Stop recording" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = null,
                            tint = WhiteText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Stop",
                            color = WhiteText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Listening indicator
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Dimens.ButtonCornerRadius))
                        .background(WhiteText.copy(alpha = 0.1f))
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Pulsing dot
                        val pulseDot = rememberInfiniteTransition(label = "dot")
                        val dotAlpha by pulseDot.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dotAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    ErrorRed.copy(alpha = dotAlpha),
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Listening...",
                            color = WhiteText.copy(alpha = 0.8f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            // When idle: show Tap to Speak button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.ButtonCornerRadius))
                    .background(WhiteText.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                WhiteText.copy(alpha = 0.4f),
                                WhiteText.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(Dimens.ButtonCornerRadius)
                    )
                    .clickable { onMicClick() }
                    .padding(vertical = 14.dp)
                    .semantics { contentDescription = "Tap to start recording" },
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
                    Spacer(Modifier.width(Dimens.SpacingSm))
                    Text(
                        text = "Tap to Speak",
                        color = WhiteText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PulsingMicIcon(isRecording: Boolean) {
    if (!isRecording) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.semantics { contentDescription = "Microphone idle" }
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(PrimaryCyan, RoundedCornerShape(Dimens.ButtonCornerRadius)),
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
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.semantics {
            contentDescription = if (isRecording) "Microphone active" else "Microphone idle"
        }
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
                    shape = RoundedCornerShape(Dimens.ButtonCornerRadius),
                    ambientColor = LightCyan,
                    spotColor = LightCyan
                )
                .background(PrimaryCyan, RoundedCornerShape(Dimens.ButtonCornerRadius)),
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
    voiceLevel: Float,
    barCount: Int = 18,
    barWidth: Dp = 6.dp,
    barSpacing: Dp = 4.dp,
    maxBarHeight: Dp = 60.dp,
    minBarHeight: Dp = 12.dp
) {
    if (!isRecording) {
        Row(
            modifier = Modifier
                .height(maxBarHeight)
                .padding(top = Dimens.SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(barCount) {
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height((minBarHeight.value + 4f).dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    WhiteText.copy(alpha = 0.25f),
                                    LightCyan.copy(alpha = 0.2f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.width(barSpacing))
            }
        }
        return
    }

    val normalized = (voiceLevel / 10f).coerceIn(0f, 1f)
    val multipliers = remember {
        List(barCount) { i ->
            val phase = (i % 6) + 1
            0.6f + 0.4f * (phase / 6f)
        }
    }
    val targetHeights = multipliers.map { m ->
        minBarHeight.value + (maxBarHeight.value - minBarHeight.value) * (normalized * m)
    }

    Row(
        modifier = Modifier
            .height(maxBarHeight)
            .padding(top = Dimens.SpacingLg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        targetHeights.forEachIndexed { idx, target ->
            val height by animateFloatAsState(
                targetValue = target,
                animationSpec = tween(120, easing = FastOutSlowInEasing),
                label = "barAnim$idx"
            )
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(height.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                WhiteText.copy(alpha = 1f),
                                LightCyan.copy(alpha = 1f)
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.width(barSpacing))
        }
    }
}
