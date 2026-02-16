package com.example.speak2do.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
                    text = "Sign in with Phone",
                    style = MaterialTheme.typography.headlineSmall,
                    color = LightCyan
                )
                Text(
                    text = "Enter your 10-digit mobile number",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = phone,
                    onValueChange = {
                        phone = it.filter { ch -> ch.isDigit() }.take(10)
                        onClearError()
                    },
                    label = { Text("Phone number", color = MutedText) },
                    leadingIcon = {
                        Text(
                            text = "+91",
                            color = LightCyan,
                            modifier = Modifier.width(40.dp)
                        )
                    },
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
                    onClick = { onSendOtp(phone) },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Continue", color = DarkBackground)
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (state.isLoading) {
            AuthLoadingOverlay(title = "Sending OTP", subtitle = "Please wait a moment")
        }
    }
}
