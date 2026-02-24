package com.example.speak2do.components

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.speak2do.model.RecordingItem
import com.example.speak2do.ui.theme.Dimens

private val ScreenNavy = Color(0xFF050B18)
private val DeepNavy = Color(0xFF001A3D)
private val EmeraldTeal = Color(0xFF00C897)
private val GlassBg = Color(0x1AFFFFFF)
private val GlassBorder = Color(0x44FFFFFF)
private val CardNavy = Color(0xFF0B1A34)
private val LightBackground = Color(0xFFF4F8FF)
private val LightCard = Color(0xFFE7F0FF)

@Composable
fun ProfileScreen(
    recordings: List<RecordingItem>,
    userName: String,
    profileImageUri: Uri?,
    onPickImage: () -> Unit,
    onRemoveProfileImage: () -> Unit,
    onSignOut: () -> Unit,
    onUpdateName: (String) -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onOpenNotifications: () -> Unit = {}
) {
    var showRemovePhotoDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editableName by remember(userName) { mutableStateOf(userName) }

    val screenColor by animateColorAsState(
        targetValue = if (isDarkMode) ScreenNavy else LightBackground,
        animationSpec = tween(260),
        label = "profileScreenColor"
    )
    val settingCardColor by animateColorAsState(
        targetValue = if (isDarkMode) CardNavy else LightCard,
        animationSpec = tween(260),
        label = "profileSettingCard"
    )
    val primaryText by animateColorAsState(
        targetValue = if (isDarkMode) Color.White else Color(0xFF0F2744),
        animationSpec = tween(260),
        label = "profilePrimaryText"
    )
    val secondaryText by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xA6FFFFFF) else Color(0xFF49617D),
        animationSpec = tween(260),
        label = "profileSecondaryText"
    )
    val heroStart by animateColorAsState(
        targetValue = if (isDarkMode) DeepNavy else Color(0xFF1E4D8F),
        animationSpec = tween(260),
        label = "heroStart"
    )
    val heroEnd by animateColorAsState(
        targetValue = if (isDarkMode) EmeraldTeal else Color(0xFF4FD6B4),
        animationSpec = tween(260),
        label = "heroEnd"
    )

    val waveShape = GenericShape { size, _ ->
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height * 0.78f)
        quadraticBezierTo(
            size.width * 0.76f,
            size.height * 1.03f,
            size.width * 0.48f,
            size.height * 0.86f
        )
        quadraticBezierTo(
            size.width * 0.2f,
            size.height * 0.72f,
            0f,
            size.height * 0.84f
        )
        close()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = screenColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(waveShape)
                    .background(
                        Brush.linearGradient(
                            listOf(heroStart, heroEnd)
                        )
                    )
                    .drawWithCache {
                        onDrawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0x66FFFFFF), Color.Transparent)
                                ),
                                radius = size.minDimension * 0.44f,
                                center = androidx.compose.ui.geometry.Offset(
                                    x = size.width * 0.86f,
                                    y = size.height * 0.22f
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0x6644FFD3), Color.Transparent)
                                ),
                                radius = size.minDimension * 0.36f,
                                center = androidx.compose.ui.geometry.Offset(
                                    x = size.width * 0.18f,
                                    y = size.height * 0.12f
                                )
                            )
                        }
                    }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.ScreenPadding,
                    end = Dimens.ScreenPadding,
                    top = 108.dp,
                    bottom = Dimens.ScreenPadding
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
            ) {
                item {
                    ProfileHeroCard(
                        recordings = recordings,
                        userName = userName,
                        profileImageUri = profileImageUri,
                        onOpenPhotoOptions = { showPhotoOptionsDialog = true },
                        onEditName = {
                            editableName = userName
                            showEditNameDialog = true
                        },
                        isDarkMode = isDarkMode,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Tasks",
                            value = recordings.size.toString(),
                            isDarkMode = isDarkMode,
                            primaryText = primaryText,
                            secondaryText = secondaryText
                        )
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Completed",
                            value = recordings.count { it.isCompleted }.toString(),
                            isDarkMode = isDarkMode,
                            primaryText = primaryText,
                            secondaryText = secondaryText
                        )
                    }
                }

                item {
                    SettingContainer(
                        icon = Icons.Rounded.NotificationsActive,
                        iconTint = Color(0xFFFFC107),
                        title = "Notifications",
                        subtitle = "View in-app history",
                        containerColor = settingCardColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        onClick = onOpenNotifications
                    ) {
                        Text("Open", color = primaryText)
                    }
                }

                item {
                    SettingContainer(
                        icon = Icons.Rounded.DarkMode,
                        iconTint = Color(0xFFB388FF),
                        title = "Theme",
                        subtitle = "Light / Dark",
                        containerColor = settingCardColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedContent(
                                targetState = isDarkMode,
                                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                                label = "themeLabel"
                            ) { dark ->
                                Text(
                                    text = if (dark) "Dark" else "Light",
                                    color = primaryText
                                )
                            }
                            Spacer(modifier = Modifier.size(8.dp))
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = onDarkModeChange
                            )
                        }
                    }
                }

                item {
                    SettingContainer(
                        icon = Icons.Rounded.Language,
                        iconTint = Color(0xFF42A5F5),
                        title = "Language",
                        subtitle = "Voice and UI",
                        containerColor = settingCardColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    ) {
                        Text("English", color = primaryText)
                    }
                }

                item {
                    Button(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color(0xFF1C2E4F) else Color(0xFF355D9A),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Rounded.ExitToApp, contentDescription = null)
                        Text(text = "Sign Out", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }

    if (showPhotoOptionsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = {
                Text(
                    "Profile Photo",
                    color = if (isDarkMode) Color.White else Color(0xFF0F2744)
                )
            },
            text = {
                Text(
                    "Choose an action",
                    color = if (isDarkMode) Color(0xA6FFFFFF) else Color(0xFF49617D)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPhotoOptionsDialog = false
                        onPickImage()
                    }
                ) {
                    Text("Set profile photo")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (profileImageUri != null) {
                        TextButton(
                            onClick = {
                                showPhotoOptionsDialog = false
                                showRemovePhotoDialog = true
                            }
                        ) {
                            Text("Remove photo")
                        }
                    }
                    TextButton(onClick = { showPhotoOptionsDialog = false }) {
                        Text("Cancel")
                    }
                }
            },
            containerColor = if (isDarkMode) CardNavy else LightCard
        )
    }

    if (showEditNameDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text(
                    "Edit Name",
                    color = if (isDarkMode) Color.White else Color(0xFF0F2744)
                )
            },
            text = {
                OutlinedTextField(
                    value = editableName,
                    onValueChange = { editableName = it },
                    singleLine = true,
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val next = editableName.trim()
                        if (next.isNotEmpty()) onUpdateName(next)
                        showEditNameDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = if (isDarkMode) CardNavy else LightCard
        )
    }

    if (showRemovePhotoDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRemovePhotoDialog = false },
            title = {
                Text(
                    "Remove Profile Photo",
                    color = if (isDarkMode) Color.White else Color(0xFF0F2744)
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove your profile photo?",
                    color = if (isDarkMode) Color(0xA6FFFFFF) else Color(0xFF49617D)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveProfileImage()
                        showRemovePhotoDialog = false
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemovePhotoDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = if (isDarkMode) CardNavy else LightCard
        )
    }
}

