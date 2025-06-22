package com.elvis.pregmap.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.elvis.pregmap.R

@Composable
fun NameStep(navController: NavController, viewModel: EmailSignUpViewModel) {
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
            onClick = { navController.navigate("email") },
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
fun EmailStep(navController: NavController, viewModel: EmailSignUpViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches()
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
            text = "We'll use this to keep you updated on your journey",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF424242)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        
        AuthTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email Address (Compulsory)",
            keyboardType = KeyboardType.Email,
            isError = uiState.email.isNotEmpty() && !isEmailValid,
            errorText = "Please enter a valid email.",
            modifier = Modifier.width(280.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { navController.navigate("password") },
            enabled = isEmailValid,
            modifier = Modifier.width(280.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PasswordStep(navController: NavController, viewModel: EmailSignUpViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val passwordMetRequirements = uiState.password.length >= 8
    val passwordsMatch = uiState.password == uiState.confirmPassword

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
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { viewModel.createAccount() },
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
                Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage!!,
                color = Color(0xFFD32F2F),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(280.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun VerificationStep(navController: NavController, viewModel: EmailSignUpViewModel) {
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
        
        // Success icon
        Icon(
            painter = painterResource(id = R.drawable.ic_email),
            contentDescription = "Email Sent",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "🎉 Account Created Successfully!",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "We've sent a verification email to:",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF424242)
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = uiState.email,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E8)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📧 Next Steps:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("1. Check your email inbox (and spam folder)")
                Text("2. Click the verification link in the email")
                Text("3. Return here and tap 'Verify Email'")
                
                if (uiState.appCheckEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✅ App Check enabled - Better email deliverability",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { viewModel.checkEmailVerification() },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "✅ Verify Email",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.resendVerificationEmail() },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Text(
                text = "📤 Resend Email",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1976D2)
            )
        }
        
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage!!,
                color = Color(0xFFD32F2F),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SuccessStep(navController: NavController, viewModel: EmailSignUpViewModel) {
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
        
        // Success icon
        Icon(
            painter = painterResource(id = R.drawable.ic_email),
            contentDescription = "Email Verified",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "🎉 Welcome to PregMap!",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your email has been verified successfully!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF424242)
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E8)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "👤 Account Details:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Name: ${uiState.firstName} ${uiState.middleName} ${uiState.surname}")
                Text("Email: ${uiState.email}")
                Text("Status: ✅ Verified")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                navController.navigate("main") {
                    popUpTo("auth_selection") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2)
            )
        ) {
            Text(
                text = "🚀 Start Your Journey",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                viewModel.resetState()
                navController.navigate("auth_selection") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F)
            )
        ) {
            Text(
                text = "🚪 Sign Out",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
} 