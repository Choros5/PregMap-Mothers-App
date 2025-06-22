package com.elvis.pregmap.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PhoneNameStep(navController: NavController, viewModel: PhoneSignUpViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "Let's get to know you!",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tell us your name to personalize your experience",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF424242)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        AuthTextField(
            value = uiState.firstName, 
            onValueChange = viewModel::onFirstNameChange, 
            label = "First Name (Compulsory)",
            modifier = Modifier.width(280.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = uiState.middleName, 
            onValueChange = viewModel::onMiddleNameChange, 
            label = "Middle Name",
            modifier = Modifier.width(280.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = uiState.surname, 
            onValueChange = viewModel::onSurnameChange, 
            label = "Surname (Compulsory)",
            modifier = Modifier.width(280.dp)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { navController.navigate("phone_number") },
            enabled = uiState.firstName.isNotBlank() && uiState.surname.isNotBlank(),
            modifier = Modifier.width(280.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PhoneNumberStep(navController: NavController, viewModel: PhoneSignUpViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isPhoneValid = uiState.phoneNumber.length >= 9 && uiState.phoneNumber.all { it.isDigit() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "Almost there!",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We'll send you a verification code via SMS",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF424242)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        
        PhoneNumberTextField(
            value = uiState.phoneNumber,
            onValueChange = viewModel::onPhoneNumberChange,
            label = "Phone Number (Compulsory)",
            isError = uiState.phoneNumber.isNotEmpty() && !isPhoneValid,
            errorText = "Please enter a valid phone number.",
            modifier = Modifier.width(280.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { navController.navigate("phone_password") },
            enabled = isPhoneValid,
            modifier = Modifier.width(280.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PhonePasswordStep(navController: NavController, viewModel: PhoneSignUpViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val passwordMetRequirements = uiState.password.length >= 8
    val passwordsMatch = uiState.password == uiState.confirmPassword
    
    // Clear error when step is shown
    LaunchedEffect(Unit) {
        viewModel.clearError()
        viewModel.clearSuccess()
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "Secure your account!",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose a strong password to protect your data",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF424242)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        
        PasswordTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password (Compulsory)",
            modifier = Modifier.width(280.dp)
        )
        Text(
            text = "Password must be at least 8 characters long.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF666666)
            ),
            textAlign = TextAlign.Start,
            modifier = Modifier.width(280.dp).padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        PasswordTextField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = "Confirm Password (Compulsory)",
            isError = uiState.confirmPassword.isNotEmpty() && !passwordsMatch,
            errorText = "Passwords do not match.",
            modifier = Modifier.width(280.dp)
        )
        
        // Error message
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage!!,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFD32F2F)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(280.dp)
            )
        }
        
        // Success message
        if (uiState.successMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.successMessage!!,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF388E3C)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(280.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { 
                if (uiState.isCodeSent) {
                    navController.navigate("phone_verification")
                } else {
                    viewModel.sendVerificationCode()
                }
            },
            enabled = passwordMetRequirements && passwordsMatch && !uiState.isLoading,
            modifier = Modifier.width(280.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    if (uiState.isCodeSent) "Continue to Verification" else "Send Verification Code", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PhoneVerificationStep(navController: NavController, viewModel: PhoneSignUpViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Clear error when step is shown
    LaunchedEffect(Unit) {
        viewModel.clearError()
        viewModel.clearSuccess()
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "Verify your phone!",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter the 6-digit code sent to +254${uiState.phoneNumber}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF424242)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        
        AuthTextField(
            value = uiState.verificationCode,
            onValueChange = viewModel::onVerificationCodeChange,
            label = "Verification Code (6 digits)",
            keyboardType = KeyboardType.Number,
            isError = uiState.verificationCode.isNotEmpty() && uiState.verificationCode.length != 6,
            errorText = "Please enter a 6-digit code",
            modifier = Modifier.width(280.dp)
        )
        
        // Error message
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage!!,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFD32F2F)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(280.dp)
            )
        }
        
        // Success message
        if (uiState.successMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.successMessage!!,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF388E3C)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(280.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { viewModel.verifyCode() },
            enabled = uiState.verificationCode.length == 6 && !uiState.isLoading,
            modifier = Modifier.width(280.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Verify Code", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Resend Code Button with Timer
        if (uiState.canResendCode) {
            Button(
                onClick = { viewModel.resendCode() },
                enabled = !uiState.isLoading,
                modifier = Modifier.width(280.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Resend Code", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            // Show countdown timer
            Card(
                modifier = Modifier.width(280.dp).height(48.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val minutes = uiState.resendTimerSeconds / 60
                    val seconds = uiState.resendTimerSeconds % 60
                    val timerText = if (minutes > 0) {
                        "Resend code in ${minutes}m ${seconds}s"
                    } else {
                        "Resend code in ${seconds}s"
                    }
                    
                    Text(
                        text = timerText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
} 