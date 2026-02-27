package com.example.speak2do.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.ui.theme.Dimens
import com.example.speak2do.ui.theme.ErrorRed
import com.example.speak2do.ui.theme.WhiteText
import com.example.speak2do.util.formatTime

private val VoiceDarkStart = Color(0xFF1F2A44)
private val VoiceDarkEnd = Color(0xFF2C3A5A)
private val VoiceDarkAccent = Color(0xFF2F8F9D)
private val VoiceLightStart = Color(0xFFE8F2FF)
private val VoiceLightEnd = Color(0xFFD5E7FF)
private val VoiceLightAccent = Color(0xFF4C7AC2)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VoiceAssistantCard(
    isRecording: Boolean,
    recordingTime: Int,
    spokenText: String,
    voiceLevel: Float,
    isDarkMode: Boolean = true,
    onMicClick: () -> Unit,
    onCancelRecording: () -> Unit = {}
) {
    val cardStart = if (isDarkMode) VoiceDarkStart else VoiceLightStart
    val cardEnd = if (isDarkMode) VoiceDarkEnd else VoiceLightEnd
    val accent = if (isDarkMode) VoiceDarkAccent else VoiceLightAccent
    val titleColor = if (isDarkMode) WhiteText else Color(0xFF183A62)
    val subtitleColor = if (isDarkMode) WhiteText.copy(alpha = 0.72f) else Color(0xFF4D6790)
    val onCardButtonBg = if (isDarkMode) WhiteText.copy(alpha = 0.2f) else Color(0x1A183A62)
    val controlBg = if (isDarkMode) WhiteText.copy(alpha = 0.15f) else Color(0x1A183A62)
    val controlBorderA = if (isDarkMode) WhiteText.copy(alpha = 0.4f) else Color(0x4D4C7AC2)
    val controlBorderB = if (isDarkMode) WhiteText.copy(alpha = 0.1f) else Color(0x1A4C7AC2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .background(
                Brush.linearGradient(listOf(cardStart, cardEnd)),
                RoundedCornerShape(Dimens.CardCornerRadius + 8.dp)
            )
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingMd)
            .semantics { contentDescription = "Voice assistant card" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoiceOrbIcon(isRecording = isRecording, voiceLevel = voiceLevel, isDarkMode = isDarkMode)
                Spacer(Modifier.width(Dimens.SpacingLg))
                Column {
                    Text(
                        "How can I help?",
                        color = titleColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "\"Hey Speak2Do, add a meeting...\"",
                        color = subtitleColor,
                        fontSize = 13.sp
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isRecording,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = onCancelRecording,
                    modifier = Modifier
                        .size(36.dp)
                        .background(onCardButtonBg, CircleShape)
                        .semantics { contentDescription = "Cancel recording" }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = titleColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpacingMd))

        SiriWaveform(
            isRecording = isRecording,
            voiceLevel = voiceLevel,
            isDarkMode = isDarkMode,
            barCount = 14,
            barWidth = 5.dp,
            barSpacing = 3.dp,
            maxBarHeight = 44.dp,
            minBarHeight = 8.dp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
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
                        tint = titleColor,
                        modifier = Modifier.size(Dimens.IconSizeSm)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = formatTime(recordingTime), color = titleColor, fontSize = 14.sp)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = spokenText.isNotEmpty(),
                enter = fadeIn(tween(400)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(200))
            ) {
                Text(text = "\"$spokenText\"", color = titleColor, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(Dimens.SpacingSm))

        if (isRecording) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Dimens.ButtonCornerRadius))
                        .background(if (isDarkMode) WhiteText.copy(alpha = 0.25f) else Color(0x1A183A62))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                if (isDarkMode) listOf(WhiteText.copy(alpha = 0.5f), WhiteText.copy(alpha = 0.15f))
                                else listOf(Color(0x664C7AC2), Color(0x1A4C7AC2))
                            ),
                            shape = RoundedCornerShape(Dimens.ButtonCornerRadius)
                        )
                        .clickable { onCancelRecording() }
                        .padding(vertical = 12.dp)
                        .semantics { contentDescription = "Stop recording" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Stop", color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Dimens.ButtonCornerRadius))
                        .background(if (isDarkMode) WhiteText.copy(alpha = 0.1f) else Color(0x14183A62))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                .background(ErrorRed.copy(alpha = dotAlpha), CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Listening...",
                            color = subtitleColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            val tapIntentModifier = Modifier.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        onMicClick()
                        waitForUpOrCancellation()
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.ButtonCornerRadius))
                    .background(controlBg)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(controlBorderA, controlBorderB)),
                        shape = RoundedCornerShape(Dimens.ButtonCornerRadius)
                    )
                    .then(tapIntentModifier)
                    .padding(vertical = 12.dp)
                    .semantics { contentDescription = "Tap to start recording" },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Dimens.SpacingSm))
                    Text("Tap to Speak", color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun VoiceOrbIcon(isRecording: Boolean, voiceLevel: Float, isDarkMode: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "voiceOrb")
    val ringRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(6000, easing = LinearEasing)),
        label = "ringRotation"
    )

    val normalized = (voiceLevel / 10f).coerceIn(0f, 1f)
    val activeGlow by animateFloatAsState(
        targetValue = if (isRecording) 0.22f + normalized * 0.35f else 0.16f,
        animationSpec = tween(220),
        label = "activeGlow"
    )
    val accent = if (isDarkMode) VoiceDarkAccent else VoiceLightAccent
    val ringMid = if (isDarkMode) VoiceDarkEnd else Color(0xFFBFD8FF)
    val orbTop = if (isDarkMode) VoiceDarkEnd else Color(0xFFDCEBFF)
    val orbBottom = if (isDarkMode) VoiceDarkAccent else VoiceLightAccent

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.semantics {
            contentDescription = if (isRecording) "Microphone active" else "Microphone idle"
        }
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .rotate(ringRotation)
                .background(
                    brush = Brush.sweepGradient(
                        listOf(
                            WhiteText.copy(alpha = 0.18f),
                            accent.copy(alpha = activeGlow),
                            ringMid.copy(alpha = activeGlow),
                            WhiteText.copy(alpha = 0.18f)
                        )
                    ),
                    shape = CircleShape
                )
                .padding(5.dp)
                .background((if (isDarkMode) VoiceDarkStart else Color(0xFFB5CCF0)).copy(alpha = 0.2f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(58.dp)
                .background(
                    Brush.verticalGradient(listOf(orbTop, orbBottom)),
                    RoundedCornerShape(Dimens.ButtonCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = "Microphone",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun SiriWaveform(
    isRecording: Boolean,
    voiceLevel: Float,
    isDarkMode: Boolean = true,
    barCount: Int = 18,
    barWidth: Dp = 6.dp,
    barSpacing: Dp = 4.dp,
    maxBarHeight: Dp = 60.dp,
    minBarHeight: Dp = 12.dp
) {
    val waveAccent = if (isDarkMode) VoiceDarkAccent else VoiceLightAccent

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
                                listOf(WhiteText.copy(alpha = 0.22f), waveAccent.copy(alpha = 0.2f))
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
                            listOf(WhiteText.copy(alpha = 1f), waveAccent.copy(alpha = 1f))
                        )
                    )
            )
            Spacer(modifier = Modifier.width(barSpacing))
        }
    }
}
