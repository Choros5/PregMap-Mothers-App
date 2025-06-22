package com.elvis.pregmap.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elvis.pregmap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneSignUpScreen(navController: NavController) {
    val phoneSignUpNavController = rememberNavController()
    val viewModel = remember { PhoneSignUpViewModel() }

    // Handle successful verification
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) {
            // Navigate to main screen after successful verification
            navController.navigate("main") {
                popUpTo("phone_signup") { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFE3F2FD),
        topBar = {
            TopAppBar(
                title = { Text("Phone Sign Up") },
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
        NavHost(
            navController = phoneSignUpNavController,
            startDestination = "name",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("name") {
                PhoneNameStep(phoneSignUpNavController, viewModel)
            }
            composable("phone_number") {
                PhoneNumberStep(phoneSignUpNavController, viewModel)
            }
            composable("phone_password") {
                PhonePasswordStep(phoneSignUpNavController, viewModel)
            }
            composable("phone_verification") {
                PhoneVerificationStep(phoneSignUpNavController, viewModel)
            }
        }
    }
} 