package com.example.speak2do.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.speak2do.ui.theme.CardBackground
import com.example.speak2do.ui.theme.DarkBackground
import com.example.speak2do.ui.theme.GrayText
import com.example.speak2do.ui.theme.LightCyan
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.PrimaryCyan

@Composable
fun OtpVerificationScreen(
    state: AuthState,
    onVerifyOtp: (String) -> Unit,
    onResendOtp: (String) -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    var otp by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, CardBackground)))
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Verify OTP",
                    style = MaterialTheme.typography.headlineSmall,
                    color = LightCyan
                )
                Text(
                    text = "Code sent to ${state.phoneNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = otp,
                    onValueChange = {
                        otp = it.filter { ch -> ch.isDigit() }.take(6)
                        onClearError()
                    },
                    label = { Text("6-digit OTP", color = MutedText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = GrayText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = MutedText,
                        focusedTextColor = GrayText,
                        unfocusedTextColor = GrayText,
                        focusedLabelColor = PrimaryCyan,
                        unfocusedLabelColor = MutedText,
                        cursorColor = PrimaryCyan
                    )
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onVerifyOtp(otp) },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = LightCyan),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Verify & Sign In", color = DarkBackground)
                }

                Text(
                    text = "Resend OTP",
                    color = LightCyan,
                    modifier = Modifier.clickable { onResendOtp(state.phoneNumber) }
                )
                Text(
                    text = "Change number",
                    color = MutedText,
                    modifier = Modifier.clickable { onBack() }
                )

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (state.isLoading) {
            AuthLoadingOverlay(title = "Verifying", subtitle = "Logging you in")
        }
    }
}
