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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var userData by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGoogleUser by remember { mutableStateOf(false) }
    
    // Check user data on screen load
    LaunchedEffect(Unit) {
        try {
            val user = auth.currentUser
            if (user != null) {
                // Check if user exists in Firestore and is a Google user
                val userDoc = firestore.collection("users").document(user.uid).get().await()
                
                if (userDoc.exists()) {
                    val signInMethod = userDoc.getString("signInMethod") ?: ""
                    isGoogleUser = signInMethod == "google"
                    
                    if (isGoogleUser) {
                        // Load user data
                        userData = UserData(
                            firstName = userDoc.getString("firstName") ?: "",
                            middleName = userDoc.getString("middleName") ?: "",
                            lastName = userDoc.getString("lastName") ?: "",
                            fullName = userDoc.getString("fullName") ?: "",
                            email = userDoc.getString("email") ?: "",
                            photoUrl = userDoc.getString("photoUrl") ?: "",
                            googleId = userDoc.getString("googleId") ?: "",
                            createdAt = userDoc.getDate("createdAt") ?: Date(),
                            lastSignInAt = userDoc.getDate("lastSignInAt") ?: Date(),
                            signInMethod = signInMethod
                        )
                    } else {
                        // User exists but not Google user
                        errorMessage = "This account was not created with Google Sign-In. Please use the appropriate sign-in method."
                    }
                } else {
                    // User doesn't exist in Firestore
                    errorMessage = "User account not found. Please sign up first."
                }
            } else {
                // No authenticated user
                errorMessage = "No authenticated user found. Please sign in first."
            }
        } catch (e: Exception) {
            errorMessage = "Error loading user data: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        containerColor = Color(0xFFE3F2FD),
        topBar = {
            TopAppBar(
                title = { Text("Welcome to PregMap") },
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
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF1976D2),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading your account...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1976D2)
                )
            } else if (errorMessage != null) {
                // Error state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Error",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "⚠️ Login Error",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                auth.signOut()
                                navController.navigate("auth_selection") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Text("🔙 Go Back to Sign In")
                        }
                    }
                }
            } else if (isGoogleUser && userData != null) {
                // Success state for Google users
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Sign-In Success",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🎉 You're All Set!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ready to start your maternal journey with PregMap! 🌟",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // User information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "👤 Your Google Account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("👤 Name: ${userData!!.fullName}")
                        Text("📧 Email: ${userData!!.email}")
                        Text("🆔 User ID: ${currentUser?.uid}")
                        Text("🔐 Sign-In Method: Google")
                        Text("📅 Member Since: ${userData!!.createdAt?.toString()?.substring(0, 10) ?: "N/A"}")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Continue to main app button
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
                
                // Sign out button
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                auth.signOut()
                                navController.navigate("auth_selection") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } catch (e: Exception) {
                                // Handle sign out error
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text(
                        text = "🚪 Sign Out",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
} 