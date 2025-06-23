package com.elvis.pregmap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseUser
import com.elvis.pregmap.ui.screens.PinViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

// Shared Drawer menu items enum
enum class DrawerMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Home", Icons.Default.Home, "main"),
    TWIN_AI("TwinAI", Icons.Default.Info, "twin_ai"),
    PREGNANCY_TIMELINE("My Pregnancy Timeline", Icons.Default.DateRange, "pregnancy_timeline"),
    CLINIC_VISITS("My Clinic Visits", Icons.Default.Home, "clinic_visits"),
    FIND_ADVICE("Find Advice", Icons.Default.Search, "find_advice"),
    EMERGENCY_TRANSPORT("Emergency Transport", Icons.Default.Info, "emergency_transport"),
    MAMA_COMMUNITY("Mama Community", Icons.Default.Person, "mama_community"),
    MIDWIFERY_DOULAS("Midwifery & Doulas", Icons.Default.Person, "midwifery_doulas")
}

@Composable
fun DrawerContent(
    currentUser: FirebaseUser?,
    selectedMenuItem: DrawerMenuItem,
    onMenuItemClick: (DrawerMenuItem) -> Unit,
    onProfileClick: () -> Unit,
    onSignOut: () -> Unit,
    navController: NavController?
) {
    val scope = rememberCoroutineScope()
    val pinViewModel = remember { PinViewModel() }
    val context = LocalContext.current

    // Initialize PIN cache with context
    LaunchedEffect(Unit) {
        pinViewModel.initializePrefs(context)
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color(0xFFE3F2FD))
            .verticalScroll(rememberScrollState())
    ) {
        // UserProfileSection is assumed to be defined elsewhere
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
                        val userId = currentUser?.uid
                        if (userId != null) {
                            if (pinViewModel.hasUserRegistered(userId)) {
                                navController?.navigate("clinic_visits_pin_login")
                            } else {
                                navController?.navigate("clinic_visits_registration")
                            }
                        } else {
                            navController?.navigate("clinic_visits_registration")
                        }
                        onMenuItemClick(item)
                    } else if (item == DrawerMenuItem.TWIN_AI) {
                        navController?.navigate("twin_ai")
                        onMenuItemClick(item)
                    } else {
                        onMenuItemClick(item)
                        navController?.navigate(item.route)
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