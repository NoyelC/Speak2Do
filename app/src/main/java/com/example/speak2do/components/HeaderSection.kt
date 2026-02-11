package com.example.speak2do.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.PrimaryPurple
import com.example.speak2do.ui.theme.SecondaryIndigo
import com.example.speak2do.ui.theme.WhiteText
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HeaderSection() {
    val currentHour = LocalDateTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
    val todayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting, Noyel",
                fontSize = 24.sp,
                color = WhiteText,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = todayDate,
                fontSize = 14.sp,
                color = MutedText
            )
        }

        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    Brush.linearGradient(listOf(PrimaryPurple, SecondaryIndigo)),
                    RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N",
                color = WhiteText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
