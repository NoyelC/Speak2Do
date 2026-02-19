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

private val TasksCardBg = Color(0xCC1A1F3A)
private val TasksCardBgDone = Color(0xCC222945)
private val TasksCardBorder = Color(0x338FB3FF)
private val TasksCardTextPrimary = Color(0xFFF2F5FF)
private val TasksCardTextMuted = Color(0xFF8A95B6)
private val TasksCardAccent = Color(0xFF7DA7FF)
private val TasksCardBgLight = Color(0xFFF6F9FF)
private val TasksCardBgDoneLight = Color(0xFFEAF1FF)
private val TasksCardBorderLight = Color(0x668DB7FF)
private val TasksCardTextPrimaryLight = Color(0xFF183A62)
private val TasksCardTextMutedLight = Color(0xFF5C7391)
private val TasksCardAccentLight = Color(0xFF4C7AC2)
private val BaseCardBg = Color(0xFF131D35)
private val BaseCardBgDone = Color(0xFF1B2744)
private val BaseCardBorder = Color(0x335C86D8)
private val BaseCardBorderDone = Color(0x3349CFA4)
private val BaseTextPrimary = Color(0xFFF3F7FF)
private val BaseTextMuted = Color(0xFF93A8C8)
private val BaseAccent = Color(0xFF67B7FF)

@Composable
fun RecordingCard(
    item: RecordingItem,
    onToggleCompleted: (Long, Boolean) -> Unit,
    searchQuery: String = "",
    useTasksStyle: Boolean = false,
    isDarkMode: Boolean = true
) {
    val checkboxScale by animateFloatAsState(
        targetValue = if (item.isCompleted) 1.15f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "checkboxScale"
    )

    val tasksBg = if (isDarkMode) TasksCardBg else TasksCardBgLight
    val tasksBgDone = if (isDarkMode) TasksCardBgDone else TasksCardBgDoneLight
    val tasksBorder = if (isDarkMode) TasksCardBorder else TasksCardBorderLight
    val tasksTextPrimary = if (isDarkMode) TasksCardTextPrimary else TasksCardTextPrimaryLight
    val tasksTextMuted = if (isDarkMode) TasksCardTextMuted else TasksCardTextMutedLight
    val tasksAccent = if (isDarkMode) TasksCardAccent else TasksCardAccentLight

    val cardGradient = if (useTasksStyle) {
        if (item.isCompleted) {
            Brush.linearGradient(listOf(tasksBgDone, tasksBgDone))
        } else {
            Brush.linearGradient(listOf(tasksBg, tasksBg))
        }
    } else {
        if (item.isCompleted) {
            Brush.linearGradient(listOf(BaseCardBgDone, BaseCardBgDone))
        } else {
            Brush.linearGradient(listOf(BaseCardBg, BaseCardBg))
        }
    }

    val borderGradient = if (useTasksStyle) {
        Brush.linearGradient(listOf(tasksBorder, tasksBorder.copy(alpha = 0.2f)))
    } else {
        if (item.isCompleted) {
            Brush.linearGradient(listOf(BaseCardBorderDone, BaseCardBorderDone.copy(alpha = 0.12f)))
        } else {
            Brush.linearGradient(listOf(BaseCardBorder, BaseCardBorder.copy(alpha = 0.12f)))
        }
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
        if (!useTasksStyle) {
            Box(
                modifier = Modifier
                    .width(Dimens.AccentBarWidth)
                    .fillMaxHeight()
                    .background(
                        if (item.isCompleted) {
                            Brush.verticalGradient(listOf(SuccessGreen, SuccessGreenDark))
                        } else {
                            Brush.verticalGradient(listOf(BaseAccent, Color(0xFF86C7FF)))
                        }
                    )
            )
        }

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
                        uncheckedColor = if (useTasksStyle) tasksTextMuted else BaseTextMuted,
                        checkmarkColor = if (useTasksStyle) tasksTextPrimary else BaseTextPrimary
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
                        highlightColor = if (useTasksStyle) tasksAccent else BaseAccent,
                        baseColor = if (item.isCompleted) {
                            if (useTasksStyle) tasksTextMuted else BaseTextMuted
                        } else {
                            if (useTasksStyle) tasksTextPrimary else BaseTextPrimary
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = item.text,
                        color = if (item.isCompleted) {
                            if (useTasksStyle) tasksTextMuted else BaseTextMuted
                        } else {
                            if (useTasksStyle) tasksTextPrimary else BaseTextPrimary
                        },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (useTasksStyle) 0.dp else 34.dp),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.AccessTime,
                        contentDescription = "Created at",
                        tint = if (useTasksStyle) tasksTextMuted else BaseTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.dateTime,
                        color = if (useTasksStyle) tasksTextMuted else BaseTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Text(
                    text = "\u2022",
                    color = if (useTasksStyle) tasksTextMuted else BaseTextMuted,
                    fontSize = 12.sp
                )

                Text(
                    text = item.duration,
                    color = if (item.isCompleted) {
                        if (useTasksStyle) tasksTextMuted else BaseTextMuted
                    } else {
                        if (useTasksStyle) tasksAccent else BaseAccent
                    },
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
    highlightColor: Color = BaseAccent,
    baseColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val resolvedBaseColor = baseColor ?: if (isCompleted) BaseTextMuted else BaseTextPrimary

    val annotatedString = buildAnnotatedString {
        var startIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()

        while (startIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                withStyle(SpanStyle(color = resolvedBaseColor)) {
                    append(text.substring(startIndex))
                }
                break
            }
            if (matchIndex > startIndex) {
                withStyle(SpanStyle(color = resolvedBaseColor)) {
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
    searchQuery: String = "",
    useTasksStyle: Boolean = false,
    isDarkMode: Boolean = true
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

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> if (useTasksStyle) {
                        if (isDarkMode) Color(0xFF5C7DFF) else Color(0xFF6F96D8)
                    } else ErrorRed
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
            searchQuery = searchQuery,
            useTasksStyle = useTasksStyle,
            isDarkMode = isDarkMode
        )
    }
}
