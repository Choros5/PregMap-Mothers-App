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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.elvis.pregmap.ui.screens.HomeScreen
import com.elvis.pregmap.ui.screens.ClinicVisitsScreen
import androidx.compose.ui.tooling.preview.Preview

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
    val currentUser = remember { auth.currentUser }
    val db = remember { Firebase.firestore }
    
    var selectedMenuItem by remember { mutableStateOf(DrawerMenuItem.HOME) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    val scope = rememberCoroutineScope()
    
    // Memoize the navigation items to prevent recreation
    val navigationItems = remember {
        listOf(
            NavigationItem("Home", Icons.Default.Home, 0),
            NavigationItem("AI Assistant", Icons.Default.Info, 1),
            NavigationItem("Community", Icons.Default.Person, 2),
            NavigationItem("Notifications", Icons.Default.Notifications, 3),
            NavigationItem("Timeline", Icons.Default.DateRange, 4)
        )
    }
    
    // Sign out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text("Confirm Sign Out")
            },
            text = {
                Text("Are you sure you want to sign out? You will need to log in again to access your account.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        auth.signOut()
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showSignOutDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF757575)
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color(0xFFE3F2FD)
            ) {
                DrawerContent(
                    currentUser = currentUser,
                    selectedMenuItem = selectedMenuItem,
                    onMenuItemClick = { menuItem ->
                        selectedMenuItem = menuItem
                        scope.launch { drawerState.close() }
                    },
                    onProfileClick = {
                        // TODO: Navigate to profile screen
                    },
                    onSignOut = {
                        showSignOutDialog = true
                    },
                    navController = navController
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color(0xFFE3F2FD),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            PregMapLogo()
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color(0xFF1976D2)
                            )
                        }
                    },
                    actions = {
                        // Spacer to balance the navigation icon and center the title
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.height(60.dp),
                    containerColor = Color(0xFFE3F2FD)
                ) {
                    navigationItems.forEach { item ->
                        NavigationBarItem(
                            selected = selectedTab == item.index,
                            onClick = { selectedTab = item.index },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1976D2),
                                unselectedIconColor = Color(0xFF666666),
                                indicatorColor = Color(0xFFFFFFFF)
                            )
                        )
                    }
                }
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
                    DrawerMenuItem.CLINIC_VISITS -> {
                        // This space is intentionally left blank.
                        // The navigation is handled by the DrawerContent's onMenuItemClick.
                    }
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
fun PregMapLogo() {
    val headlineMedium = MaterialTheme.typography.headlineMedium
    Text(
        text = "PregMap",
        style = TextStyle(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF42A5F5), // Light shiny blue
                    Color(0xFF1976D2)  // Darker shiny blue
                )
            ),
            fontWeight = FontWeight.ExtraBold,
            fontSize = headlineMedium.fontSize,
            fontFamily = headlineMedium.fontFamily,
            fontStyle = headlineMedium.fontStyle,
            letterSpacing = headlineMedium.letterSpacing,
            lineHeight = headlineMedium.lineHeight,
            textAlign = TextAlign.Center
        )
    )
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val index: Int
)

@Composable
fun DrawerContent(
    currentUser: com.google.firebase.auth.FirebaseUser?,
    selectedMenuItem: DrawerMenuItem,
    onMenuItemClick: (DrawerMenuItem) -> Unit,
    onProfileClick: () -> Unit,
    onSignOut: () -> Unit,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val db = remember { Firebase.firestore }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color(0xFFE3F2FD))
            .verticalScroll(rememberScrollState())
    ) {
        UserProfileSection(
            currentUser = currentUser,
            onProfileClick = onProfileClick
        )
        Spacer(modifier = Modifier.height(24.dp))
        DrawerMenuItem.values().forEach { item ->
            NavigationDrawerItem(
                label = { 
                    Text(
                        text = item.title,
                        color = if (item == selectedMenuItem) Color(0xFF1976D2) else Color(0xFF424242),
                        fontWeight = if (item == selectedMenuItem) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                selected = item == selectedMenuItem,
                icon = { 
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.title,
                        tint = if (item == selectedMenuItem) Color(0xFF1976D2) else Color(0xFF666666)
                    ) 
                },
                onClick = {
                    if (item == DrawerMenuItem.CLINIC_VISITS) {
                        // Navigate directly without Firestore call for better performance
                        navController.navigate("clinic_visits_registration")
                        onMenuItemClick(item)
                    } else {
                        onMenuItemClick(item)
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFFE3F2FD),
                    unselectedContainerColor = Color.Transparent,
                    selectedIconColor = Color(0xFF1976D2),
                    unselectedIconColor = Color(0xFF666666),
                    selectedTextColor = Color(0xFF1976D2),
                    unselectedTextColor = Color(0xFF424242)
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        SettingsAndSupport()
        Spacer(modifier = Modifier.height(16.dp))
        SignOutButton(onSignOut = onSignOut)
        Spacer(modifier = Modifier.height(16.dp))
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
fun SettingsAndSupport() {
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
                contentDescription = "Settings & Support",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings & Support",
                style = MaterialTheme.typography.bodyMedium,
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
        Text("Sign Out (Secure)")
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

// Placeholder screens for each menu item
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

@Preview(showBackground = true)
@Composable
private fun MainScreenContent(
    onSignOutClick: () -> Unit,
    selectedMenuItem: DrawerMenuItem,
    onMenuItemClick: (DrawerMenuItem) -> Unit,
    navController: NavController
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    DrawerContent(
        currentUser = currentUser,
        selectedMenuItem = selectedMenuItem,
        onMenuItemClick = onMenuItemClick,
        onProfileClick = { /* Handle profile click */ },
        onSignOut = onSignOutClick,
        navController = navController
    )
} 