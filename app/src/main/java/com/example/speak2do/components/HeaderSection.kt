package com.example.speak2do.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.R
import com.example.speak2do.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HeaderSection(
    userName: String = "Noyel",
    onAvatarClick: () -> Unit = {}
) {
    val currentHour = LocalDateTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
    val todayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$greeting $userName, $todayDate" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                fontSize = 20.sp,
                color = MutedText,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = userName.ifBlank { "User" },
                fontSize = 24.sp,
                color = WhiteText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = todayDate,
                fontSize = 14.sp,
                color = MutedText
            )
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(PrimaryCyan, LightCyan)),
                    CircleShape
                )
                .clickable { onAvatarClick() }
                .semantics { contentDescription = "Profile avatar for $userName" },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile_avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}
