package com.elvis.pregmap.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt
import com.elvis.pregmap.ui.components.CommonLayout
import com.elvis.pregmap.ui.MainScreen
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.BackHandler
import com.elvis.pregmap.ui.DrawerMenuItem
import com.elvis.pregmap.ui.DrawerContent
import com.elvis.pregmap.ui.NavigationItem

// --- Data Models ---
data class ClinicVisit(
    val id: String,
    val visitNumber: String,
    val visitName: String,
    val date: String,
    val trimester: String,
    val status: VisitStatus,
    val facility: FacilityInfo,
    val doctor: String,
    val vitals: VitalsInfo,
    val examination: ExaminationInfo,
    val fetalDetails: FetalDetails,
    val medications: List<String>,
    val dangerSigns: List<String>,
    val tetanusStatus: String,
    val ifasStatus: String
)

enum class VisitStatus { COMPLETED, UPCOMING }

data class FacilityInfo(
    val name: String,
    val phone: String,
    val mapsUrl: String
)

data class VitalsInfo(
    val bloodPressure: String,
    val temperature: String,
    val pulse: String,
    val respiratoryRate: String,
    val maternalWeight: String
)

data class ExaminationInfo(
    val generalAppearance: String,
    val chiefComplaints: String,
    val diagnosis: String,
    val followUpPlan: String,
    val generalObservations: String,
    val breastExamination: String,
    val abdomenExamination: String,
    val pelvicExamination: String,
    val cervicalExamination: String
)

data class FetalDetails(
    val fetalHeartRate: String,
    val fundalHeight: String,
    val fetalPosition: String,
    val fetalMovement: String,
    val fetalHeartSounds: String,
    val fetalLie: String,
    val fetalPresentation: String,
    val fetalEngagement: String,
    val fetalBiometry: String,
    val amnioticFluidLevel: String,
    val placentaLocation: String
)

data class VitalReading(
    val label: String, // e.g., "BP"
    val values: List<Float>, // e.g., [120, 122, 118]
    val unit: String // e.g., "mmHg"
)

// --- ViewModel ---
sealed class ClinicVisitsState {
    object Loading : ClinicVisitsState()
    data class Success(val visits: List<ClinicVisit>) : ClinicVisitsState()
    data class Error(val message: String) : ClinicVisitsState()
}

class ClinicVisitsViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val _state = MutableStateFlow<ClinicVisitsState>(ClinicVisitsState.Loading)
    val state: StateFlow<ClinicVisitsState> = _state

    init {
        loadClinicVisits()
    }

    // Add a method to manually set patient ID for testing
    fun setPatientIdForTesting(patientId: String) {
        PatientDataStore.verifiedPatientId = patientId
        println("🧪 Test: Patient ID manually set in ClinicVisitsViewModel: $patientId")
        loadClinicVisits() // Reload with the new patient ID
    }

    private fun loadClinicVisits() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    println("❌ No user logged in")
                    _state.value = ClinicVisitsState.Error("User not logged in.")
                    return@launch
                }

                // Get patient ID from user's document
                val userDoc = db.collection("users").document(userId).get().await()
                val patientId = userDoc.getString("patientId")
                
                println("🔍 Retrieved patient ID from user document: $patientId")
                
                if (patientId == null) {
                    println("❌ No patient ID found in user document")
                    _state.value = ClinicVisitsState.Error("No patient ID found. Please verify your details first.")
                    return@launch
                }

                println("🔍 Loading ANC visits for patient: $patientId")
                
                val visitsCollection = db.collection("ancVisits")
                val querySnapshot = visitsCollection
                    .whereEqualTo("patientId", patientId)
                    .get()
                    .await()
                
                println("🔍 Found ${querySnapshot.size()} ANC visits")
                
                // Debug: Let's also check what documents exist
                val allVisits = visitsCollection.get().await()
                println("🔍 Total ANC visits in collection: ${allVisits.size()}")
                allVisits.documents.take(3).forEach { doc ->
                    println("🔍 Sample document - ID: ${doc.id}, patientId: ${doc.getString("patientId")}")
                }
                
                val visits = mutableListOf<ClinicVisit>()
                for (document in querySnapshot.documents) {
                    try {
                        // Debug: Print all available fields in this document
                        println("🔍 Document ${document.id} fields:")
                        document.data?.forEach { (key, value) ->
                            println("  $key = $value")
                        }
                        
                        val visit = ClinicVisit(
                            id = document.id,
                            visitNumber = document.get("visitNumber")?.toString() ?: "",
                            visitName = document.getString("name") ?: "Clinic Visit",
                            date = document.getString("visitDate") ?: "",
                            trimester = document.getLong("trimester")?.toString() ?: "",
                            status = when (document.getString("status")?.lowercase()) {
                                "completed" -> VisitStatus.COMPLETED
                                "scheduled" -> VisitStatus.UPCOMING
                                "pending" -> VisitStatus.UPCOMING
                                "upcoming" -> VisitStatus.UPCOMING
                                else -> VisitStatus.COMPLETED
                            },
                            facility = FacilityInfo(
                                name = document.getString("name") ?: "Clinic Visit",
                                phone = "", // Not in the structure, will be empty
                                mapsUrl = "" // Not in the structure, will be empty
                            ),
                            doctor = "", // Not in the structure, will be empty
                            vitals = VitalsInfo(
                                bloodPressure = (document.get("notes") as? Map<String, Any>)?.get("bloodPressure")?.toString() ?: "",
                                temperature = (document.get("notes") as? Map<String, Any>)?.get("temperature")?.toString() ?: "",
                                pulse = (document.get("notes") as? Map<String, Any>)?.get("pulseRate")?.toString() ?: "",
                                respiratoryRate = (document.get("notes") as? Map<String, Any>)?.get("respiratoryRate")?.toString() ?: "",
                                maternalWeight = (document.get("notes") as? Map<String, Any>)?.get("maternalWeight")?.toString() ?: ""
                            ),
                            examination = ExaminationInfo(
                                generalAppearance = (document.get("notes") as? Map<String, Any>)?.get("generalAppearance")?.toString() ?: "",
                                chiefComplaints = (document.get("notes") as? Map<String, Any>)?.get("chiefComplaints")?.toString() ?: "",
                                diagnosis = (document.get("notes") as? Map<String, Any>)?.get("diagnosis")?.toString() ?: "",
                                followUpPlan = (document.get("notes") as? Map<String, Any>)?.get("followUpPlan")?.toString() ?: "",
                                generalObservations = (document.get("notes") as? Map<String, Any>)?.get("generalObservations")?.toString() ?: "",
                                breastExamination = (document.get("notes") as? Map<String, Any>)?.get("breastExamination")?.toString() ?: "",
                                abdomenExamination = (document.get("notes") as? Map<String, Any>)?.get("abdomenExamination")?.toString() ?: "",
                                pelvicExamination = (document.get("notes") as? Map<String, Any>)?.get("pelvicExamination")?.toString() ?: "",
                                cervicalExamination = (document.get("notes") as? Map<String, Any>)?.get("cervicalExamination")?.toString() ?: ""
                            ),
                            fetalDetails = FetalDetails(
                                fetalHeartRate = (document.get("notes") as? Map<String, Any>)?.get("fetalHeartRate")?.toString() ?: "",
                                fundalHeight = (document.get("notes") as? Map<String, Any>)?.get("fundalHeight")?.toString() ?: "",
                                fetalPosition = (document.get("notes") as? Map<String, Any>)?.get("fetalPosition")?.toString() ?: "",
                                fetalMovement = (document.get("notes") as? Map<String, Any>)?.get("fetalMovement")?.toString() ?: "",
                                fetalHeartSounds = (document.get("notes") as? Map<String, Any>)?.get("fetalHeartSounds")?.toString() ?: "",
                                fetalLie = (document.get("notes") as? Map<String, Any>)?.get("fetalLie")?.toString() ?: "",
                                fetalPresentation = (document.get("notes") as? Map<String, Any>)?.get("fetalPresentation")?.toString() ?: "",
                                fetalEngagement = (document.get("notes") as? Map<String, Any>)?.get("fetalEngagement")?.toString() ?: "",
                                fetalBiometry = (document.get("notes") as? Map<String, Any>)?.get("fetalBiometry")?.toString() ?: "",
                                amnioticFluidLevel = (document.get("notes") as? Map<String, Any>)?.get("amnioticFluidLevel")?.toString() ?: "",
                                placentaLocation = (document.get("notes") as? Map<String, Any>)?.get("placentaLocation")?.toString() ?: ""
                            ),
                            medications = (document.get("notes") as? Map<String, Any>)?.get("medicationPlan")?.toString()?.split(",")?.map { it.trim() } ?: emptyList(),
                            dangerSigns = buildDangerSignsList(document),
                            tetanusStatus = (document.get("notes") as? Map<String, Any>)?.get("tetanusDose")?.toString() ?: "",
                            ifasStatus = (document.get("notes") as? Map<String, Any>)?.get("ifasRefilled")?.toString() ?: ""
                        )
                        
                        // Debug: Print the parsed visit data
                        println("🔍 Parsed visit data for ${visit.id}:")
                        println("  Vitals - BP: '${visit.vitals.bloodPressure}', Temp: '${visit.vitals.temperature}', Pulse: '${visit.vitals.pulse}', Weight: '${visit.vitals.maternalWeight}'")
                        println("  Examination - General: '${visit.examination.generalAppearance}', Complaints: '${visit.examination.chiefComplaints}', Observations: '${visit.examination.generalObservations}'")
                        println("  Fetal - HR: '${visit.fetalDetails.fetalHeartRate}', Position: '${visit.fetalDetails.fetalPosition}', Movement: '${visit.fetalDetails.fetalMovement}'")
                        println("  Danger Signs: ${visit.dangerSigns.size} found")
                        println("  Medications: ${visit.medications.size} found")
                        
                        visits.add(visit)
                        println("🔍 Added visit: ${visit.date} - ${visit.facility.name}")
                    } catch (e: Exception) {
                        println("❌ Error parsing visit ${document.id}: ${e.message}")
                    }
                }
                
                if (visits.isEmpty()) {
                    _state.value = ClinicVisitsState.Error("No ANC visits found for this patient.")
                } else {
                    _state.value = ClinicVisitsState.Success(visits)
                }
                
            } catch (e: Exception) {
                println("❌ Error loading ANC visits: ${e.message}")
                _state.value = ClinicVisitsState.Error("Failed to load ANC visits: ${e.message}")
            }
        }
    }
}

