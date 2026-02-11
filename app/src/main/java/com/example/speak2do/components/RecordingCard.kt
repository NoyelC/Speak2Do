package com.example.speak2do.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.CardBackground
import com.example.speak2do.ui.theme.LightCyan
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.PrimaryCyan
import com.example.speak2do.ui.theme.WhiteText

@Composable
fun RecordingCard(item: RecordingItem, onToggleCompleted: (Long, Boolean) -> Unit) {
    val checkboxScale by animateFloatAsState(
        targetValue = if (item.isCompleted) 1.15f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "checkboxScale"
    )

    val cardGradient = if (item.isCompleted) {
        Brush.linearGradient(
            listOf(
                Color(0xFF1A2E1A),
                CardBackground
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFF0F2A3E),
                CardBackground
            )
        )
    }

    val borderGradient = if (item.isCompleted) {
        Brush.linearGradient(
            listOf(
                Color(0xFF4CAF50).copy(alpha = 0.3f),
                Color(0xFF4CAF50).copy(alpha = 0.05f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                PrimaryCyan.copy(alpha = 0.4f),
                LightCyan.copy(alpha = 0.08f)
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                brush = borderGradient,
                shape = RoundedCornerShape(20.dp)
            )
            .background(cardGradient)
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    if (item.isCompleted)
                        Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)))
                    else
                        Brush.verticalGradient(listOf(LightCyan, PrimaryCyan))
                )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggleCompleted(item.id, item.isCompleted) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF4CAF50),
                        uncheckedColor = MutedText,
                        checkmarkColor = WhiteText
                    ),
                    modifier = Modifier
                        .size(24.dp)
                        .scale(checkboxScale)
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = item.text,
                    color = if (item.isCompleted) MutedText else WhiteText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 34.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.dateTime,
                        color = MutedText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Text(
                    text = "\u2022",
                    color = MutedText,
                    fontSize = 12.sp
                )

                Text(
                    text = item.duration,
                    color = if (item.isCompleted) MutedText else PrimaryCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableRecordingCard(
    item: RecordingItem,
    onToggleCompleted: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(item.id)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFD32F2F)
                    else -> Color.Transparent
                },
                label = "swipeBg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(color)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = WhiteText,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        RecordingCard(item = item, onToggleCompleted = onToggleCompleted)
    }
}
