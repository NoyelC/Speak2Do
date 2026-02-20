package com.example.speak2do.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PhoneLoginScreen(
    state: AuthState,
    onSendOtp: (String) -> Unit,
    onClearError: () -> Unit
) {
    var phone by remember {
        mutableStateOf(
            state.phoneNumber.removePrefix("+91").filter { it.isDigit() }.take(10)
        )
    }
    val formattedPhonePreview = remember(phone) {
        val clean = phone.filter { it.isDigit() }.take(10)
        if (clean.isEmpty()) "+91 XXXXX XXXXX"
        else "+91 ${clean.padEnd(10, 'X').chunked(5).joinToString(" ")}"
    }

    val bgTop = Color(0xFF04131E)
    val bgMid = Color(0xFF0C2D3F)
    val bgBottom = Color(0xFF133D4B)
    val glowA = Color(0x6637C6B8)
    val glowB = Color(0x4D6ED4E8)
    val cardColor = Color(0xCC071B29)
    val cardBorder = Color(0x4DCDF7FF)
    val primaryText = Color(0xFFF3FDFF)
    val secondaryText = Color(0xFFC0DCE5)
    val accent = Color(0xFF5DE3D1)
    val accentPressed = Color(0xFF3CCAB8)
    val fieldBg = Color(0x1AFFFFFF)
    val fieldBorder = Color(0x66A2E6EE)
    val fieldText = Color(0xFFE8FBFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgMid, bgBottom)))
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(220.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(glowA, Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(280.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(glowB, Color.Transparent)))
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF2CA6A0), Color(0xFF5DE3D1)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhoneAndroid,
                        contentDescription = "Phone",
                        tint = Color(0xFF042029),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Welcome to Speak2Do",
                    style = MaterialTheme.typography.headlineSmall,
                    color = primaryText
                )
                Text(
                    text = "Sign in with your phone and continue managing tasks by voice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = formattedPhonePreview,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = phone,
                    onValueChange = {
                        phone = it.filter { ch -> ch.isDigit() }.take(10)
                        onClearError()
                    },
                    label = { Text("Phone number", color = secondaryText) },
                    leadingIcon = {
                        Text(
                            text = "+91",
                            color = accent,
                            modifier = Modifier.width(40.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = fieldText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = fieldBorder,
                        focusedTextColor = fieldText,
                        unfocusedTextColor = fieldText,
                        focusedLabelColor = accent,
                        unfocusedLabelColor = secondaryText,
                        cursorColor = accent,
                        focusedContainerColor = fieldBg,
                        unfocusedContainerColor = fieldBg
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSendOtp(phone) },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        disabledContainerColor = accentPressed.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Send OTP", color = Color(0xFF022127))
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (state.isLoading) {
            AuthLoadingOverlay(title = "Sending OTP", subtitle = "Please wait a moment")
        }
    }
}