// --- Main Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicVisitsScreen(
    viewModel: ClinicVisitsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navController: NavController? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var selectedTrimester by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var expandedVisitId by remember { mutableStateOf<String?>(null) }
    var reminderOptIn by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var selectedMenuItem by remember { mutableStateOf(DrawerMenuItem.CLINIC_VISITS) }
    
    val state by viewModel.state.collectAsState()
    val auth = remember { Firebase.auth }
    val currentUser = remember { auth.currentUser }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pinViewModel = remember { PinViewModel() }
    
    // Initialize PIN cache with context
    LaunchedEffect(Unit) {
        pinViewModel.initializePrefs(context)
    }

    // BackHandler: Go to Home and remove Clinic Visits from stack
    BackHandler {
        navController?.navigate("main") {
            popUpTo("clinic_visits") { inclusive = true }
        }
    }

    // Navigation items to match MainScreen
    val navigationItems = remember {
        listOf(
            NavigationItem("Home", Icons.Default.Home, 0),
            NavigationItem("TwinAI", Icons.Default.Info, 1),
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
                        // Clear PIN cache before signing out
                        pinViewModel.clearCache()
                        auth.signOut()
                        navController?.navigate("welcome") {
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
                        // Back button removed - using system back button instead
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
                            selected = false, // Always false since this is a separate screen
                            onClick = {
                                when (item.index) {
                                    0 -> navController?.navigate("main") {
                                        popUpTo("clinic_visits") { inclusive = true }
                                    }
                                    1 -> navController?.navigate("twin_ai")
                                    2 -> navController?.navigate("mama_community")
                                    3 -> navController?.navigate("notifications")
                                    4 -> navController?.navigate("pregnancy_timeline")
                                }
                            },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE3F2FD),
                                Color(0xFFF3E5F5),
                                Color(0xFFE8F5E8)
                            )
                        )
                    )
            ) {
                // Enhanced Tab Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF1976D2)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { 
                                Text(
                                    "📊 Vitals Trends", 
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { 
                                Text(
                                    "🏥 ANC Visits", 
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
                
                when (state) {
                    is ClinicVisitsState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF1976D2))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Loading your clinic visits...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF666666)
                                    )
                                }
                            }
                        }
                    }
                    is ClinicVisitsState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Error",
                                        modifier = Modifier.size(48.dp),
                                        tint = Color(0xFFD32F2F)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = (state as ClinicVisitsState.Error).message,
                                        color = Color(0xFFD32F2F),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    is ClinicVisitsState.Success -> {
                        val visits = (state as ClinicVisitsState.Success).visits
                        when (selectedTab) {
                            0 -> {
                                // Vitals Trends Tab
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    // Enhanced Header Section (now scrolls with content)
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data("https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=800&h=400&fit=crop")
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "Clinic Visits Background",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(
                                                                    Color.Transparent,
                                                                    Color.Black.copy(alpha = 0.7f)
                                                                )
                                                            )
                                                        )
                                                )
                                                
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(20.dp),
                                                    verticalArrangement = Arrangement.Bottom
                                                ) {
                                                    Text(
                                                        text = "My Clinic Visits",
                                                        style = MaterialTheme.typography.headlineSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    )
                                                    Text(
                                                        text = "Track your pregnancy journey",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            color = Color.White.copy(alpha = 0.9f)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    
                                    item {
                                        VitalsTrendsSection(visits)
                                    }
                                }
                            }
                            1 -> {
                                // ANC Visits Tab
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    // Enhanced Header Section (now scrolls with content)
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data("https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=800&h=400&fit=crop")
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "Clinic Visits Background",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(
                                                                    Color.Transparent,
                                                                    Color.Black.copy(alpha = 0.7f)
                                                                )
                                                            )
                                                        )
                                                )
                                                
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(20.dp),
                                                    verticalArrangement = Arrangement.Bottom
                                                ) {
                                                    Text(
                                                        text = "My Clinic Visits",
                                                        style = MaterialTheme.typography.headlineSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    )
                                                    Text(
                                                        text = "Track your pregnancy journey",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            color = Color.White.copy(alpha = 0.9f)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    
                                    // Enhanced Filter controls
                                    item {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Text(
                                                    "Filter Visits",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1976D2)
                                                    ),
                                                    modifier = Modifier.padding(bottom = 12.dp)
                                                )
                                                var trimesterExpanded by remember { mutableStateOf(false) }
                                                var statusExpanded by remember { mutableStateOf(false) }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    // Trimester filter
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("Trimester", fontSize = 12.sp, color = Color.Gray)
                                                        Box {
                                                            OutlinedButton(
                                                                onClick = { trimesterExpanded = true },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                colors = ButtonDefaults.outlinedButtonColors(
                                                                    containerColor = Color(0xFFF5F5F5)
                                                                )
                                                            ) {
                                                                Text(selectedTrimester, color = Color(0xFF1976D2))
                                                                Icon(
                                                                    Icons.Default.ArrowDropDown,
                                                                    contentDescription = "Expand",
                                                                    tint = Color(0xFF1976D2)
                                                                )
                                                            }
                                                            DropdownMenu(
                                                                expanded = trimesterExpanded,
                                                                onDismissRequest = { trimesterExpanded = false }
                                                            ) {
                                                                listOf("All", "1st", "2nd", "3rd").forEach { trimester ->
                                                                    DropdownMenuItem(
                                                                        text = { Text(trimester) },
                                                                        onClick = { 
                                                                            selectedTrimester = trimester
                                                                            trimesterExpanded = false
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                    // Status filter
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("Status", fontSize = 12.sp, color = Color.Gray)
                                                        Box {
                                                            OutlinedButton(
                                                                onClick = { statusExpanded = true },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                colors = ButtonDefaults.outlinedButtonColors(
                                                                    containerColor = Color(0xFFF5F5F5)
                                                                )
                                                            ) {
                                                                Text(selectedStatus, color = Color(0xFF1976D2))
                                                                Icon(
                                                                    Icons.Default.ArrowDropDown,
                                                                    contentDescription = "Expand",
                                                                    tint = Color(0xFF1976D2)
                                                                )
                                                            }
                                                            DropdownMenu(
                                                                expanded = statusExpanded,
                                                                onDismissRequest = { statusExpanded = false }
                                                            ) {
                                                                listOf("All", "Completed", "Upcoming").forEach { status ->
                                                                    DropdownMenuItem(
                                                                        text = { Text(status) },
                                                                        onClick = { 
                                                                            selectedStatus = status
                                                                            statusExpanded = false
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    // Filter logic
                                    val filteredVisits = visits.filter {
                                        (selectedTrimester == "All" || it.trimester == selectedTrimester) &&
                                        (selectedStatus == "All" || it.status.name == selectedStatus.uppercase())
                                    }
                                    items(filteredVisits) { visit ->
                                        VisitCard(
                                            visit = visit,
                                            expanded = expandedVisitId == visit.id,
                                            onExpandToggle = {
                                                expandedVisitId = if (expandedVisitId == visit.id) null else visit.id
                                            },
                                            onCall = {
                                                // Handle call action
                                            },
                                            onMap = {
                                                // Handle map action
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text("$label: $selected")
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Expand")
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun VisitCard(
    visit: ClinicVisit,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onCall: () -> Unit,
    onMap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onExpandToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with visit info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Visit ${visit.visitNumber}: ${visit.visitName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(visit.date, fontSize = 14.sp, color = Color(0xFF1976D2))
                    Text("${visit.trimester} Trimester", fontSize = 13.sp, color = Color.Gray)
                }
                StatusChip(visit.status)
                IconButton(onClick = onExpandToggle) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            
            if (expanded) {
                Spacer(Modifier.height(16.dp))
                
                // Vitals Section
                VisitSection(
                    title = "Vitals",
                    content = {
                        Column {
                            // Debug logging
                            LaunchedEffect(visit.id) {
                                println("🔍 UI: Rendering vitals for visit ${visit.id}")
                                println("  BP: '${visit.vitals.bloodPressure}', Temp: '${visit.vitals.temperature}', Pulse: '${visit.vitals.pulse}', Weight: '${visit.vitals.maternalWeight}'")
                            }
                            
                            VitalRow("Blood Pressure", visit.vitals.bloodPressure)
                            VitalRow("Temperature", visit.vitals.temperature)
                            VitalRow("Pulse Rate", visit.vitals.pulse)
                            VitalRow("Respiratory Rate", visit.vitals.respiratoryRate)
                            // Add maternal weight if available
                            if (visit.vitals.maternalWeight.isNotEmpty()) {
                                VitalRow("Maternal Weight", "${visit.vitals.maternalWeight} kg")
                            }
                            // Show "No data recorded" if all vitals are empty
                            if (visit.vitals.bloodPressure.isEmpty() && 
                                visit.vitals.temperature.isEmpty() && 
                                visit.vitals.pulse.isEmpty() && 
                                visit.vitals.respiratoryRate.isEmpty() && 
                                visit.vitals.maternalWeight.isEmpty()) {
                                Text("No vitals recorded", fontSize = 14.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                )
                
                // Examination & Observations Section
                VisitSection(
                    title = "Examination & Observations",
                    content = {
                        Column {
                            // Debug logging
                            LaunchedEffect(visit.id) {
                                println("🔍 UI: Rendering examination for visit ${visit.id}")
                                println("  General: '${visit.examination.generalAppearance}', Complaints: '${visit.examination.chiefComplaints}', Observations: '${visit.examination.generalObservations}'")
                            }
                            
                            if (visit.examination.generalAppearance.isNotEmpty()) {
                                Text("General Appearance: ${visit.examination.generalAppearance}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.examination.chiefComplaints.isNotEmpty() && visit.examination.chiefComplaints != "No chief complaint") {
                                Text("Chief Complaints: ${visit.examination.chiefComplaints}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.examination.diagnosis.isNotEmpty()) {
                                Text("Diagnosis: ${visit.examination.diagnosis}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.examination.followUpPlan.isNotEmpty()) {
                                Text("Follow-up Plan: ${visit.examination.followUpPlan}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            
                            // Add additional examination fields from the actual structure
                            if (visit.examination.generalObservations.isNotEmpty()) {
                                Text("General Observations: ${visit.examination.generalObservations}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            
                            if (visit.examination.breastExamination.isNotEmpty()) {
                                Text("Breast Examination: ${visit.examination.breastExamination}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            
                            if (visit.examination.abdomenExamination.isNotEmpty()) {
                                Text("Abdomen Examination: ${visit.examination.abdomenExamination}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            
                            if (visit.examination.pelvicExamination.isNotEmpty()) {
                                Text("Pelvic Examination: ${visit.examination.pelvicExamination}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            
                            if (visit.examination.cervicalExamination.isNotEmpty()) {
                                Text("Cervical Examination: ${visit.examination.cervicalExamination}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            
                            // Show "No data recorded" if all fields are empty
                            if (visit.examination.generalAppearance.isEmpty() && 
                                (visit.examination.chiefComplaints.isEmpty() || visit.examination.chiefComplaints == "No chief complaint") &&
                                visit.examination.diagnosis.isEmpty() && 
                                visit.examination.followUpPlan.isEmpty() &&
                                visit.examination.generalObservations.isEmpty() &&
                                visit.examination.breastExamination.isEmpty() &&
                                visit.examination.abdomenExamination.isEmpty() &&
                                visit.examination.pelvicExamination.isEmpty() &&
                                visit.examination.cervicalExamination.isEmpty()) {
                                Text("No examination data recorded", fontSize = 14.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                )
                
                // Fetal Details Section
                VisitSection(
                    title = "Fetal Details",
                    content = {
                        Column {
                            if (visit.fetalDetails.fetalHeartRate.isNotEmpty()) {
                                Text("Fetal Heart Rate: ${visit.fetalDetails.fetalHeartRate} bpm", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.fetalDetails.fundalHeight.isNotEmpty()) {
                                Text("Fundal Height: ${visit.fetalDetails.fundalHeight} cm", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.fetalDetails.fetalPosition.isNotEmpty()) {
                                Text("Fetal Position: ${visit.fetalDetails.fetalPosition}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.fetalDetails.fetalMovement.isNotEmpty()) {
                                Text("Fetal Movement: ${visit.fetalDetails.fetalMovement}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            
                            // Add additional fetal details from the actual structure
                            if (visit.fetalDetails.fetalHeartSounds.isNotEmpty()) {
                                Text("Fetal Heart Sounds: ${visit.fetalDetails.fetalHeartSounds}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.fetalDetails.fetalLie.isNotEmpty()) {
                                Text("Fetal Lie: ${visit.fetalDetails.fetalLie}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.fetalDetails.fetalPresentation.isNotEmpty()) {
                                Text("Fetal Presentation: ${visit.fetalDetails.fetalPresentation}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.fetalDetails.fetalEngagement.isNotEmpty()) {
                                Text("Fetal Engagement: ${visit.fetalDetails.fetalEngagement}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.fetalDetails.fetalBiometry.isNotEmpty()) {
                                Text("Fetal Biometry: ${visit.fetalDetails.fetalBiometry}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.fetalDetails.amnioticFluidLevel.isNotEmpty()) {
                                Text("Amniotic Fluid Level: ${visit.fetalDetails.amnioticFluidLevel}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.fetalDetails.placentaLocation.isNotEmpty()) {
                                Text("Placenta Location: ${visit.fetalDetails.placentaLocation}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            
                            // Show "No data recorded" if all fields are empty
                            if (visit.fetalDetails.fetalHeartRate.isEmpty() && 
                                visit.fetalDetails.fundalHeight.isEmpty() && 
                                visit.fetalDetails.fetalPosition.isEmpty() && 
                                visit.fetalDetails.fetalMovement.isEmpty() &&
                                visit.fetalDetails.fetalHeartSounds.isEmpty() &&
                                visit.fetalDetails.fetalLie.isEmpty() &&
                                visit.fetalDetails.fetalPresentation.isEmpty() &&
                                visit.fetalDetails.fetalEngagement.isEmpty() &&
                                visit.fetalDetails.fetalBiometry.isEmpty() &&
                                visit.fetalDetails.amnioticFluidLevel.isEmpty() &&
                                visit.fetalDetails.placentaLocation.isEmpty()) {
                                Text("No fetal details recorded", fontSize = 14.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                )
                
                // Medications Section
                VisitSection(
                    title = "Medications",
                    content = {
                        Column {
                            if (visit.medications.isNotEmpty()) {
                                visit.medications.forEach { medication ->
                                    Text("• $medication", fontSize = 14.sp)
                                    Spacer(Modifier.height(2.dp))
                                }
                            } else {
                                Text("No medications prescribed", fontSize = 14.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                )
                
                // Danger Signs Section
                VisitSection(
                    title = "Danger Signs",
                    content = {
                        Column {
                            if (visit.dangerSigns.isNotEmpty()) {
                                visit.dangerSigns.forEach { sign ->
                                    Text("• $sign", fontSize = 14.sp, color = Color.Red)
                                    Spacer(Modifier.height(2.dp))
                                }
                            } else {
                                Text("No danger signs detected", fontSize = 14.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                )
                
                // Tetanus & IFAS Status Section
                VisitSection(
                    title = "Tetanus & IFAS Status",
                    content = {
                        Column {
                            if (visit.tetanusStatus.isNotEmpty()) {
                                Text("Tetanus: ${visit.tetanusStatus}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            if (visit.ifasStatus.isNotEmpty()) {
                                Text("IFAS: ${visit.ifasStatus}", fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                            }
                            // Show "No data recorded" if both fields are empty
                            if (visit.tetanusStatus.isEmpty() && visit.ifasStatus.isEmpty()) {
                                Text("No tetanus/IFAS data recorded", fontSize = 14.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Facility contact buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onCall,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("Call", color = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onMap,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Text("Map", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun VisitSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF1976D2),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

@Composable
fun VitalRow(label: String, value: String) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 14.sp, color = Color(0xFF1976D2))
        }
    }
}

@Composable
fun StatusChip(status: VisitStatus) {
    val color = if (status == VisitStatus.COMPLETED) Color(0xFF43A047) else Color(0xFFFFA000)
    Box(
        modifier = Modifier
            .background(color, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun VitalsTrendsSection(visits: List<ClinicVisit>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFFF3E5F5), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Vitals Trends",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        VitalsTrendChart(
            label = "Blood Pressure (Systolic)",
            values = visits.mapNotNull { it.vitals.bloodPressure.split("/").firstOrNull()?.toFloatOrNull() },
            xLabels = visits.map { it.date.takeLast(5) },
            color = Color(0xFF1976D2)
        )
        Spacer(Modifier.height(12.dp))
        VitalsTrendChart(
            label = "Temperature (°C)",
            values = visits.mapNotNull { it.vitals.temperature.toFloatOrNull() },
            xLabels = visits.map { it.date.takeLast(5) },
            color = Color(0xFFFFA000)
        )
        Spacer(Modifier.height(12.dp))
        VitalsTrendChart(
            label = "Pulse Rate (bpm)",
            values = visits.mapNotNull { it.vitals.pulse.toFloatOrNull() },
            xLabels = visits.map { it.date.takeLast(5) },
            color = Color(0xFF43A047)
        )
        Spacer(Modifier.height(12.dp))
        VitalsTrendChart(
            label = "Respiratory Rate (bpm)",
            values = visits.mapNotNull { it.vitals.respiratoryRate.toFloatOrNull() },
            xLabels = visits.map { it.date.takeLast(5) },
            color = Color(0xFF7B1FA2)
        )
        Spacer(Modifier.height(12.dp))
        VitalsTrendChart(
            label = "Maternal Weight (kg)",
            values = visits.mapNotNull { it.vitals.maternalWeight.toFloatOrNull() },
            xLabels = visits.map { it.date.takeLast(5) },
            color = Color(0xFFB71C1C)
        )
    }
}

@Composable
fun VitalsTrendChart(label: String, values: List<Float>, xLabels: List<String>, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
        if (values.isEmpty()) {
            Text("No data recorded", fontSize = 12.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        } else {
            val max = values.maxOrNull() ?: 1f
            val min = values.minOrNull() ?: 0f
            val range = (max - min).takeIf { it > 0 } ?: 1f
            val chartHeight = 60.dp
            val chartWidth = 220.dp
            Box(
                modifier = Modifier
                    .height(chartHeight)
                    .width(chartWidth)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(6.dp)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val stepX = size.width / (values.size - 1).coerceAtLeast(1)
                    val stepY = size.height / range
                    for (i in 0 until values.size - 1) {
                        val x1 = i * stepX
                        val y1 = size.height - ((values[i] - min) * stepY)
                        val x2 = (i + 1) * stepX
                        val y2 = size.height - ((values[i + 1] - min) * stepY)
                        drawLine(
                            color = color,
                            start = androidx.compose.ui.geometry.Offset(x1, y1),
                            end = androidx.compose.ui.geometry.Offset(x2, y2),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                // Show last value
                Text(
                    text = values.last().roundToInt().toString(),
                    fontSize = 12.sp,
                    color = color,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
            // X-axis labels (dates)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                xLabels.forEachIndexed { i, label ->
                    if (i % (xLabels.size / 4 + 1) == 0 || i == xLabels.lastIndex) {
                        Text(label, fontSize = 10.sp, color = Color.Gray)
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                }
            }
        }
    }
}

// Helper functions to build visit data from ancVisits structure
private fun buildVisitNotes(document: com.google.firebase.firestore.DocumentSnapshot): String {
    val notes = mutableListOf<String>()
    
    // Add key findings
    val bloodPressure = document.getString("bloodPressure")
    if (!bloodPressure.isNullOrEmpty()) notes.add("BP: $bloodPressure")
    
    val pulseRate = document.getString("pulseRate")
    if (!pulseRate.isNullOrEmpty()) notes.add("Pulse: $pulseRate")
    
    val temperature = document.getString("temperature")
    if (!temperature.isNullOrEmpty()) notes.add("Temp: $temperature°C")
    
    val maternalWeight = document.getString("maternalWeight")
    if (!maternalWeight.isNullOrEmpty()) notes.add("Weight: ${maternalWeight}kg")
    
    val fundalHeight = document.getString("fundalHeight")
    if (!fundalHeight.isNullOrEmpty()) notes.add("Fundal Height: ${fundalHeight}cm")
    
    val fetalHeartRate = document.getString("fetalHeartRate")
    if (!fetalHeartRate.isNullOrEmpty()) notes.add("FHR: $fetalHeartRate bpm")
    
    // Add examination findings
    val generalAppearance = document.getString("generalAppearance")
    if (!generalAppearance.isNullOrEmpty()) notes.add("General: $generalAppearance")
    
    val chiefComplaints = document.getString("chiefComplaints")
    if (!chiefComplaints.isNullOrEmpty() && chiefComplaints != "No chief complaint") {
        notes.add("Complaints: $chiefComplaints")
    }
    
    val diagnosis = document.getString("diagnosis")
    if (!diagnosis.isNullOrEmpty()) notes.add("Diagnosis: $diagnosis")
    
    val followUpPlan = document.getString("followUpPlan")
    if (!followUpPlan.isNullOrEmpty()) notes.add("Follow-up: $followUpPlan")
    
    return notes.joinToString("\n")
}

private fun buildVitalReadings(document: com.google.firebase.firestore.DocumentSnapshot): List<String> {
    val vitals = mutableListOf<String>()
    
    // Blood Pressure
    val bloodPressure = document.getString("bloodPressure")
    if (!bloodPressure.isNullOrEmpty()) {
        vitals.add(bloodPressure)
    }
    
    // Pulse Rate
    val pulseRate = document.getString("pulseRate")
    if (!pulseRate.isNullOrEmpty()) {
        vitals.add(pulseRate)
    }
    
    // Temperature
    val temperature = document.getString("temperature")
    if (!temperature.isNullOrEmpty()) {
        vitals.add(temperature)
    }
    
    // Maternal Weight
    val maternalWeight = document.getString("maternalWeight")
    if (!maternalWeight.isNullOrEmpty()) {
        vitals.add(maternalWeight)
    }
    
    // Fundal Height
    val fundalHeight = document.getString("fundalHeight")
    if (!fundalHeight.isNullOrEmpty()) {
        vitals.add(fundalHeight)
    }
    
    // Fetal Heart Rate
    val fetalHeartRate = document.getString("fetalHeartRate")
    if (!fetalHeartRate.isNullOrEmpty()) {
        vitals.add(fetalHeartRate)
    }
    
    return vitals
}

private fun buildDangerSignsList(document: com.google.firebase.firestore.DocumentSnapshot): List<String> {
    val dangerSigns = mutableListOf<String>()
    val notes = document.get("notes") as? Map<String, Any>
    
    // Check for various danger signs in the document
    val severeHeadache = notes?.get("severeHeadache")?.toString()
    if (!severeHeadache.isNullOrEmpty()) {
        dangerSigns.add("Severe Headache: $severeHeadache")
    }
    
    val severeAbdominalPain = notes?.get("severeAbdominalPain")?.toString()
    if (!severeAbdominalPain.isNullOrEmpty()) {
        dangerSigns.add("Severe Abdominal Pain: $severeAbdominalPain")
    }
    
    val vaginalBleeding = notes?.get("vaginalBleeding")?.toString()
    if (!vaginalBleeding.isNullOrEmpty() && vaginalBleeding != "None") {
        dangerSigns.add("Vaginal Bleeding: $vaginalBleeding")
    }
    
    val convulsions = notes?.get("convulsions")?.toString()
    if (!convulsions.isNullOrEmpty()) {
        dangerSigns.add("Convulsions: $convulsions")
    }
    
    val fever = notes?.get("fever")?.toString()
    if (!fever.isNullOrEmpty()) {
        dangerSigns.add("Fever: $fever")
    }
    
    val breakingWater = notes?.get("breakingWater")?.toString()
    if (!breakingWater.isNullOrEmpty()) {
        dangerSigns.add("Breaking Water: $breakingWater")
    }
    
    val noFetalMovement = notes?.get("noFetalMovement")?.toString()
    if (!noFetalMovement.isNullOrEmpty()) {
        dangerSigns.add("No Fetal Movement: $noFetalMovement")
    }
    
    val swellingFaceHands = notes?.get("swellingFaceHands")?.toString()
    if (!swellingFaceHands.isNullOrEmpty()) {
        dangerSigns.add("Swelling Face/Hands: $swellingFaceHands")
    }
    
    // Add additional danger signs from the actual structure
    val oedema = notes?.get("oedema")?.toString()
    if (!oedema.isNullOrEmpty() && oedema != "None") {
        dangerSigns.add("Oedema: $oedema")
    }
    
    val pallor = notes?.get("pallor")?.toString()
    if (!pallor.isNullOrEmpty() && pallor != "None") {
        dangerSigns.add("Pallor: $pallor")
    }
    
    val mentalStatus = notes?.get("mentalStatus")?.toString()
    if (!mentalStatus.isNullOrEmpty() && mentalStatus != "Normal") {
        dangerSigns.add("Mental Status: $mentalStatus")
    }
    
    val heartSounds = notes?.get("heartSounds")?.toString()
    if (!heartSounds.isNullOrEmpty() && heartSounds != "Normal") {
        dangerSigns.add("Heart Sounds: $heartSounds")
    }
    
    val lungSounds = notes?.get("lungSounds")?.toString()
    if (!lungSounds.isNullOrEmpty() && lungSounds != "Normal") {
        dangerSigns.add("Lung Sounds: $lungSounds")
    }
    
    val hydrationStatus = notes?.get("hydrationStatus")?.toString()
    if (!hydrationStatus.isNullOrEmpty() && hydrationStatus != "Normal") {
        dangerSigns.add("Hydration Status: $hydrationStatus")
    }
    
    val nutritionalStatus = notes?.get("nutritionalStatus")?.toString()
    if (!nutritionalStatus.isNullOrEmpty() && nutritionalStatus != "Good") {
        dangerSigns.add("Nutritional Status: $nutritionalStatus")
    }
    
    val organomegaly = notes?.get("organomegaly")?.toString()
    if (!organomegaly.isNullOrEmpty() && organomegaly != "None") {
        dangerSigns.add("Organomegaly: $organomegaly")
    }
    
    return dangerSigns
}

@Composable
fun PregMapLogo() {
    Text(
        text = "PregMap",
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
    )
} 