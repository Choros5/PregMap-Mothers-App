package com.elvis.pregmap.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// --- Data Models ---
data class ClinicVisit(
    val id: Int,
    val date: String,
    val trimester: String,
    val status: VisitStatus,
    val facility: FacilityInfo,
    val doctor: String,
    val notes: String,
    val vitals: List<VitalReading>
)

enum class VisitStatus { COMPLETED, UPCOMING }

data class FacilityInfo(
    val name: String,
    val phone: String,
    val mapsUrl: String
)

data class VitalReading(
    val label: String, // e.g., "BP"
    val values: List<Float>, // e.g., [120, 122, 118]
    val unit: String // e.g., "mmHg"
)

// --- Main Screen ---
@Composable
fun ClinicVisitsScreen() {
    val context = LocalContext.current
    var selectedTrimester by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var expandedVisitId by remember { mutableStateOf<Int?>(null) }
    var reminderOptIn by remember { mutableStateOf(false) }

    // Mock data
    val allVisits = remember {
        listOf(
            ClinicVisit(
                id = 1,
                date = "2024-03-10",
                trimester = "1st",
                status = VisitStatus.COMPLETED,
                facility = FacilityInfo(
                    name = "Sunrise Maternity Clinic",
                    phone = "+254712345678",
                    mapsUrl = "https://maps.google.com/?q=Sunrise+Maternity+Clinic"
                ),
                doctor = "Dr. Achieng",
                notes = "Routine checkup. All normal.",
                vitals = listOf(
                    VitalReading("BP", listOf(120f, 122f, 118f, 121f), "mmHg"),
                    VitalReading("Weight", listOf(60f, 61f, 62f, 62.5f), "kg")
                )
            ),
            ClinicVisit(
                id = 2,
                date = "2024-05-15",
                trimester = "2nd",
                status = VisitStatus.UPCOMING,
                facility = FacilityInfo(
                    name = "Sunrise Maternity Clinic",
                    phone = "+254712345678",
                    mapsUrl = "https://maps.google.com/?q=Sunrise+Maternity+Clinic"
                ),
                doctor = "Dr. Achieng",
                notes = "Next scheduled ANC visit.",
                vitals = listOf(
                    VitalReading("BP", listOf(121f, 123f, 120f, 122f), "mmHg"),
                    VitalReading("Weight", listOf(62.5f, 63f, 63.5f, 64f), "kg")
                )
            )
        )
    }

    // Filter logic
    val filteredVisits = allVisits.filter {
        (selectedTrimester == "All" || it.trimester == selectedTrimester) &&
        (selectedStatus == "All" || it.status.name == selectedStatus.uppercase())
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Text(
            text = "My Clinic Visits",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(20.dp)
        )
        // Filters
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                label = "Trimester",
                options = listOf("All", "1st", "2nd", "3rd"),
                selected = selectedTrimester,
                onSelected = { selectedTrimester = it }
            )
            FilterChip(
                label = "Status",
                options = listOf("All", "Completed", "Upcoming"),
                selected = selectedStatus,
                onSelected = { selectedStatus = it }
            )
        }
        // Reminder toggle
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Visit Reminders:", fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = reminderOptIn,
                onCheckedChange = { reminderOptIn = it }
            )
            Spacer(Modifier.width(8.dp))
            Text(if (reminderOptIn) "On" else "Off", fontSize = 14.sp)
        }
        // Visits list
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredVisits) { visit ->
                ClinicVisitCard(
                    visit = visit,
                    expanded = expandedVisitId == visit.id,
                    onExpandToggle = {
                        expandedVisitId = if (expandedVisitId == visit.id) null else visit.id
                    },
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${visit.facility.phone}"))
                        context.startActivity(intent)
                    },
                    onMap = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(visit.facility.mapsUrl))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun FilterChip(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        options.forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .background(
                        if (isSelected) Color(0xFF1976D2) else Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelected(option) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color.White else Color(0xFF1976D2),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ClinicVisitCard(
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(visit.date, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(visit.facility.name, fontSize = 14.sp, color = Color(0xFF1976D2))
                    Text(visit.trimester + " Trimester", fontSize = 13.sp, color = Color.Gray)
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
                Spacer(Modifier.height(10.dp))
                Text("Doctor: ${visit.doctor}", fontWeight = FontWeight.Medium)
                Text("Notes: ${visit.notes}", fontSize = 14.sp)
                Spacer(Modifier.height(10.dp))
                // Vitals Graph
                visit.vitals.forEach { vital ->
                    Text(vital.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    VitalsLineChart(vital)
                }
                Spacer(Modifier.height(10.dp))
                // Facility contact
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onCall, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) {
                        Text("Call", color = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onMap, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))) {
                        Text("Map", color = Color.White)
                    }
                }
            }
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
fun VitalsLineChart(vital: VitalReading) {
    // Simple line chart using Canvas (mock, not to scale)
    val points = vital.values
    val max = points.maxOrNull() ?: 1f
    val min = points.minOrNull() ?: 0f
    val range = (max - min).takeIf { it > 0 } ?: 1f
    val chartHeight = 40.dp
    val chartWidth = 120.dp
    Box(
        modifier = Modifier
            .height(chartHeight)
            .width(chartWidth)
            .background(Color(0xFFF3E5F5), shape = RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
            val stepY = size.height / range
            for (i in 0 until points.size - 1) {
                val x1 = i * stepX
                val y1 = size.height - ((points[i] - min) * stepY)
                val x2 = (i + 1) * stepX
                val y2 = size.height - ((points[i + 1] - min) * stepY)
                drawLine(
                    color = Color(0xFF1976D2),
                    start = androidx.compose.ui.geometry.Offset(x1, y1),
                    end = androidx.compose.ui.geometry.Offset(x2, y2),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
        }
        // Show last value
        Text(
            text = "${points.last().roundToInt()} ${vital.unit}",
            fontSize = 12.sp,
            color = Color(0xFF1976D2),
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
} 