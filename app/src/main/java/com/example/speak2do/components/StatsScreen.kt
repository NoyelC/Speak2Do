package com.example.speak2do.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.*

@Composable
fun StatsScreen(recordings: List<RecordingItem>) {
    val total = recordings.size
    val completed = recordings.count { it.isCompleted }
    val pending = total - completed
    val completionRate = if (total > 0) completed.toFloat() / total else 0f

    // Animated values
    val animatedRate by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(600),
        label = "animRate"
    )
    val animatedTotal by animateIntAsState(
        targetValue = total,
        animationSpec = tween(400),
        label = "animTotal"
    )
    val animatedCompleted by animateIntAsState(
        targetValue = completed,
        animationSpec = tween(400),
        label = "animCompleted"
    )
    val animatedPending by animateIntAsState(
        targetValue = pending,
        animationSpec = tween(400),
        label = "animPending"
    )
    val animatedPercent by animateIntAsState(
        targetValue = (completionRate * 100).toInt(),
        animationSpec = tween(600),
        label = "animPercent"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        item {
            Text(
                text = "Statistics",
                fontSize = 24.sp,
                color = WhiteText,
                fontWeight = FontWeight.Bold
            )
        }

        // Completion rate card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.CardCornerRadius))
                    .background(
                        Brush.linearGradient(listOf(PrimaryCyan, LightCyan))
                    )
                    .padding(Dimens.SpacingXl)
                    .semantics {
                        contentDescription = "Completion rate: $animatedPercent percent, $completed of $total tasks completed"
                    }
            ) {
                Text(
                    text = "Completion Rate",
                    color = WhiteText.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(Dimens.SpacingSm))
                Text(
                    text = "$animatedPercent%",
                    color = WhiteText,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(Dimens.SpacingMd))
                LinearProgressIndicator(
                    progress = { animatedRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = WhiteText,
                    trackColor = WhiteText.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(Dimens.SpacingSm))
                Text(
                    text = "$animatedCompleted of $animatedTotal tasks completed",
                    color = WhiteText.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        // Stat cards grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Assignment,
                    label = "Total Tasks",
                    value = "$animatedTotal",
                    iconTint = PrimaryCyan
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.CheckCircle,
                    label = "Completed",
                    value = "$animatedCompleted",
                    iconTint = SuccessGreen
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
                    iconTint = WarningOrange
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.TrendingUp,
                    label = "Rate",
                    value = "$animatedPercent%",
                    iconTint = LightCyan
                )
            }
        }

        // Recent activity
        item {
            Spacer(Modifier.height(Dimens.SpacingXs))
            Text(
                text = "Recent Activity",
                fontSize = 18.sp,
                color = WhiteText,
                fontWeight = FontWeight.Bold
            )
        }

        if (recordings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No activity yet", color = MutedText, fontSize = 14.sp)
                }
            }
        } else {
            val recent = recordings.takeLast(5).reversed()
            items(recent.size) { index ->
                AnimatedListItem(index = index) {
                    RecordingCard(item = recent[index], onToggleCompleted = { _, _ -> })
                }
            }
        }

        item {
            Spacer(Modifier.height(Dimens.SpacingXl))
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.SmallCornerRadius + 4.dp))
            .background(CardBackground)
            .padding(Dimens.SpacingLg)
            .semantics { contentDescription = "$label: $value" }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(Dimens.SmallCornerRadius))
                .background(iconTint.copy(alpha = 0.15f)),
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
            color = WhiteText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = MutedText,
            fontSize = 13.sp
        )
    }
}
