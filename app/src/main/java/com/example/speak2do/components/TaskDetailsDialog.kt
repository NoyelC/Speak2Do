package com.example.speak2do.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem

@Composable
fun TaskDetailsDialog(
    item: RecordingItem,
    onDismiss: () -> Unit,
    isDarkMode: Boolean = true,
    title: String = "Task Details",
    onMarkComplete: ((RecordingItem) -> Unit)? = null,
    onEditTask: ((RecordingItem, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val parsed = remember(item.id, item.text) { parseTaskText(item.text) }

    var isEditing by remember(item.id) { mutableStateOf(false) }
    var mainText by remember(item.id) { mutableStateOf(parsed.mainText) }
    var subtasks by remember(item.id) { mutableStateOf(parsed.subtasks) }
    var newSubtask by remember(item.id) { mutableStateOf("") }

    val containerColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFF131D31) else androidx.compose.ui.graphics.Color(0xFFF0F6FF)
    val cardColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFF1A2943) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val borderColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0x33A6C6FF) else androidx.compose.ui.graphics.Color(0x336D98D8)
    val primaryTextColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF0E2746)
    val secondaryTextColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFFB7C9E8) else androidx.compose.ui.graphics.Color(0xFF58739A)
    val accentColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFF69D9FF) else androidx.compose.ui.graphics.Color(0xFF2E79D4)

    fun currentTaskText(): String = buildTaskText(mainText.trim(), subtasks)

    val shareText = buildString {
        appendLine(currentTaskText())
        appendLine("Time: ${item.dateTime}")
        appendLine("Category: ${item.duration}")
        append("Status: ${if (item.isCompleted) "Completed" else "Pending"}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (item.isCompleted) "Completed" else "Pending",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardColor, RoundedCornerShape(16.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Actions",
                            color = secondaryTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onEditTask != null) {
                                TopActionButton(
                                    label = if (isEditing) "Save Edit" else "Edit",
                                    icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                    onClick = {
                                        if (isEditing) {
                                            val normalized = mainText.trim()
                                            if (normalized.isNotBlank()) {
                                                mainText = normalized
                                                onEditTask(item, currentTaskText())
                                                isEditing = false
                                            }
                                        } else {
                                            isEditing = true
                                        }
                                    }
                                )
                            }

                            if (onMarkComplete != null && !item.isCompleted) {
                                TopActionButton(
                                    label = "Mark Complete",
                                    icon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null) },
                                    onClick = { onMarkComplete(item) }
                                )
                            }

                            TopActionButton(
                                label = "Copy",
                                icon = { Icon(Icons.Rounded.ContentCopy, contentDescription = null) },
                                onClick = { clipboardManager.setText(AnnotatedString(shareText)) }
                            )

                            TopActionButton(
                                label = "Share",
                                icon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Share task details")
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            )

                            if (isEditing) {
                                TopActionButton(
                                    label = "Cancel",
                                    icon = { Icon(Icons.Rounded.Close, contentDescription = null) },
                                    onClick = {
                                        isEditing = false
                                        mainText = parsed.mainText
                                    }
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardColor, RoundedCornerShape(16.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Task Overview",
                            color = secondaryTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (isEditing) {
                            OutlinedTextField(
                                value = mainText,
                                onValueChange = { mainText = it },
                                label = { Text("Task text") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = mainText.ifBlank { "Untitled task" },
                                color = primaryTextColor,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        InfoRow("Time", item.dateTime, secondaryTextColor, primaryTextColor)
                        InfoRow("Category", item.duration, secondaryTextColor, primaryTextColor)
                    }
                }

                if (onEditTask != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardColor, RoundedCornerShape(16.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sub-tasks",
                                    color = secondaryTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = subtasks.size.toString(),
                                        color = accentColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (subtasks.isEmpty()) {
                                Text(
                                    text = "No sub-tasks yet",
                                    color = secondaryTextColor,
                                    fontSize = 12.sp
                                )
                            }

                            subtasks.forEachIndexed { index, subtask ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = subtask,
                                        color = primaryTextColor,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            subtasks = subtasks.toMutableList().also { it.removeAt(index) }
                                            onEditTask(item, currentTaskText())
                                        },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete sub-task")
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newSubtask,
                                    onValueChange = { newSubtask = it },
                                    label = { Text("Add sub-task") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = {
                                        val normalized = newSubtask.trim()
                                        if (normalized.isNotBlank()) {
                                            subtasks = subtasks + normalized
                                            newSubtask = ""
                                            onEditTask(item, currentTaskText())
                                        }
                                    },
                                    modifier = Modifier.width(92.dp)
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = accentColor)
            }
        },
        containerColor = containerColor
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    labelColor: androidx.compose.ui.graphics.Color,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = labelColor, fontSize = 12.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
@Composable
private fun TopActionButton(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.width(128.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
        }
    }
}

private data class ParsedTaskText(
    val mainText: String,
    val subtasks: List<String>
)

private fun parseTaskText(raw: String): ParsedTaskText {
    val lines = raw.lines()
    val markerIndex = lines.indexOfFirst { it.trim().equals("Subtasks:", ignoreCase = true) }
    if (markerIndex < 0) {
        return ParsedTaskText(mainText = raw.trim(), subtasks = emptyList())
    }
    val main = lines.take(markerIndex).joinToString("\n").trim()
    val subtasks = lines.drop(markerIndex + 1)
        .map { it.trim().removePrefix("-").trim() }
        .filter { it.isNotBlank() }
    return ParsedTaskText(mainText = main, subtasks = subtasks)
}

private fun buildTaskText(mainText: String, subtasks: List<String>): String {
    val normalizedMain = mainText.ifBlank { "Untitled task" }
    if (subtasks.isEmpty()) return normalizedMain

    return buildString {
        appendLine(normalizedMain)
        appendLine()
        appendLine("Subtasks:")
        subtasks.forEach { appendLine("- $it") }
    }.trim()
}
