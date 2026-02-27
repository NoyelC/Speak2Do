package com.example.speak2do.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.Dimens
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.PrimaryCyan

private val StatsScreenNavy = Color(0xFF0A1224)
private val StatsDeepNavy = Color(0xFF1B2B4B)
private val StatsAccentBlue = Color(0xFF3FB6D3)
private val StatsGlassBg = Color(0x1AFFFFFF)
private val StatsGlassBorder = Color(0x44FFFFFF)
private val StatsCardNavy = Color(0xFF102241)
private val StatsWhite = Color(0xFFFFFFFF)
private val StatsLightBg = Color(0xFFF4F8FF)
private val StatsLightCard = Color(0xFFEAF1FF)
private val StatsLightText = Color(0xFF102544)
private val StatsLightMuted = Color(0xFF5C7391)
private val StatsPendingAccent = Color(0xFF8E7CFF)

@Composable
fun StatsScreen(
    recordings: List<RecordingItem>,
    isDarkMode: Boolean = true
) {
    val total = recordings.size
    val completed = recordings.count { it.isCompleted }
    val pending = total - completed
    val completionRate = if (total > 0) completed.toFloat() / total else 0f

    val animatedRate by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(650),
        label = "statsRate"
    )
    val animatedTotal by animateIntAsState(total, tween(450), label = "statsTotal")
    val animatedCompleted by animateIntAsState(completed, tween(450), label = "statsCompleted")
    val animatedPending by animateIntAsState(pending, tween(450), label = "statsPending")
    val animatedPercent by animateIntAsState((completionRate * 100).toInt(), tween(650), label = "statsPercent")
    val screenColor by animateColorAsState(
        targetValue = if (isDarkMode) StatsScreenNavy else StatsLightBg,
        animationSpec = tween(280),
        label = "statsScreenColor"
    )
    val heroStart by animateColorAsState(
        targetValue = if (isDarkMode) StatsDeepNavy else Color(0xFF183F77),
        animationSpec = tween(280),
        label = "statsHeroStart"
    )
    val heroEnd by animateColorAsState(
        targetValue = if (isDarkMode) StatsAccentBlue else Color(0xFF64A6FF),
        animationSpec = tween(280),
        label = "statsHeroEnd"
    )
    val cardColor by animateColorAsState(
        targetValue = if (isDarkMode) StatsCardNavy else StatsLightCard,
        animationSpec = tween(280),
        label = "statsCardColor"
    )
    val glassColor by animateColorAsState(
        targetValue = if (isDarkMode) StatsGlassBg else Color(0xCCFFFFFF),
        animationSpec = tween(280),
        label = "statsGlassColor"
    )
    val glassBorderColor by animateColorAsState(
        targetValue = if (isDarkMode) StatsGlassBorder else Color(0x668AB6FF),
        animationSpec = tween(280),
        label = "statsGlassBorderColor"
    )
    val primaryText by animateColorAsState(
        targetValue = if (isDarkMode) StatsWhite else StatsLightText,
        animationSpec = tween(280),
        label = "statsPrimaryText"
    )
    val secondaryText by animateColorAsState(
        targetValue = if (isDarkMode) StatsWhite.copy(alpha = 0.8f) else StatsLightMuted,
        animationSpec = tween(280),
        label = "statsSecondaryText"
    )

    val waveShape = GenericShape { size, _ ->
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height * 0.78f)
        quadraticBezierTo(size.width * 0.78f, size.height * 1.02f, size.width * 0.46f, size.height * 0.86f)
        quadraticBezierTo(size.width * 0.18f, size.height * 0.7f, 0f, size.height * 0.84f)
        close()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .clip(waveShape)
                .background(
                    Brush.linearGradient(listOf(heroStart, heroEnd))
                )
                .drawWithCache {
                    onDrawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color(0x66FFFFFF), Color.Transparent)),
                            radius = size.minDimension * 0.48f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.86f, size.height * 0.2f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    if (isDarkMode) Color(0x665EC2FF) else Color(0x559FD0FF),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension * 0.34f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.12f)
                        )
                    }
                }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.ScreenPadding,
                end = Dimens.ScreenPadding,
                top = 96.dp,
                bottom = Dimens.ScreenPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            item {
                Text(
                    text = "Statistics",
                    fontSize = 30.sp,
                    color = primaryText,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Track your productivity momentum",
                    fontSize = 14.sp,
                    color = secondaryText
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(glassColor)
                        .border(BorderStroke(1.dp, glassBorderColor), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                        .semantics {
                            contentDescription = "Completion rate: $animatedPercent percent, $animatedCompleted of $animatedTotal tasks completed"
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Completion", color = secondaryText, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "$animatedPercent%",
                                color = primaryText,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFF2962FF))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("PROGRESS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { animatedRate },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = if (isDarkMode) StatsWhite else Color(0xFF2E5D95),
                        trackColor = if (isDarkMode) StatsWhite.copy(alpha = 0.22f) else Color(0x332E5D95),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "$animatedCompleted of $animatedTotal tasks completed",
                        color = secondaryText,
                        fontSize = 13.sp
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Assignment,
                        label = "Total",
                        value = "$animatedTotal",
                        iconTint = Color(0xFF7BA6FF),
                        containerColor = cardColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.CheckCircle,
                        label = "Completed",
                        value = "$animatedCompleted",
                        iconTint = Color(0xFF4FC3F7),
                        containerColor = cardColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Pending,
                        label = "Pending",
                        value = "$animatedPending",
                        iconTint = StatsPendingAccent,
                        containerColor = cardColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.TrendingUp,
                        label = "Rate",
                        value = "$animatedPercent%",
                        iconTint = PrimaryCyan,
                        containerColor = cardColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    )
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Recent Activity",
                    fontSize = 20.sp,
                    color = primaryText,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            if (recordings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardColor)
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No activity yet", color = if (isDarkMode) MutedText else secondaryText, fontSize = 14.sp)
                    }
                }
            } else {
                val recent = recordings.takeLast(5).reversed()
                items(recent.size) { index ->
                    AnimatedListItem(index = index) {
                        RecentActivityCard(
                            item = recent[index],
                            isDarkMode = isDarkMode,
                            containerColor = cardColor,
                            primaryText = primaryText,
                            secondaryText = secondaryText
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RecentActivityCard(
    item: RecordingItem,
    isDarkMode: Boolean,
    containerColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val statusColor = if (item.isCompleted) Color(0xFF53D7A4) else StatsPendingAccent
    val statusText = if (item.isCompleted) "Done" else "Pending"
    val progressValue = item.progress.coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isDarkMode) Brush.linearGradient(
                    listOf(
                        Color(0xFF11284A),
                        Color(0xFF0E223E)
                    )
                ) else Brush.linearGradient(
                    listOf(
                        Color(0xFFF6F9FF),
                        containerColor
                    )
                )
            )
            .border(
                BorderStroke(1.dp, if (isDarkMode) Color(0x2FFFFFFF) else Color(0x338AB6FF)),
                RoundedCornerShape(18.dp)
            )
            .padding(0.dp)
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(104.dp)
                .background(statusColor)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(14.dp)
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
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Pending,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = item.text.trim().ifBlank { "Untitled task" }.take(48),
                        color = primaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (item.isCompleted) statusColor else Color.Transparent, CircleShape)
                        .border(
                            BorderStroke(
                                width = 2.dp,
                                color = if (item.isCompleted) statusColor else if (isDarkMode) Color(0x55FFFFFF) else Color(0x558AB6FF)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isCompleted) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (isDarkMode) Color.White else Color(0xFF0F2744),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFF8FB0D8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = item.dateTime,
                    color = secondaryText,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = item.duration,
                    color = secondaryText,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = statusColor,
                trackColor = if (isDarkMode) Color(0x33FFFFFF) else Color(0x33496A93),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    containerColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(BorderStroke(1.dp, Color(0x1FFFFFFF)), RoundedCornerShape(18.dp))
            .padding(Dimens.SpacingLg)
            .semantics { contentDescription = "$label: $value" }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(Dimens.IconSizeMd)
            )
        }

        Spacer(Modifier.height(Dimens.SpacingMd))

        Text(
            text = value,
            color = primaryText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = secondaryText,
            fontSize = 13.sp
        )
    }
}