@Composable
private fun ProfileHeroCard(
    recordings: List<RecordingItem>,
    userName: String,
    profileImageUri: Uri?,
    onOpenPhotoOptions: () -> Unit,
    onEditName: () -> Unit,
    isDarkMode: Boolean,
    primaryText: Color,
    secondaryText: Color
) {
    val completedCount = recordings.count { it.isCompleted }
    val pendingCount = recordings.size - completedCount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDarkMode) GlassBg else Color(0xAAFFFFFF))
            .border(
                BorderStroke(1.dp, if (isDarkMode) GlassBorder else Color(0x668DB7FF)),
                RoundedCornerShape(24.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(Color(0x22FFFFFF))
                .clickable(onClick = onOpenPhotoOptions),
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                AsyncImage(
                    model = profileImageUri,
                    contentDescription = "Profile image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = primaryText,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            /*Text(
                text = "Name",
                color = secondaryText,
                style = MaterialTheme.typography.labelMedium
            )*/
            Text(
                text = userName.ifBlank { "User" },
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "${recordings.size} tasks • $pendingCount pending • $completedCount done",
                color = secondaryText,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.size(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onOpenPhotoOptions,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Profile photo",
                        color = if (isDarkMode) Color(0xFF7DD7EC) else Color(0xFF2E77D0),
                    )
                }
                TextButton(
                    onClick = onEditName,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Edit name",
                        color = secondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    isDarkMode: Boolean,
    primaryText: Color,
    secondaryText: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDarkMode) GlassBg else Color(0xAAFFFFFF))
            .border(
                BorderStroke(1.dp, if (isDarkMode) GlassBorder else Color(0x668DB7FF)),
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = title,
            color = secondaryText,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = value,
            color = primaryText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingContainer(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    containerColor: Color,
    primaryText: Color,
    secondaryText: Color,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Column {
                Text(
                    text = title,
                    color = primaryText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = secondaryText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        trailing()
    }
}
