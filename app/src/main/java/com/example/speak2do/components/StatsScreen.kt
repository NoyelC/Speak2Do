package com.example.speak2do.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.CardBackground
import com.example.speak2do.ui.theme.DarkBackground
import com.example.speak2do.ui.theme.LightCyan
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.PrimaryCyan
import com.example.speak2do.ui.theme.WhiteText

@Composable
fun StatsScreen(recordings: List<RecordingItem>) {
    val total = recordings.size
    val completed = recordings.count { it.isCompleted }
    val pending = total - completed
    val completionRate = if (total > 0) completed.toFloat() / total else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(listOf(PrimaryCyan, LightCyan))
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "Completion Rate",
                    color = WhiteText.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${(completionRate * 100).toInt()}%",
                    color = WhiteText,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { completionRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = WhiteText,
                    trackColor = WhiteText.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$completed of $total tasks completed",
                    color = WhiteText.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        // Stat cards grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Assignment,
                    label = "Total Tasks",
                    value = "$total",
                    iconTint = PrimaryCyan
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.CheckCircle,
                    label = "Completed",
                    value = "$completed",
                    iconTint = Color(0xFF4CAF50)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Pending,
                    label = "Pending",
                    value = "$pending",
                    iconTint = Color(0xFFFFA726)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.TrendingUp,
                    label = "Rate",
                    value = "${(completionRate * 100).toInt()}%",
                    iconTint = LightCyan
                )
            }
        }

        // Recent activity
        item {
            Spacer(Modifier.height(4.dp))
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
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
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
