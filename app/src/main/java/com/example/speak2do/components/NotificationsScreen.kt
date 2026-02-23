package com.example.speak2do.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.data.NotificationHistoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    notifications: List<NotificationHistoryEntity>,
    unreadCount: Int,
    isDarkMode: Boolean,
    onMarkRead: (Long) -> Unit,
    onMarkAllRead: () -> Unit,
    onDelete: (Long) -> Unit,
    onClearAll: () -> Unit,
    onOpenTask: (Long) -> Unit
) {
    val bg = if (isDarkMode) Color(0xFF0A1020) else Color(0xFFF4F8FF)
    val panel = if (isDarkMode) Color(0x1AFFFFFF) else Color(0xCCFFFFFF)
    val panelBorder = if (isDarkMode) Color(0x33FFFFFF) else Color(0x4D9BC3FF)
    val card = if (isDarkMode) Color(0xFF151E34) else Color(0xFFEAF1FF)
    val primary = if (isDarkMode) Color.White else Color(0xFF0F2744)
    val secondary = if (isDarkMode) Color(0xFFB6C5E5) else Color(0xFF5C7391)
    val unreadDot = if (isDarkMode) Color(0xFF67D7FF) else Color(0xFF2E77D0)
    val unreadBadge = if (isDarkMode) Color(0xFF2D6FA4) else Color(0xFF2E77D0)
    val danger = if (isDarkMode) Color(0xFFFF8F8F) else Color(0xFFC73636)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = panel)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, panelBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Notifications",
                            color = primary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Track reminders and open related tasks quickly",
                            color = secondary,
                            fontSize = 12.sp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (unreadCount > 0) unreadBadge else secondary.copy(alpha = 0.14f)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color.White.copy(alpha = 0.95f))
                            )
                        }
                        Text(
                            text = if (unreadCount > 0) "$unreadCount unread" else "All read",
                            color = if (unreadCount > 0) Color.White else secondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onMarkAllRead,
                        enabled = notifications.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mark all read")
                    }
                    TextButton(
                        onClick = onClearAll,
                        enabled = notifications.isNotEmpty(),
                        colors = ButtonDefaults.textButtonColors(contentColor = danger),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear all")
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = "No notifications",
                    tint = secondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(42.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text("No notifications yet", color = secondary, fontSize = 14.sp)
            }
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(top = 14.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications, key = { it.id }) { item ->
                val cardScale by animateFloatAsState(
                    targetValue = if (item.isRead) 1f else 1.01f,
                    animationSpec = tween(220),
                    label = "notificationCardScale${item.id}"
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(cardScale)
                        .clickable {
                            if (!item.isRead) onMarkRead(item.id)
                            onOpenTask(item.taskId)
                        },
                    colors = CardDefaults.cardColors(containerColor = card),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AnimatedVisibility(
                                visible = !item.isRead,
                                enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                                exit = fadeOut(tween(150)) + scaleOut(tween(150))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(9.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(unreadDot)
                                )
                            }
                            if (item.isRead) Spacer(Modifier.size(9.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = primary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.body,
                                    color = secondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = "Due time",
                                        tint = secondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Due ${formatTime(item.dueAtMillis)}",
                                        color = secondary.copy(alpha = 0.95f),
                                        fontSize = 11.sp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = if (item.isRead) "Read" else "New",
                                        color = if (item.isRead) secondary else unreadBadge,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { onDelete(item.id) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = danger
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete notification"
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    return SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(millis))
}
