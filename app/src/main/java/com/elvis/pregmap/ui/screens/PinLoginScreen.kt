package com.elvis.pregmap.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elvis.pregmap.ui.components.CommonLayout

@Composable
fun PinLoginScreen(
    onPinVerified: () -> Unit,
    onBackClick: () -> Unit,
    pinViewModel: PinViewModel = viewModel()
) {
    var pin by remember { mutableStateOf("") }
    val pinState by pinViewModel.pinState.collectAsState()
    val context = LocalContext.current
    
    // Initialize PIN cache with context
    LaunchedEffect(Unit) {
        pinViewModel.initializePrefs(context)
    }

    LaunchedEffect(pinState) {
        if (pinState is PinState.Success) {
            onPinVerified()
        }
    }

    CommonLayout(
        title = "Enter PIN",
        showBackButton = true,
        onBackClick = onBackClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Enter Your PIN",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Enter your PIN to view your medical records.",
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6) pin = it },
                label = { Text("PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color(0xFFBDBDBD),
                    focusedTextColor = Color(0xFF1976D2),
                    unfocusedTextColor = Color(0xFF424242),
                    focusedLabelColor = Color(0xFF1976D2),
                    unfocusedLabelColor = Color(0xFF666666)
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (pinState is PinState.Loading) {
                CircularProgressIndicator(color = Color(0xFF1976D2))
            }

            if (pinState is PinState.Error) {
                 Text(
                    text = (pinState as PinState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    pinViewModel.verifyPin(pin)
                },
                enabled = pin.length >= 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                Text("Unlock Records", fontWeight = FontWeight.Bold)
            }
        }
    }
} 