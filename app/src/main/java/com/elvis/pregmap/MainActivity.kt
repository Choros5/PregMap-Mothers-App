package com.elvis.pregmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize App Check
        val appCheckHelper = AppCheckHelper(this)
        appCheckHelper.initializeAppCheck()
        
        enableEdgeToEdge()
        setContent {
            PregMapTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") { SplashScreen(navController) }
                    composable("welcome") { WelcomeScreen(navController) }
                    composable("auth_selection") { AuthSelectionScreen(navController, "signup") }
                    composable(
                        "auth_selection/{source}",
                        arguments = listOf(navArgument("source") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val source = backStackEntry.arguments?.getString("source") ?: "signup"
                        AuthSelectionScreen(navController, source)
                    }
                    composable("email_signup") { EmailSignUpScreen(navController) }
                    composable("phone_signup") { PhoneSignUpScreen(navController) }
                    composable("google_signup") { GoogleSignUpScreen(navController) }
                    composable("google_login") { GoogleLoginScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("email_login") { EmailLoginScreen(navController) }
                    composable("phone_login") { PhoneLoginScreen(navController) }
                    composable("main") { MainScreen(navController) }
                }
            }
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
    LaunchedEffect(Unit) {
        systemUiController.setSystemBarsColor(Color(0xFFE3F2FD)) // Light blue
        delay(2000)
        navController.navigate("welcome") {
            popUpTo("splash") { inclusive = true }
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