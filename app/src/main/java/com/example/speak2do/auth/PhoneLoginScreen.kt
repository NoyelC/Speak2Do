package com.example.speak2do.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onVerifyOtp: (String) -> Unit,
    onClearError: () -> Unit
) {
    var phone by remember {
        mutableStateOf(
            state.phoneNumber.removePrefix("+91").filter { it.isDigit() }.take(10)
        )
    }
    var otp by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DarkBackground,
                        CardBackground
                    )
                )
            )
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PrimaryCyan.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhoneAndroid,
                        contentDescription = "Phone sign in",
                        tint = PrimaryCyan
                    )
                }

                Text(
                    text = "Phone Sign-In",
                    style = MaterialTheme.typography.headlineSmall,
                    color = LightCyan,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = "Use your mobile number to receive OTP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText,
                    modifier = Modifier.padding(top = 4.dp)
                )

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    onClick = { onSendOtp(phone) },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCyan
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (state.isCodeSent) "Resend OTP" else "Send OTP",
                        color = DarkBackground
                    )
                }

                if (state.isCodeSent) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        value = otp,
                        onValueChange = {
                            otp = it
                            onClearError()
                        },
                        label = { Text("Enter OTP", color = MutedText) },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        onClick = { onVerifyOtp(otp) },
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightCyan
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Verify OTP",
                            color = DarkBackground
                        )
                    }
                }

                state.error?.let {
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (state.isLoading) {
                    Spacer(modifier = Modifier.height(14.dp))
                    CircularProgressIndicator(color = PrimaryCyan)
                }
            }
        }
    }
}
