package com.example.speak2do.auth

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.example.speak2do.ui.theme.CardBackground
import com.example.speak2do.ui.theme.DarkBackground
import com.example.speak2do.ui.theme.GrayText
import com.example.speak2do.ui.theme.LightCyan
import com.example.speak2do.ui.theme.MutedText
import com.example.speak2do.ui.theme.PrimaryCyan

@Composable
fun NameSetupScreen(
    isLoading: Boolean,
    error: String?,
    onSaveName: (String) -> Unit,
    onClearError: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DarkBackground, CardBackground)
                )
            )
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
                    text = "Set your profile name",
                    style = MaterialTheme.typography.headlineSmall,
                    color = LightCyan
                )
                Text(
                    text = "This name will be shown in your profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onClearError()
                    },
                    label = { Text("Your name", color = MutedText) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
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
                    onClick = { onSaveName(name) },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Continue", color = DarkBackground)
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(color = PrimaryCyan)
                }
            }
        }
    }
}
