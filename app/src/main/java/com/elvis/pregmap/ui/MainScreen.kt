package com.elvis.pregmap.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Drawer menu items enum
enum class DrawerMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Home", Icons.Default.Home, "home"),
    SMART_AI("Smart AI Assistant", Icons.Default.Info, "smart_ai"),
    PREGNANCY_TIMELINE("My Pregnancy Timeline", Icons.Default.DateRange, "pregnancy_timeline"),
    CLINIC_VISITS("My Clinic Visits", Icons.Default.Home, "clinic_visits"),
    FIND_ADVICE("Find Advice", Icons.Default.Search, "find_advice"),
    EMERGENCY_TRANSPORT("Emergency Transport", Icons.Default.Info, "emergency_transport"),
    MAMA_COMMUNITY("Mama Community", Icons.Default.Person, "mama_community"),
    MIDWIFERY_DOULAS("Midwifery & Doulas", Icons.Default.Person, "midwifery_doulas")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    
    var selectedMenuItem by remember { mutableStateOf(DrawerMenuItem.HOME) }
    var isEnglish by remember { mutableStateOf(true) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentUser = currentUser,
                selectedMenuItem = selectedMenuItem,
                isEnglish = isEnglish,
                onMenuItemClick = { menuItem ->
                    selectedMenuItem = menuItem
                    scope.launch { drawerState.close() }
                },
                onLanguageToggle = { isEnglish = !isEnglish },
                onProfileClick = {
                    // TODO: Navigate to profile screen
                },
                onSignOut = {
                    auth.signOut()
                    navController.navigate("auth_selection") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Color(0xFFE3F2FD),
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = if (isEnglish) selectedMenuItem.title else getSwahiliTitle(selectedMenuItem),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        ) 
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { 
                                scope.launch { 
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close() 
                                } 
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color(0xFF1976D2),
                        navigationIconContentColor = Color(0xFF1976D2)
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedMenuItem) {
                    DrawerMenuItem.HOME -> HomeScreen()
                    DrawerMenuItem.SMART_AI -> SmartAIScreen()
                    DrawerMenuItem.PREGNANCY_TIMELINE -> PregnancyTimelineScreen()
                    DrawerMenuItem.CLINIC_VISITS -> ClinicVisitsScreen()
                    DrawerMenuItem.FIND_ADVICE -> FindAdviceScreen()
                    DrawerMenuItem.EMERGENCY_TRANSPORT -> EmergencyTransportScreen()
                    DrawerMenuItem.MAMA_COMMUNITY -> MamaCommunityScreen()
                    DrawerMenuItem.MIDWIFERY_DOULAS -> MidwiferyDoulasScreen()
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    currentUser: com.google.firebase.auth.FirebaseUser?,
    selectedMenuItem: DrawerMenuItem,
    isEnglish: Boolean,
    onMenuItemClick: (DrawerMenuItem) -> Unit,
    onLanguageToggle: () -> Unit,
    onProfileClick: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        // User Profile Section
        UserProfileSection(
            currentUser = currentUser,
            onProfileClick = onProfileClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Menu Items
        DrawerMenuItem.values().forEach { menuItem ->
            DrawerMenuItem(
                menuItem = menuItem,
                isSelected = selectedMenuItem == menuItem,
                isEnglish = isEnglish,
                onClick = { onMenuItemClick(menuItem) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Language Toggle
        LanguageToggle(
            isEnglish = isEnglish,
            onToggle = onLanguageToggle
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sign Out Button
        SignOutButton(onSignOut = onSignOut)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Footer
        FooterNote()
    }
}

@Composable
fun UserProfileSection(
    currentUser: com.google.firebase.auth.FirebaseUser?,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF90CAF9)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User Info
            Column {
                Text(
                    text = currentUser?.displayName ?: currentUser?.email ?: "User",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF1976D2)
                )
                Text(
                    text = "Tap to view profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1976D2).copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    menuItem: DrawerMenuItem,
    isSelected: Boolean,
    isEnglish: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF90CAF9)
            else 
                Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = menuItem.icon,
                contentDescription = null,
                tint = if (isSelected) 
                    Color(0xFF1976D2)
                else 
                    Color(0xFF424242),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = if (isEnglish) menuItem.title else getSwahiliTitle(menuItem),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) 
                    Color(0xFF1976D2)
                else 
                    Color(0xFF424242)
            )
        }
    }
}

@Composable
fun LanguageToggle(
    isEnglish: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE1F5FE)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Language",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Language",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isEnglish,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF1976D2),
                    checkedTrackColor = Color(0xFF90CAF9)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEnglish) "EN" else "SW",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
fun SignOutButton(onSignOut: () -> Unit) {
    Button(
        onClick = onSignOut,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD32F2F)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign Out")
    }
}

@Composable
fun FooterNote() {
    Text(
        text = "Made with ❤️ for African mothers",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF424242).copy(alpha = 0.6f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// Helper function to get Swahili titles
fun getSwahiliTitle(menuItem: DrawerMenuItem): String {
    return when (menuItem) {
        DrawerMenuItem.HOME -> "Nyumbani"
        DrawerMenuItem.SMART_AI -> "Msaidizi wa AI"
        DrawerMenuItem.PREGNANCY_TIMELINE -> "Muda wa Mimba Yangu"
        DrawerMenuItem.CLINIC_VISITS -> "Ziara za Kliniki"
        DrawerMenuItem.FIND_ADVICE -> "Tafuta Ushauri"
        DrawerMenuItem.EMERGENCY_TRANSPORT -> "Usafiri wa Dharura"
        DrawerMenuItem.MAMA_COMMUNITY -> "Jumuiya ya Mama"
        DrawerMenuItem.MIDWIFERY_DOULAS -> "Wakunga na Doulas"
    }
}

// Placeholder screens for each menu item
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to PregMap!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your pregnancy journey companion",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SmartAIScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart AI Assistant",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("AI-powered pregnancy guidance coming soon...")
    }
}

@Composable
fun PregnancyTimelineScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Pregnancy Timeline",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Track your pregnancy journey...")
    }
}

@Composable
fun ClinicVisitsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Clinic Visits",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Manage your clinic appointments...")
    }
}

@Composable
fun FindAdviceScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Find Advice",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Get expert pregnancy advice...")
    }
}

@Composable
fun EmergencyTransportScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Emergency Transport",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Quick access to emergency transport...")
    }
}

@Composable
fun MamaCommunityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mama Community",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Connect with other mothers...")
    }
}

@Composable
fun MidwiferyDoulasScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Midwifery & Doulas",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Find qualified midwives and doulas...")
    }
} 