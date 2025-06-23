package com.elvis.pregmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.elvis.pregmap.ui.theme.PregMapTheme
import kotlinx.coroutines.delay
import com.elvis.pregmap.ui.auth.EmailSignUpScreen
import com.elvis.pregmap.ui.auth.PhoneSignUpScreen
import com.elvis.pregmap.ui.auth.GoogleSignUpScreen
import com.elvis.pregmap.ui.auth.GoogleLoginScreen
import com.elvis.pregmap.ui.auth.LoginScreen
import com.elvis.pregmap.ui.auth.EmailLoginScreen
import com.elvis.pregmap.ui.auth.PhoneLoginScreen
import com.elvis.pregmap.ui.MainScreen
import com.elvis.pregmap.ui.auth.AppCheckHelper
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import com.elvis.pregmap.ui.screens.PatientRegistrationScreen
import com.elvis.pregmap.ui.screens.CreatePinScreen
import com.elvis.pregmap.ui.screens.PinLoginScreen
import com.elvis.pregmap.ui.screens.ClinicVisitsScreen
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * MainActivity handles the main navigation and authentication flow of the PregMap app.
 * 
 * Security Features:
 * - Prevents users from logging out by pressing the back button when logged in
 * - Requires double-tap back button to exit the app when on main screen
 * - Shows confirmation dialog for sign out action to prevent accidental logout
 * - Automatically redirects logged-in users to main screen
 * - Prevents navigation to auth screens when already authenticated
 * - Only allows sign out through the dedicated "Sign Out (Secure)" button in the drawer
 */
class MainActivity : ComponentActivity() {
    private var backPressedTime = 0L
    private val BACK_PRESS_INTERVAL = 2000L // 2 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize App Check
        val appCheckHelper = AppCheckHelper(this)
        appCheckHelper.initializeAppCheck()
        
        enableEdgeToEdge()
        setContent {
            MainApp()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PregMapTheme {
        Greeting("Android")
    }
}

@Composable
fun SplashScreen(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    val auth = FirebaseAuth.getInstance()
    
    LaunchedEffect(Unit) {
        systemUiController.setSystemBarsColor(Color(0xFFE3F2FD)) // Light blue
        delay(2000)
        
        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is logged in, navigate to main screen
            navController.navigate("main") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // User is not logged in, navigate to welcome screen
        navController.navigate("welcome") {
            popUpTo("splash") { inclusive = true }
            }
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)),
        color = Color(0xFFE3F2FD)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "PregMap Logo",
                modifier = Modifier.size(160.dp)
            )
        }
    }
}

@Composable
fun WelcomeScreen(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(Color(0xFFE3F2FD))
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)),
        color = Color(0xFFE3F2FD)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "PregMap Logo",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Mapping Better Maternal Journeys",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color(0xFF6D4C41), // Warm brown
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate("auth_selection/signup") },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                ) {
                    Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("auth_selection/login") },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                ) {
                    Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                }
            }
        }
    }
}

