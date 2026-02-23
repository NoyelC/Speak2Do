package com.example.speak2do.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem

@Composable
fun TaskDetailsDialog(
    item: RecordingItem,
    onDismiss: () -> Unit,
    isDarkMode: Boolean = true,
    title: String = "Task Details"
) {
    val containerColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFF151E34) else androidx.compose.ui.graphics.Color(0xFFEAF1FF)
    val primaryTextColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF0F2744)
    val secondaryTextColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFFB6C5E5) else androidx.compose.ui.graphics.Color(0xFF5C7391)
    val accentColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFF67D7FF) else androidx.compose.ui.graphics.Color(0xFF2E77D0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = primaryTextColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = item.text,
                    color = primaryTextColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Created: ${item.dateTime}",
                    color = secondaryTextColor,
                    fontSize = 13.sp
                )
                Text(
                    text = "Duration: ${item.duration}",
                    color = secondaryTextColor,
                    fontSize = 13.sp
                )
                Text(
                    text = "Status: ${if (item.isCompleted) "Completed" else "Pending"}",
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
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
