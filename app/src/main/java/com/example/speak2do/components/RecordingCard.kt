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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.*

@Composable
fun RecordingCard(
    item: RecordingItem,
    onToggleCompleted: (Long, Boolean) -> Unit,
    searchQuery: String = ""
) {
    val checkboxScale by animateFloatAsState(
        targetValue = if (item.isCompleted) 1.15f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "checkboxScale"
    )

    val cardGradient = if (item.isCompleted) {
        Brush.linearGradient(listOf(CompletedGradientStart, CardBackground))
    } else {
        Brush.linearGradient(listOf(PendingGradientStart, CardBackground))
    }

    val borderGradient = if (item.isCompleted) {
        Brush.linearGradient(
            listOf(SuccessGreen.copy(alpha = 0.3f), SuccessGreen.copy(alpha = 0.05f))
        )
    } else {
        Brush.linearGradient(
            listOf(PrimaryCyan.copy(alpha = 0.4f), LightCyan.copy(alpha = 0.08f))
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(Dimens.CardCornerRadius))
            .border(
                width = 1.dp,
                brush = borderGradient,
                shape = RoundedCornerShape(Dimens.CardCornerRadius)
            )
            .background(cardGradient)
            .semantics {
                contentDescription = "Task: ${item.text}, ${if (item.isCompleted) "completed" else "pending"}"
            }
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(Dimens.AccentBarWidth)
                .fillMaxHeight()
                .background(
                    if (item.isCompleted)
                        Brush.verticalGradient(listOf(SuccessGreen, SuccessGreenDark))
                    else
                        Brush.verticalGradient(listOf(LightCyan, PrimaryCyan))
                )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(Dimens.SpacingLg),
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
                        checkedColor = SuccessGreen,
                        uncheckedColor = MutedText,
                        checkmarkColor = WhiteText
                    ),
                    modifier = Modifier
                        .size(Dimens.MinTouchTarget)
                        .scale(checkboxScale)
                        .semantics {
                            contentDescription = if (item.isCompleted) "Mark as incomplete" else "Mark as complete"
                        }
                )

                Spacer(Modifier.width(6.dp))

                // Highlighted search text
                if (searchQuery.isNotBlank() && item.text.contains(searchQuery, ignoreCase = true)) {
                    HighlightedText(
                        text = item.text,
                        query = searchQuery,
                        isCompleted = item.isCompleted,
                        modifier = Modifier.weight(1f)
                    )
                } else {
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
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 34.dp),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.AccessTime,
                        contentDescription = "Created at",
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

@Composable
fun HighlightedText(
    text: String,
    query: String,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val baseColor = if (isCompleted) MutedText else WhiteText
    val highlightColor = WarningOrange

    val annotatedString = buildAnnotatedString {
        var startIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()

        while (startIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                withStyle(SpanStyle(color = baseColor)) {
                    append(text.substring(startIndex))
                }
                break
            }
            if (matchIndex > startIndex) {
                withStyle(SpanStyle(color = baseColor)) {
                    append(text.substring(startIndex, matchIndex))
                }
            }
            withStyle(
                SpanStyle(
                    color = highlightColor,
                    fontWeight = FontWeight.Bold,
                    background = highlightColor.copy(alpha = 0.15f)
                )
            ) {
                append(text.substring(matchIndex, matchIndex + query.length))
            }
            startIndex = matchIndex + query.length
        }
    }

    Text(
        text = annotatedString,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableRecordingCard(
    item: RecordingItem,
    onToggleCompleted: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    searchQuery: String = ""
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.4f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(item.id)
                true
            } else {
                false
            }
        }
    )

    val swipeProgress = dismissState.progress

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> ErrorRed
                    else -> Color.Transparent
                },
                label = "swipeBg"
            )

            val iconScale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 0.8f,
                animationSpec = tween(200),
                label = "deleteIconScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Dimens.CardCornerRadius))
                    .background(color)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete task",
                        tint = WhiteText,
                        modifier = Modifier
                            .size(Dimens.IconSizeLg)
                            .scale(iconScale)
                    )
                    Text(
                        text = "Delete",
                        color = WhiteText.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        RecordingCard(
            item = item,
            onToggleCompleted = onToggleCompleted,
            searchQuery = searchQuery
        )
    }
}
