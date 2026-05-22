package com.example.ui

import com.example.R
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

// DASHBOARD MODULE (Module B & Module G)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardView(
    currentUser: User?,
    activeAttendance: Attendance?,
    myAttendance: List<Attendance>,
    allAttendance: List<Attendance>,
    allTasks: List<Task>,
    allFileRecords: List<FileRecord>,
    executives: List<User>,
    simulatedLat: Double,
    simulatedLng: Double,
    onTriggerCheckIn: () -> Unit,
    onTriggerCheckOut: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_root"),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 100.dp)
    ) {
        // Welcoming header with dynamic profile avatar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
                                )
                            )
                        )
                ) {
                    val badgeColor = when (currentUser?.role) {
                        "ADMIN" -> Color(0xFFEF4444)
                        "MANAGER" -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                    }
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentUser?.photoUri ?: "ic_default_avatar")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(badgeColor)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Good Day, ${currentUser?.name ?: "Guest"}!",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = currentUser?.role ?: "GUEST",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        color = badgeColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Active Sector Assignments: ${currentUser?.workZoneName ?: "Not Assigned"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Executive Panel Workflow (FR-B1 to FR-B4, Today's checkin summary)
        if (currentUser?.role == "EXECUTIVE") {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Today's Shift Tracker",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeAttendance != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (activeAttendance != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (activeAttendance != null) Color(0xFF10B981).copy(alpha = 0.25f)
                                                else Color(0xFF64748B).copy(alpha = 0.25f)
                                            )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (activeAttendance != null) Color(0xFF10B981) else Color(0xFF64748B))
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (activeAttendance != null) "Shift Active" else "Off Shift (Checked-Out)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (activeAttendance != null) Color(0xFF10B981) else Color(0xFF64748B)
                                )
                            }
                            if (activeAttendance != null) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Started: ${formatTime(activeAttendance.checkInTime)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        if (activeAttendance == null) {
                            Text(
                                text = "Your daily operations shift is verified via coordinate fencing and a high-security visual check. Complete check-in to begin logging duties.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onTriggerCheckIn,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("action_checkin_flow"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Fingerprint, "Checkin")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check-In with Selfie", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Checked in state
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = activeAttendance.checkInSelfieUri,
                                    contentDescription = "Selfie",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Verified Work Status",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = activeAttendance.checkInNote,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (activeAttendance.isOutsideGeofence) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE11D48).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "⚠️ Out of assigned work sector boundaries",
                                                color = Color(0xFFE11D48),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onTriggerCheckOut,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("action_checkout_flow"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Filled.Logout, "Checkout")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check-Out (End of Day Duties)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Executive metrics list
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Shift History Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                
                if (myAttendance.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No shifts logged yet. Complete a checkout to view logs here.",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            items(myAttendance) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date(record.checkInTime)),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (record.isOutsideGeofence) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (record.isOutsideGeofence) "Geofence Exceeded" else "Verified Geofence",
                                    fontSize = 9.sp,
                                    color = if (record.isOutsideGeofence) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = record.checkInSelfieUri,
                                contentDescription = "Selfie verify",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("In: ${formatTime(record.checkInTime)} | Out: ${record.checkOutTime?.let { formatTime(it) } ?: "Working..."}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                record.checkOutNote?.let {
                                    Text("EOD Sum: $it", fontSize = 10.sp, fontStyle = FontStyle.Italic)
                                }
                            }
                        }
                    }
                }
            }
        }

        // MANAGER & ADMIN SUPERVISORY KPI METRICS (Module G Admin Panel)
        if (currentUser?.role == "MANAGER" || currentUser?.role == "ADMIN") {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Operational Performance KPI Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                // Grid layout of 3 KPI panels
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Task SLA Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val totalJobs = allTasks.size
                            val completed = allTasks.count { it.status == "COMPLETED" }
                            val slaVal = if (totalJobs > 0) (completed.toFloat() / totalJobs * 100).toInt() else 100
                            Text("Task SLA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$slaVal%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$completed of $totalJobs Completed", fontSize = 9.sp, color = Color.Gray)
                        }
                    }

                    // Field Attendance Rate Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val activeStaff = allAttendance.count { it.checkOutTime == null }
                            Text("Active Staff", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$activeStaff", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Currently in Field", fontSize = 9.sp, color = Color.Gray)
                        }
                    }

                    // Claims File Queue status
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val pendingApprs = allFileRecords.count { it.status == "PENDING" }
                            Text("Pending Bills", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$pendingApprs", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Claims Approval Req", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Real-time Daily Attendance map/list layout matching FR-B5 geofence checks
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Operational Team Geo-Compliance Status", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Daily map marker grid analysis for out-of-bounds tasks", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // List of Checked-In field employees with status colors
            val activeCheckedIns = allAttendance.filter { it.checkOutTime == null }
            if (activeCheckedIns.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, "Absent", tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("No field employee checked-in yet today.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(activeCheckedIns) { checkItem ->
                    val execProfile = executives.find { it.id == checkItem.employeeId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (checkItem.isOutsideGeofence) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (checkItem.isOutsideGeofence) Color(0xFFEF4444).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Selfie verification indicator
                            AsyncImage(
                                model = checkItem.checkInSelfieUri,
                                contentDescription = "Verification Selfie",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = execProfile?.name ?: "Unknown Executive",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "In: ${formatTime(checkItem.checkInTime)} | Shift Active",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Note: \"${checkItem.checkInNote}\"",
                                    fontSize = 10.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }

                            // Color-coded geo-fencing indicator light
                            Column(horizontalAlignment = Alignment.End) {
                                val statusBgColor = if (checkItem.isOutsideGeofence) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF10B981).copy(alpha = 0.12f)
                                val statusTextColor = if (checkItem.isOutsideGeofence) Color(0xFFEF4444) else Color(0xFF10B981)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusBgColor)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (checkItem.isOutsideGeofence) "OutOfBounds" else "Inside sector",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        color = statusTextColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Accuracy: ±12m",
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// TASKS & VISITS SCHEDULER VIEW (Modules C & D)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasksAndVisitsView(
    currentUser: User?,
    myTasks: List<Task>,
    myVisits: List<Visit>,
    allTasks: List<Task>,
    allVisits: List<Visit>,
    executives: List<User>,
    simulatedLat: Double,
    simulatedLng: Double,
    onViewTaskDetails: (Int) -> Unit,
    onViewVisitDetails: (Int) -> Unit,
    viewModel: FieldForceViewModel
) {
    var jobTabType by remember { mutableStateOf("tasks") } // "tasks" or "visits"
    var showStartVisitDialog by remember { mutableStateOf(false) }
    var visitTextNote by remember { mutableStateOf("") }
    var hasSignedClient by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("jobs_root")
    ) {
        // Mode Selector Tab (Tasks vs Site Meetings)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Button(
                onClick = { jobTabType = "tasks" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (jobTabType == "tasks") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (jobTabType == "tasks") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("jobtype_tasks")
            ) {
                Icon(Icons.Filled.PendingActions, "Tasks")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Assigned Tasks", fontSize = 13.sp)
            }
            Button(
                onClick = { jobTabType = "visits" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (jobTabType == "visits") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (jobTabType == "visits") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp, topStart = 0.dp, bottomStart = 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("jobtype_visits")
            ) {
                Icon(Icons.Filled.EmojiPeople, "Visits")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Customer Visits", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (jobTabType == "tasks") {
            // Task Management lists
            val listToUse = if (currentUser?.role == "EXECUTIVE") myTasks else allTasks
            if (listToUse.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.FactCheck, "Empty Jobs", modifier = Modifier.size(54.dp), tint = Color.Gray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No assigned tasks at this time.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(listToUse) { task ->
                        val execName = executives.find { it.id == task.assignedTo }?.name ?: task.assignedTo
                        Card(
                            onClick = { onViewTaskDetails(task.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (task.priority) {
                                                    "HIGH" -> Color(0xFFFEE2E2)
                                                    "MEDIUM" -> Color(0xFFFEF3C7)
                                                    else -> Color(0xFFE0F2FE)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = task.priority + " Priority",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = when (task.priority) {
                                                "HIGH" -> Color(0xFFEF4444)
                                                "MEDIUM" -> Color(0xFFD97706)
                                                else -> Color(0xFF0284C7)
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.EmojiPeople,
                                            contentDescription = "Assignee",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Agent: $execName",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (task.status) {
                                                    "PENDING" -> Color(0xFF64748B).copy(alpha = 0.12f)
                                                    "IN_PROGRESS" -> Color(0xFF3B82F6).copy(alpha = 0.12f)
                                                    "COMPLETED" -> Color(0xFF10B981).copy(alpha = 0.12f)
                                                    else -> Color(0xFFEF4444).copy(alpha = 0.12f)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = task.status,
                                            color = when (task.status) {
                                                "PENDING" -> Color(0xFF64748B)
                                                "IN_PROGRESS" -> Color(0xFF3B82F6)
                                                "COMPLETED" -> Color(0xFF10B981)
                                                else -> Color(0xFFEF4444)
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Customer Site checking visits lists in timeline
            val activeVisit = myVisits.find { it.checkOutTime == null }
            val visitsHistory = if (currentUser?.role == "EXECUTIVE") myVisits else allVisits

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("visits_lazy_column"),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                if (currentUser?.role == "EXECUTIVE") {
                    if (activeVisit == null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Customer Check-In Verified Site Action Required", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Visiting clients requires real-time geolocated checkin selfie verifying services actioned on premise.", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showStartVisitDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("start_visit_button")
                                    ) {
                                        Icon(Icons.Filled.Directions, "Start Visit")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Launch Customer Check-In")
                                    }
                                }
                            }
                        }
                    } else {
                        // Active customer check-in ongoing! Meet notes & client signatures options
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Green))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Active Visit: ${activeVisit.customerName}", fontWeight = FontWeight.Bold)
                                    }
                                    Text(activeVisit.address, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = visitTextNote,
                                        onValueChange = { visitTextNote = it },
                                        label = { Text("Log Meeting Notes / Solutions Delivered") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Draw Digital Signature panel
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        SignaturePad(
                                            modifier = Modifier.fillMaxSize(),
                                            onPathChanged = { hasSignedClient = true }
                                        )
                                    }
                                    Text("Customer Sign Off Pad", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            viewModel.endVisit(
                                                visit = activeVisit,
                                                notes = visitTextNote.ifEmpty { "Work checklist cleared on customer site successfully." },
                                                signatureBase64 = if (hasSignedClient) "ClientSignatureData" else null,
                                                photoUri = null // Will be captured from camera
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.fillMaxWidth().testTag("end_visit_button")
                                    ) {
                                        Icon(Icons.Filled.CloudUpload, "PDF")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Check Out & Draw PDF Report")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Visit Logs Timeline", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (visitsHistory.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No recorded visits list historical logs available.",
                                modifier = Modifier.padding(16.dp),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(visitsHistory) { item ->
                        Card(
                            onClick = { onViewVisitDetails(item.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (item.checkOutTime != null) Color.Green else Color.Gray)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (item.checkOutTime != null) "Resolved Report" else "Ongoing",
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(item.address, fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row {
                                    Icon(Icons.Filled.Schedule, "Time", modifier = Modifier.size(12.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Duration checkin: ${formatTime(item.checkInTime)} - ${item.checkOutTime?.let { formatTime(it) } ?: "Working"}",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal popup setup for Starting customer visits
    if (showStartVisitDialog) {
        var clientName by remember { mutableStateOf("") }
        var clientAddr by remember { mutableStateOf("") }
        val sampleClients = listOf(
            "St. Mary Hospital" to "St. Mary Hospital, San Francisco",
            "General Depot Store" to "725 Mission St Site, San Francisco",
            "Sutter Retail Mall" to "Market St Retail Branch, SF"
        )

        AlertDialog(
            onDismissRequest = { showStartVisitDialog = false },
            title = { Text("Customer Client Site Visit Check-In") },
            text = {
                Column {
                    Text("Select from assigned schedules:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    sampleClients.forEach { (name, addr) ->
                        Card(
                            onClick = {
                                clientName = name
                                clientAddr = addr
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (clientName == name) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(name, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Or Type New Customer Client Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clientName.isNotEmpty()) {
                            viewModel.startVisit(
                                customerName = clientName,
                                address = clientAddr.ifEmpty { "Custom coordinates waypoint SF" },
                                lat = simulatedLat,
                                lng = simulatedLng,
                                selfieUri = null // Will be captured from camera
                            )
                            showStartVisitDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_start_visit")
                ) {
                    Text("Generate Checked-In Site")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartVisitDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// DOCUMENT & REIMBURSABLE EXPENSES FILING MANAGER VIEW (Module E)
@Composable
fun FileTrackingCenterView(
    currentUser: User?,
    myFiles: List<FileRecord>,
    allFiles: List<FileRecord>,
    onApproveFile: (FileRecord) -> Unit,
    onRejectFile: (FileRecord, String) -> Unit,
    viewModel: FieldForceViewModel
) {
    var searchTagsQuery by remember { mutableStateOf("") }
    var selectCategoryFilter by remember { mutableStateOf("ALL") }
    var reviewFileSelected by remember { mutableStateOf<FileRecord?>(null) }
    var rejectionDetailsMsg by remember { mutableStateOf("") }
    var showRejectionDialog by remember { mutableStateOf(false) }

    val fileRecordsToView = if (currentUser?.role == "EXECUTIVE") myFiles else allFiles
    
    // Filter logical search results
    val filteredFiles = fileRecordsToView.filter { record ->
        (selectCategoryFilter == "ALL" || record.category == selectCategoryFilter) &&
        (searchTagsQuery.isEmpty() || record.tags.contains(searchTagsQuery, ignoreCase = true) || record.fileName.contains(searchTagsQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("files_root")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.FolderOpen, "Files Archive", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Voucher Receipts & POD Center", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Text("Upload and track expenses, incident reports & log sheets", fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))

        // Search Input Fields
        OutlinedTextField(
            value = searchTagsQuery,
            onValueChange = { searchTagsQuery = it },
            label = { Text("Filter by Name, Tags, or Waypoint Reference") },
            leadingIcon = { Icon(Icons.Filled.Search, "Search") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Category Fast Filters horizontal scroll row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "EXPENSE", "POD", "INCIDENT", "TIMESHEET").forEach { cat ->
                FilterChip(
                    selected = selectCategoryFilter == cat,
                    onClick = { selectCategoryFilter = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No files or documents match filter search.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredFiles) { file ->
                    val isPending = file.status == "PENDING"
                    Card(
                        onClick = {
                            if (currentUser?.role == "MANAGER" || currentUser?.role == "ADMIN") {
                                reviewFileSelected = file
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Category Icon visualizer
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (file.category) {
                                            "EXPENSE" -> Color(0xFFFEE2E2)
                                            "POD" -> Color(0xFFD1FAE5)
                                            "INCIDENT" -> Color(0xFFFFE4E6)
                                            else -> Color(0xFFDBEAFE)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when(file.category) {
                                        "EXPENSE" -> Icons.Filled.ReceiptLong
                                        "POD" -> Icons.Filled.DoneOutline
                                        "INCIDENT" -> Icons.Filled.BugReport
                                        else -> Icons.Filled.DateRange
                                    },
                                    contentDescription = file.category,
                                    tint = when(file.category) {
                                        "EXPENSE" -> Color(0xFFEF4444)
                                        "POD" -> Color(0xFF10B981)
                                        "INCIDENT" -> Color(0xFFF43F5E)
                                        else -> Color(0xFF3B82F6)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Metadata descriptions
                            Column(modifier = Modifier.weight(1f)) {
                                Text(file.fileName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("By: ${file.uploadedByName} | ${file.category}", fontSize = 11.sp, color = Color.Gray)
                                    if (file.amount != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(Color.Yellow.copy(alpha = 0.3f)).padding(horizontal = 4.dp)) {
                                            Text("$${file.amount}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        }
                                    }
                                }
                                Text("Tags: #${file.tags}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }

                            // Submissions status badges
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (file.status) {
                                            "APPROVED" -> Color.Green
                                            "REJECTED" -> Color.Red
                                            else -> Color.Gray
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = file.status,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal popup for Manager/Admin to approve or reject document uploads
    reviewFileSelected?.let { file ->
        val photoLink = file.fileUri
        AlertDialog(
            onDismissRequest = { reviewFileSelected = null },
            title = { Text("Audit Request: ${file.fileName}") },
            text = {
                Column {
                    Text("Employee Uploader: ${file.uploadedByName}", fontWeight = FontWeight.Bold)
                    Text("Type voucher category: ${file.category}", fontSize = 12.sp)
                    file.amount?.let {
                        Text("Amount requested: $${it}", fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Simulated Document Image Snapshot Preview:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = file.fileUri,
                            contentDescription = "Invoice",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Metadata Tag References: #${file.tags}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        onClick = {
                            viewModel.deleteFileRecord(file.id)
                            reviewFileSelected = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Filled.Delete, "Delete")
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Delete")
                    }

                    Row {
                        Button(
                            onClick = {
                                showRejectionDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                        ) {
                            Text("Reject")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onApproveFile(file)
                                reviewFileSelected = null
                            },
                            modifier = Modifier.testTag("manager_approve_file")
                        ) {
                            Text("Approve")
                        }
                    }
                }
            }
        )

        // Nested Rejection details modal dialog
        if (showRejectionDialog) {
            AlertDialog(
                onDismissRequest = { showRejectionDialog = false },
                title = { Text("Enter Rejection Feedback Reason") },
                text = {
                    OutlinedTextField(
                        value = rejectionDetailsMsg,
                        onValueChange = { rejectionDetailsMsg = it },
                        label = { Text("Reason for Rejection (e.g. Receipt Unclear)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onRejectFile(file, rejectionDetailsMsg.ifEmpty { "Receipt details missing or unreadable." })
                            showRejectionDialog = false
                            reviewFileSelected = null
                        },
                        modifier = Modifier.testTag("manager_reject_file")
                    ) {
                        Text("Submit Rejection")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectionDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

// SIMULATED INTERACTIVE CITY VECTOR MAP (Module B & Module G)
@Composable
fun InteractiveSimulationMapView(
    currentUser: User?,
    executives: List<User>,
    activeAttendance: Attendance?,
    allAttendance: List<Attendance>,
    allVisits: List<Visit>,
    simulatedLat: Double,
    simulatedLng: Double
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("map_root")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Explore, "Map", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Field Force Live Asset Grid", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Text("Displays real-time geo-locations & active work sector limits", fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))

        // Vector city drawing simulation canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
                    val width = size.width
                    val height = size.height

                    // Draw stylized matrix road grids in elegant light silver lines
                    val gridInters = 80f
                    var i = 0f
                    while (i < width) {
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(i, 0f),
                            end = Offset(i, height),
                            strokeWidth = 3f
                        )
                        i += gridInters
                    }
                    var j = 0f
                    while (j < height) {
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(0f, j),
                            end = Offset(width, j),
                            strokeWidth = 3f
                        )
                        j += gridInters
                    }

                    // Main central highway
                    drawLine(
                        color = Color(0xFFE2E8F0).copy(alpha = 0.8f),
                        start = Offset(width / 2, 0f),
                        end = Offset(width / 2, height),
                        strokeWidth = 14f
                    )
                    drawLine(
                        color = Color(0xFFE2E8F0).copy(alpha = 0.8f),
                        start = Offset(0f, height / 2),
                        end = Offset(width, height / 2),
                        strokeWidth = 14f
                    )

                    // Draw simulated City Park zone green
                    drawRect(
                        color = Color(0xFFDCFCE7),
                        topLeft = Offset(width * 0.15f, height * 0.15f),
                        size = Size(width * 0.25f, height * 0.2f)
                    )

                    val mapAccentGeoColor = primaryColor
                    val mapAccentBgColor = primaryColor.copy(alpha = 0.12f)

                    // Map Sector bounds central circle limit (SF Central representation)
                    drawCircle(
                        color = mapAccentBgColor,
                        radius = minOf(width, height) * 0.35f,
                        center = Offset(width / 2, height / 2)
                    )
                    drawCircle(
                        color = mapAccentGeoColor,
                        radius = minOf(width, height) * 0.35f,
                        center = Offset(width / 2, height / 2),
                        style = Stroke(width = 4f)
                    )

                    // Coordinates representations
                    // Plot clients, checkins
                    // Draw Customer checkin markers yellow/orange
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 16f,
                        center = Offset(width * 0.75f, height * 0.3f)
                    )

                    // Blinking executive pinpoint location tracking red/green
                    val pinX = width / 2 + ((simulatedLat - 37.7725) * width * 10).toFloat()
                    val pinY = height / 2 + ((simulatedLng - (-122.4194)) * height * 5).toFloat()

                    drawCircle(
                        color = mapAccentGeoColor.copy(alpha = 0.4f),
                        radius = 28f,
                        center = Offset(pinX, pinY)
                    )
                    drawCircle(
                        color = mapAccentGeoColor,
                        radius = 10f,
                        center = Offset(pinX, pinY)
                    )
                }

                // Overlay tag indices on vector map
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(primaryColor))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Your Live GPS", color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("St. Mary Hub Site", color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(primaryColor.copy(alpha = 0.5f)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Work Geofence Bounds", color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Simulated Telemetries Map Frame", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Use slider walk tool to shift coordinates inside or outside the verified zone limits.", fontSize = 9.sp, color = Color.Gray)
                        }
                        AssistChip(
                            onClick = {},
                            label = { Text("Accuracy GPS: HIGH", fontSize = 9.sp) }
                        )
                    }
                }
            }
        }
    }
}
