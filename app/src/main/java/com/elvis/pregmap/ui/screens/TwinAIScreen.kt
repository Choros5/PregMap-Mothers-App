package com.elvis.pregmap.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import com.elvis.pregmap.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.elvis.pregmap.ui.components.CommonLayout
import com.elvis.pregmap.ui.components.PregMapLogo
import com.elvis.pregmap.ui.components.FooterNote
import androidx.compose.material3.DrawerState
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import com.elvis.pregmap.ui.NavigationItem
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- Data Models ---
data class TwinAIMessage(val text: String, val isUser: Boolean, val role: String? = null)

val twinAIProfiles = listOf(
    "Midwife", "Obstetrician", "Pediatrician", "Doulas", "Gynecologist", "Nurse", "Doctor"
)

val twinAITopicShortcuts = listOf(
    "Nutrition Tips", "Danger Signs", "Mental Wellness", "Is this normal?", "What to expect this week"
)

// --- Main Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwinAIScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    onShowHistory: () -> Unit,
    onNewChat: () -> Unit,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var messages by remember { mutableStateOf(listOf<TwinAIMessage>()) }
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf(twinAIProfiles[0]) }
    var showEmergencyBanner by remember { mutableStateOf(false) }
    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var profileMenuExpanded by remember { mutableStateOf(false) }
    var selectedTopic by remember { mutableStateOf(twinAITopicShortcuts[0]) }
    var topicMenuExpanded by remember { mutableStateOf(false) }

    // --- Emergency detection (simple keyword match for demo) ---
    fun detectEmergency(text: String): Boolean {
        val keywords = listOf("severe bleeding", "can't feel baby", "painful contractions", "fainting", "emergency", "ambulance")
        return keywords.any { text.contains(it, ignoreCase = true) }
    }

    // --- OpenAI API Integration ---
    // NOTE: If you see errors, ensure you have set OPENAI_API_KEY in local.properties and exposed it in build.gradle
    suspend fun getTwinAIResponse(
        prompt: String,
        profile: String,
        context: Context
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENAI_API_KEY
        Log.e("TwinAI", "API Key: $apiKey")
        val client = OkHttpClient()
        // --- Enhanced system prompt for profile specificity ---
        val systemPrompt = when (profile) {
            "Midwife" -> "You are a compassionate Midwife. Give practical, supportive, and culturally relevant advice to a pregnant mother in East Africa. Use simple language and focus on actionable steps."
            "Doulas" -> "You are a caring Doula. Offer emotional support, encouragement, and practical tips to a pregnant mother in East Africa. Be empathetic, use gentle language, and focus on comfort and reassurance."
            else -> "You are a compassionate $profile. Answer as if you are talking to a pregnant mother in East Africa. Be empathetic, accurate, and culturally relevant."
        }
        val requestBody = JSONObject(
            mapOf(
                "model" to "gpt-3.5-turbo",
                "messages" to listOf(
                    mapOf("role" to "system", "content" to systemPrompt),
                    mapOf("role" to "user", "content" to prompt)
                )
            )
        ).toString()
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorMsg = "OpenAI API error: ${response.code} - ${response.message}"
                Log.e("TwinAI", errorMsg)
                throw Exception(errorMsg)
            }
            val body = response.body?.string() ?: throw Exception("Empty response")
            val json = JSONObject(body)
            val aiReply = json
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
            formatAIResponse(aiReply.trim())
        } catch (e: Exception) {
            Log.e("TwinAI", "Exception in getTwinAIResponse", e)
            e.printStackTrace()
            e.message ?: "Sorry, I couldn't get a response from TwinAI. Please try again later."
        }
    }

    // --- Message sending logic ---
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val newMsg = TwinAIMessage(text, isUser = true, role = selectedProfile)
        messages = messages + newMsg
        isLoading = true
        coroutineScope.launch {
            if (detectEmergency(text)) {
                showEmergencyBanner = true
            }
            try {
                val aiReply = getTwinAIResponse(text, selectedProfile, context)
                messages = messages + TwinAIMessage(aiReply, isUser = false, role = selectedProfile)
                errorMessage = null
            } catch (e: Exception) {
                Log.e("TwinAI", "Exception in sendMessage", e)
                errorMessage = e.message ?: "Network issue – please try again later"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFE3F2FD),
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            PregMapLogo()
                        }
                        Text(
                            text = "TwinAI 1.0",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
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
                    IconButton(onClick = onShowHistory) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Past Chats",
                            tint = Color(0xFF1976D2)
                        )
                    }
                    IconButton(onClick = onNewChat) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = Color(0xFF1976D2)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            val navigationItems = listOf(
                NavigationItem("Home", Icons.Default.Home, 0),
                NavigationItem("TwinAI", Icons.Default.Info, 1),
                NavigationItem("Community", Icons.Default.Person, 2),
                NavigationItem("Notifications", Icons.Default.Notifications, 3),
                NavigationItem("Timeline", Icons.Default.DateRange, 4)
            )
            NavigationBar(
                modifier = Modifier.height(60.dp),
                containerColor = Color(0xFFE3F2FD)
            ) {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        selected = item.title == "TwinAI",
                        onClick = {
                            // TODO: Implement navigation for other items
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
                .background(Color(0xFFE3F2FD))
        ) {
            // Header: PregMap logo (like HomeScreen)
            Spacer(modifier = Modifier.height(12.dp))
            // Profile switcher, Quick Topics dropdown, and emergency button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Topics Dropdown
                Box {
                    Button(
                        onClick = { topicMenuExpanded = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(Modifier.width(4.dp))
                        Text(selectedTopic, color = Color(0xFF1976D2))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF1976D2))
                    }
                    DropdownMenu(expanded = topicMenuExpanded, onDismissRequest = { topicMenuExpanded = false }) {
                        twinAITopicShortcuts.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text(topic) },
                                onClick = {
                                    selectedTopic = topic
                                    topicMenuExpanded = false
                                    input = TextFieldValue(topic)
                                }
                            )
                        }
                    }
                }
                Box {
                    Button(
                        onClick = { profileMenuExpanded = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(Modifier.width(4.dp))
                        Text(selectedProfile, color = Color(0xFF1976D2))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF1976D2))
                    }
                    DropdownMenu(expanded = profileMenuExpanded, onDismissRequest = { profileMenuExpanded = false }) {
                        twinAIProfiles.forEach { profile ->
                            DropdownMenuItem(
                                text = { Text(profile) },
                                onClick = {
                                    selectedProfile = profile
                                    profileMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                IconButton(onClick = { showEmergencyBanner = !showEmergencyBanner }) {
                    Icon(Icons.Default.Warning, contentDescription = "Emergency", tint = Color(0xFFD32F2F))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Emergency Banner
            AnimatedVisibility(showEmergencyBanner) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "🚨 Emergency detected!",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "If you are in distress, call your emergency contacts or use the emergency transport section.",
                            color = Color(0xFFB71C1C)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Button(
                                onClick = { /* TODO: Navigate to emergency contacts */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) { Text("View Emergency Contacts", color = Color.White) }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { /* TODO: Navigate to emergency transport */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) { Text("Call Ambulance", color = Color.White) }
                        }
                    }
                }
            }
            // Chat Window
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                LazyColumn(
                    reverseLayout = false,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!msg.isUser) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = msg.role ?: "TwinAI",
                                        color = Color(0xFF1976D2),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                                    )
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF1976D2),
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE3F2FD))
                                                .padding(4.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.widthIn(max = 320.dp)
                                        ) {
                                            Text(
                                                msg.text,
                                                color = Color(0xFF1976D2),
                                                modifier = Modifier.padding(12.dp),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE3F2FD))
                                        .padding(4.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (msg.isUser) Color(0xFF1976D2) else Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.widthIn(max = 320.dp)
                            ) {
                                Text(
                                    msg.text,
                                    color = if (msg.isUser) Color.White else Color(0xFF1976D2),
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            if (msg.isUser) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE3F2FD))
                                        .padding(4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    if (isLoading) {
                        item {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE3F2FD))
                                        .padding(4.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("TwinAI is typing", color = Color(0xFF1976D2))
                                        AnimatedTypingDots()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Message Input Area
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(onClick = { /* TODO: Attach file */ }) {
                    Icon(Icons.Default.Email, contentDescription = "Attach", tint = Color(0xFF1976D2))
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your message…", color = Color(0xFF757575)) },
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Send,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            sendMessage(input.text)
                            input = TextFieldValue("")
                        }
                    ),
                    textStyle = TextStyle(fontSize = 16.sp, color = Color(0xFF212121)), // Visible color
                    trailingIcon = {
                        if (input.text.isNotBlank()) {
                            IconButton(onClick = {
                                sendMessage(input.text)
                                input = TextFieldValue("")
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF1976D2))
                            }
                        }
                    }
                )
            }
            // Error Toast
            if (errorMessage != null) {
                LaunchedEffect(errorMessage) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    errorMessage = null
                }
            }
        }
    }
}

// --- Typing Animation ---
@Composable
fun AnimatedTypingDots() {
    val transition = rememberInfiniteTransition(label = "typing")
    val dot1 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "dot1"
    )
    val dot2 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "dot2"
    )
    val dot3 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "dot3"
    )
    Row(Modifier.padding(start = 8.dp)) {
        Dot(dot1)
        Dot(dot2)
        Dot(dot3)
    }
}

@Composable
fun Dot(alpha: Float) {
    Box(
        Modifier
            .size(8.dp)
            .padding(2.dp)
            .background(Color(0xFF1976D2).copy(alpha = alpha), CircleShape)
    )
}

// --- Format AI response for better readability ---
fun formatAIResponse(response: String): String {
    return response
        .replace("\n\n", "\n\n") // Keep paragraph breaks
        .replace("\n- ", "\n\u2022 ") // Replace dash with bullet
        .replace("\n1. ", "\n1. ") // Numbered lists stay the same
        .replace(Regex("(?<!\n)\n(?!\n)"), "\n") // Single newlines stay
} 