@Composable
fun AuthSelectionScreen(navController: NavController, source: String) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(Color(0xFFE3F2FD)) // Match WelcomeScreen background

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)),
        color = Color(0xFFE3F2FD)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Back Button
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color(0xFF6D4C41)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // Logo and Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "PregMap Logo",
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Your Supportive Journey Starts Here",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color(0xFF6D4C41), // Warm brown from WelcomeScreen
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Auth Buttons and Toggle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuthButton(
                        text = "Continue with Google",
                        iconRes = R.drawable.ic_google,
                        onClick = { 
                            if (source == "login") {
                                navController.navigate("google_login")
                            } else {
                                navController.navigate("google_signup")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthButton(
                        text = "Continue with Email",
                        iconRes = R.drawable.ic_email,
                        onClick = { 
                            if (source == "login") {
                                navController.navigate("email_login")
                            } else {
                                navController.navigate("email_signup")
                            }
                        },
                        isPrimary = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthButton(
                        text = "Continue with Phone",
                        iconRes = R.drawable.ic_phone,
                        onClick = { 
                            if (source == "login") {
                                navController.navigate("phone_login")
                            } else {
                                navController.navigate("phone_signup")
                            }
                        },
                        isPrimary = false
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val (text, linkText, newSource) = if (source == "signup") {
                        Triple("Already have an account? ", "Log In", "login")
                    } else {
                        Triple("New to PregMap? ", "Sign Up", "signup")
                    }

                    ClickableText(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                append(text)
                            }
                            withStyle(style = SpanStyle(color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)) {
                                append(linkText)
                            }
                        },
                        onClick = {
                            navController.navigate("auth_selection/$newSource") {
                                popUpTo("auth_selection/$source") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AuthButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    isPrimary: Boolean = true
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) Color.White else Color(0xFF90CAF9)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null, // Decorative
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPrimary) Color(0xFF1976D2) else Color.White
            )
        }
    }
}

@Composable
fun MainApp() {
    val systemUiController = rememberSystemUiController()
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    
    // Global back handler for all screens when user is logged in
    val context = LocalContext.current
    var backPressedTime by remember { mutableStateOf(0L) }
    val BACK_PRESS_INTERVAL = 2000L
    
    // Create a global back press callback
    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if user is logged in
                if (auth.currentUser != null) {
                    // Check if we can pop back stack
                    if (navController.previousBackStackEntry != null) {
                        // There's a previous screen, allow normal back navigation
                        navController.popBackStack()
                    } else {
                        // No previous screen, we're at the root (main screen)
                        // Implement double-tap exit
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - backPressedTime > BACK_PRESS_INTERVAL) {
                            backPressedTime = currentTime
                            Toast.makeText(
                                context,
                                "Press back again to exit",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // User pressed back twice quickly, exit the app
                            (context as? MainActivity)?.finish()
                        }
                    }
                } else {
                    // User is not logged in, allow normal back navigation
                    navController.popBackStack()
                }
            }
        }
    }
    
    // Register the global back callback
    LaunchedEffect(Unit) {
        val activity = context as? MainActivity
        activity?.onBackPressedDispatcher?.addCallback(backCallback)
    }
    
    // Clean up the callback when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            backCallback.remove()
        }
    }

    PregMapTheme {
        systemUiController.setSystemBarsColor(Color(0xFFE3F2FD))
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFE3F2FD)
        ) {
            val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                "main"
            } else {
                "welcome"
            }
            NavHost(navController = navController, startDestination = startDestination) {
                composable("splash") { SplashScreen(navController) }
                composable("welcome") { WelcomeScreen(navController) }
                composable("auth_selection/signup") { AuthSelectionScreen(navController, "signup") }
                composable("auth_selection/login") { AuthSelectionScreen(navController, "login") }
                composable("login") { LoginScreen(navController) }
                composable("google_login") { GoogleLoginScreen(navController) }
                composable("google_signup") { GoogleSignUpScreen(navController) }
                composable("email_login") { EmailLoginScreen(navController) }
                composable("email_signup") { EmailSignUpScreen(navController) }
                composable("phone_login") { PhoneLoginScreen(navController) }
                composable("phone_signup") { PhoneSignUpScreen(navController) }
                composable("main") { MainScreen(navController) }
                composable("clinic_visits_registration") {
                    PatientRegistrationScreen(
                        onRegistrationSuccess = { navController.navigate("clinic_visits_create_pin") },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable("clinic_visits_create_pin") {
                    CreatePinScreen(
                        onPinCreated = { navController.navigate("clinic_visits") },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable("clinic_visits_pin_login") {
                    PinLoginScreen(
                        onPinVerified = { navController.navigate("clinic_visits") },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable("clinic_visits") { 
                    ClinicVisitsScreen(
                        navController = navController
                    ) 
                }
            }
        }
    }
}