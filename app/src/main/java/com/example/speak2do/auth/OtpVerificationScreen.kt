package com.example.speak2do.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
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
fun OtpVerificationScreen(
    state: AuthState,
    onVerifyOtp: (String) -> Unit,
    onResendOtp: (String) -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    val normalizedPhone = remember(state.phoneNumber) {
        state.phoneNumber
            .ifBlank { "+91" }
            .let { raw ->
                val digits = raw.filter { it.isDigit() }
                if (digits.length >= 10) {
                    "+${digits.dropLast(10)} ${digits.takeLast(10).chunked(5).joinToString(" ")}"
                } else raw
            }
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
                .align(Alignment.TopEnd)
                .size(220.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(glowA, Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
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
                        imageVector = Icons.Rounded.Security,
                        contentDescription = "OTP verification",
                        tint = Color(0xFF042029),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Verify OTP",
                    style = MaterialTheme.typography.headlineSmall,
                    color = primaryText
                )
                Text(
                    text = "Code sent to $normalizedPhone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = otp,
                    onValueChange = {
                        otp = it.filter { ch -> ch.isDigit() }.take(6)
                        onClearError()
                    },
                    label = { Text("6-digit OTP", color = secondaryText) },
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
                    onClick = { onVerifyOtp(otp) },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        disabledContainerColor = accentPressed.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Verify & Sign In", color = Color(0xFF022127))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Resend OTP",
                        color = accent,
                        modifier = Modifier.clickable { onResendOtp(state.phoneNumber) }
                    )
                    Text(
                        text = "Change number",
                        color = secondaryText,
                        modifier = Modifier.clickable { onBack() }
                    )
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
            AuthLoadingOverlay(title = "Verifying", subtitle = "Logging you in")
        }
    }
}
