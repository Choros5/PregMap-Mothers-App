package com.elvis.pregmap.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.elvis.pregmap.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleLoginScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAccessDeniedMessage by remember { mutableStateOf(false) }
    var signInResult by remember { mutableStateOf<SignInResult?>(null) }
    
    // Google Sign-In Helper
    val googleSignInHelper = remember { GoogleSignInHelper(context) }
    
    // Google Sign-In Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                scope.launch {
                    try {
                        isLoading = true
                        errorMessage = null
                        showAccessDeniedMessage = false
                        
                        // Sign in with Google using helper (this implements access control)
                        val signInResultValue = googleSignInHelper.signInWithGoogle(account)
                        
                        if (signInResultValue.isSuccess) {
                            signInResult = signInResultValue.getOrNull()
                            isLoading = false
                            
                            when (signInResult) {
                                SignInResult.ExistingUser -> {
                                    // ✅ Access granted - navigate to main app
                                    navController.navigate("main") {
                                        popUpTo("auth_selection") { inclusive = true }
                                    }
                                }
                                SignInResult.AccessDenied -> {
                                    // ❌ Access denied - show message
                                    showAccessDeniedMessage = true
                                }
                                SignInResult.NewUser -> {
                                    // This shouldn't happen during login, but handle it
                                    errorMessage = "Unexpected result: New user during login"
                                }
                                null -> {
                                    errorMessage = "Unexpected null result"
                                }
                            }
                        } else {
                            errorMessage = signInResultValue.exceptionOrNull()?.message ?: "Sign in failed"
                            isLoading = false
                        }
                        
                    } catch (e: Exception) {
                        errorMessage = "Authentication failed: ${e.message}"
                        isLoading = false
                    }
                }
            } catch (e: ApiException) {
                errorMessage = "Google sign in failed: ${e.message}"
                isLoading = false
            }
        } else {
            errorMessage = "Google sign in cancelled"
            isLoading = false
        }
    }
    
    Scaffold(
        containerColor = Color(0xFFE3F2FD),
        topBar = {
            TopAppBar(
                title = { Text("Google Login") },
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
            if (showAccessDeniedMessage) {
                // Access Denied Message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Access Denied",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "⚠️ Access Denied",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This Google account is not registered with PregMap. Please sign up first to create your account.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Sign Up button
                        Button(
                            onClick = {
                                navController.navigate("google_signup") {
                                    popUpTo("google_login") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFFFF9800)
                            ),
                            modifier = Modifier
                                .width(200.dp)
                                .height(48.dp)
                        ) {
                            Text(
                                text = "📝 Sign Up Now",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Try different account button
                        OutlinedButton(
                            onClick = {
                                showAccessDeniedMessage = false
                                scope.launch {
                                    googleSignInHelper.signOutToShowAccountPicker()
                                    launcher.launch(googleSignInHelper.getSignInIntent())
                                }
                            },
                            modifier = Modifier
                                .width(200.dp)
                                .height(48.dp)
                        ) {
                            Text(
                                text = "🔄 Try Different Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Or go back to choose a different sign-in method",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Normal login UI
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Sign in with your Google account to access PregMap",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Google Sign-In Button
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            
                            // Sign out first to ensure account picker is shown
                            googleSignInHelper.signOutToShowAccountPicker()
                            
                            // Launch sign-in with account picker
                            launcher.launch(googleSignInHelper.getSignInIntent())
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .width(280.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLoading) "Signing in..." else "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF666666)
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = {
                        navController.navigate("google_signup") {
                            popUpTo("google_login") { inclusive = true }
                        }
                    }
                ) {
                    Text(
                        text = "Sign up with Google",
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "By signing in, you agree to our Terms of Service and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF666666)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
} 