package com.elvis.pregmap.ui.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elvis.pregmap.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSignUpScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: EmailSignUpViewModel = viewModel { EmailSignUpViewModel(context) }
    val stepNavController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    val currentBackStackEntry by stepNavController.currentBackStackEntryAsState()
    val navStep = when (currentBackStackEntry?.destination?.route) {
        "name" -> 1
        "email" -> 2
        "password" -> 3
        "verification" -> 4
        "success" -> 5
        else -> 1
    }
    
    // Use the step from ViewModel if available, otherwise use navigation step
    val progressStep = if (uiState.currentStep > navStep) uiState.currentStep else navStep
    val progress by animateFloatAsState(targetValue = progressStep / 5f, label = "SignUpProgress")

    Scaffold(
        containerColor = Color(0xFFE3F2FD),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Create Account", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Medium
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (stepNavController.previousBackStackEntry != null) {
                            stepNavController.popBackStack()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back), 
                            contentDescription = "Back",
                            tint = Color(0xFF1976D2)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
            NavHost(navController = stepNavController, startDestination = "name", modifier = Modifier.weight(1f)) {
                composable("name") { NameStep(navController = stepNavController, viewModel = viewModel) }
                composable("email") { EmailStep(navController = stepNavController, viewModel = viewModel) }
                composable("password") { PasswordStep(navController = stepNavController, viewModel = viewModel) }
                composable("verification") { VerificationStep(navController = stepNavController, viewModel = viewModel) }
                composable("success") { SuccessStep(navController = stepNavController, viewModel = viewModel) }
            }
        }
    }
    
    // Handle navigation based on ViewModel state
    LaunchedEffect(uiState.currentStep) {
        when (uiState.currentStep) {
            4 -> stepNavController.navigate("verification") {
                popUpTo("password") { inclusive = true }
            }
            5 -> stepNavController.navigate("success") {
                popUpTo("verification") { inclusive = true }
            }
        }
    }
} 