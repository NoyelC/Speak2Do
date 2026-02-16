package com.example.speak2do.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.*

@Composable
fun ProfileScreen(
    recordings: List<RecordingItem>,
    userName: String = "Noyel",
    onSignOut: () -> Unit = {},
    onUpdateName: (String) -> Unit = {}
) {
    val total = recordings.size
    val completed = recordings.count { it.isCompleted }
    val pending = total - completed

    var showAbout by remember { mutableStateOf(false) }
    var showEditName by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf(userName) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Profile",
                fontSize = 24.sp,
                color = WhiteText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.CardCornerRadius))
                        .background(CardBackground)
                        .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingXl)
                        .semantics { contentDescription = "Profile card for $userName" },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .background(
                                Brush.linearGradient(listOf(PrimaryCyan, LightCyan)),
                                CircleShape
                            )
                            .semantics { contentDescription = "Profile avatar for $userName" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "?",
                            color = WhiteText,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(Dimens.SpacingMd))
                    Text(
                        text = userName.ifBlank { "User" },
                        color = WhiteText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Speak2Do User",
                        color = MutedText,
                        fontSize = 14.sp
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.SmallCornerRadius + 4.dp))
                    .background(CardBackground)
                    .padding(Dimens.SpacingLg)
                    .semantics { contentDescription = "Task summary: $total total, $completed done, $pending pending" },
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItem(value = "$total", label = "Total", color = PrimaryCyan)
                ProfileStatItem(value = "$completed", label = "Done", color = SuccessGreen)
                ProfileStatItem(value = "$pending", label = "Pending", color = WarningOrange)
            }
        }

        item {
            Text(
                text = "Settings",
                fontSize = 18.sp,
                color = WhiteText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.SmallCornerRadius + 4.dp))
                    .background(CardBackground)
            ) {
                SettingsItem(
                    icon = Icons.Rounded.Notifications,
                    label = "Notifications",
                    onClick = {}
                )
                SettingsItem(
                    icon = Icons.Rounded.DarkMode,
                    label = "Appearance",
                    subtitle = "Dark mode",
                    onClick = {}
                )
                SettingsItem(
                    icon = Icons.Rounded.Language,
                    label = "Language",
                    subtitle = "English",
                    onClick = {}
                )
                SettingsItem(
                    icon = Icons.Rounded.Edit,
                    label = "Edit Name",
                    subtitle = userName.ifBlank { "User" },
                    onClick = {
                        draftName = userName
                        showEditName = true
                    }
                )
                SettingsItem(
                    icon = Icons.Rounded.Info,
                    label = "About",
                    showDivider = false,
                    onClick = { showAbout = !showAbout }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = showAbout,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground.copy(alpha = 0.5f))
                            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingMd)
                    ) {
                        Text(
                            text = "Speak2Do",
                            color = WhiteText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Dimens.SpacingXs))
                        Text(
                            text = "Version 1.0.0",
                            color = MutedText,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Voice-powered task manager",
                            color = MutedText,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.SmallCornerRadius + 4.dp))
                    .background(CardBackground)
            ) {
                SettingsItem(
                    icon = Icons.Rounded.Logout,
                    label = "Sign Out",
                    showDivider = false,
                    onClick = onSignOut
                )
            }
        }

        item {
            Spacer(Modifier.height(Dimens.SpacingXl))
        }
    }

    if (showEditName) {
        AlertDialog(
            onDismissRequest = { showEditName = false },
            title = { Text("Edit Profile Name", color = WhiteText) },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = draftName.trim()
                        if (trimmed.isNotEmpty()) {
                            onUpdateName(trimmed)
                            showEditName = false
                        }
                    }
                ) { Text("Save", color = PrimaryCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showEditName = false }) {
                    Text("Cancel", color = MutedText)
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
fun ProfileStatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color = PrimaryCyan
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics { contentDescription = "$label: $value" }
    ) {
        Text(
            text = value,
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = MutedText,
            fontSize = 13.sp
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    showDivider: Boolean = true,
    onClick: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = Dimens.SpacingLg, vertical = 14.dp)
                .semantics { contentDescription = "$label setting${if (subtitle != null) ": $subtitle" else ""}" },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(Dimens.IconSizeMd)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = WhiteText,
                    fontSize = 15.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = MutedText,
                        fontSize = 12.sp
                    )
                }
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = "Open $label",
                tint = MutedText,
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingLg)
                    .height(0.5.dp)
                    .background(MutedText.copy(alpha = 0.2f))
            )
        }
    }
}
