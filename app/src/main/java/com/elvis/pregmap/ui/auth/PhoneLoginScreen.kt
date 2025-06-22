package com.elvis.pregmap.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.elvis.pregmap.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLoginScreen(navController: NavController) {
    val phoneAuthHelper = remember { PhoneAuthHelper() }
    val scope = rememberCoroutineScope()
    
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPhoneError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    
    fun validatePhoneNumber(): Boolean {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length < 9) {
            isPhoneError = true
            return false
        }
        isPhoneError = false
        return true
    }
    
    fun validatePassword(): Boolean {
        if (password.isEmpty() || password.length < 6) {
            isPasswordError = true
            return false
        }
        isPasswordError = false
        return true
    }
    
    fun handleLogin() {
        if (!validatePhoneNumber() || !validatePassword()) return
        
        isLoading = true
        errorMessage = null
        
        scope.launch {
            try {
                val result = phoneAuthHelper.signInWithPhoneAndPassword(phoneNumber, password)
                
                if (result.isSuccess) {
                    // ✅ Success: Navigate to main screen
                    navController.navigate("main") {
                        popUpTo("auth_selection") { inclusive = true }
                    }
                } else {
                    // ❌ Error: Show error message
                    errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An unexpected error occurred"
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        containerColor = Color(0xFFE3F2FD),
        topBar = {
            TopAppBar(
                title = { Text("Phone Login") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color(0xFF1976D2)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFF1976D2)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Icon(
                painter = painterResource(id = R.drawable.ic_phone),
                contentDescription = "Phone Login",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sign in with your phone number",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF424242)
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Login Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Phone Number Input (without leading 0)
                    PhoneNumberTextField(
                        value = phoneNumber,
                        onValueChange = { 
                            phoneNumber = it
                            if (isPhoneError) isPhoneError = false
                        },
                        label = "Phone Number (without 0)",
                        isError = isPhoneError,
                        errorText = if (isPhoneError) "Please enter a valid phone number without the leading 0" else null
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password Field
                    PasswordTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            if (isPasswordError) isPasswordError = false
                        },
                        label = "Password",
                        isError = isPasswordError,
                        errorText = if (isPasswordError) "Password must be at least 6 characters" else null
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Error Message
                    if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Text(
                                text = "⚠️ $errorMessage",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFD32F2F)
                                ),
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Login Button
                    Button(
                        onClick = { handleLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "🔐 Sign In",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sign Up Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    )
                )
                TextButton(
                    onClick = { navController.navigate("phone_signup") }
                ) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Forgot Password Link
            TextButton(
                onClick = { 
                    // TODO: Implement forgot password functionality
                }
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF1976D2)
                    )
                )
            }
        }
    }
}
