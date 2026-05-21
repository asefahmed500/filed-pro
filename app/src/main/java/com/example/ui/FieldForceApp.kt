package com.example.ui

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldForceApp(
    viewModel: FieldForceViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    if (currentUser == null) {
        WelcomeScreen(viewModel = viewModel)
        return
    }
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val activeAttendance by viewModel.activeAttendance.collectAsStateWithLifecycle()

    val myAttendance by viewModel.myAttendance.collectAsStateWithLifecycle()
    val myTasks by viewModel.myTasks.collectAsStateWithLifecycle()
    val myVisits by viewModel.myVisits.collectAsStateWithLifecycle()
    val myFiles by viewModel.myFiles.collectAsStateWithLifecycle()
    val myNotifications by viewModel.myNotifications.collectAsStateWithLifecycle()

    val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allVisits by viewModel.allVisits.collectAsStateWithLifecycle()
    val allFileRecords by viewModel.allFileRecords.collectAsStateWithLifecycle()
    val executives by viewModel.executives.collectAsStateWithLifecycle()

    // Screen navigation tracking
    var currentTab by remember { mutableStateOf("dashboard") }
    var showNotificationCenter by remember { mutableStateOf(false) }

    // Dialog control states
    var showCheckInDialog by remember { mutableStateOf(false) }
    var showCheckOutDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddFileDialog by remember { mutableStateOf(false) }
    var showVisitDetailsId by remember { mutableStateOf<Int?>(null) }
    var showTaskDetailsId by remember { mutableStateOf<Int?>(null) }
    var showUserSwitcherDir by remember { mutableStateOf(false) }

    // Coordinates Simulation Slider
    var simulatedLat by remember { mutableStateOf(37.7725) }
    var simulatedLng by remember { mutableStateOf(-122.4194) }

    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    // Handle incoming toasts
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            // Display as SnackBar or Toast
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.SafetyCheck,
                            contentDescription = "FieldForce Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "FieldForce Pro",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = currentUser?.role?.lowercase()?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } ?: "Select Role",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Predefined Profile Picker Switcher
                    IconButton(
                        onClick = { showUserSwitcherDir = true },
                        modifier = Modifier.testTag("user_switcher_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwitchAccount,
                            contentDescription = "Switch Account"
                        )
                    }

                    // Network Offline Toggle
                    IconButton(
                        onClick = { viewModel.toggleOnline() },
                        modifier = Modifier.testTag("network_toggle")
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Filled.CloudQueue else Icons.Filled.CloudOff,
                            contentDescription = "Network State",
                            tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    // Notification Center button
                    BadgedBox(
                        badge = {
                            val unreadCount = myNotifications.count { !it.isRead }
                            if (unreadCount > 0) {
                                Badge { Text(unreadCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { showNotificationCenter = true },
                            modifier = Modifier.testTag("notifications_button")
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                        }
                    }

                    // Logout / Sign Out button
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            // High-fidelity Apple-style floating rounded glassmorphic tab-bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .navigationBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, bottom = 12.dp, top = 2.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    val tabs = when (currentUser?.role) {
                        "ADMIN" -> listOf(
                            TabItem("dashboard", "KPIs", Icons.Filled.Assessment, Icons.Outlined.Assessment, "tab_dashboard"),
                            TabItem("tasks", "SLA Monitor", Icons.Filled.FolderShared, Icons.Outlined.FolderShared, "tab_tasks"),
                            TabItem("docs", "Finance", Icons.Filled.Description, Icons.Outlined.Description, "tab_docs"),
                            TabItem("map", "Central Map", Icons.Filled.Map, Icons.Outlined.Map, "tab_map"),
                            TabItem("prd", "Control Hub", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings, "tab_prd")
                        )
                        "MANAGER" -> listOf(
                            TabItem("dashboard", "Team Space", Icons.Filled.Analytics, Icons.Outlined.Analytics, "tab_dashboard"),
                            TabItem("tasks", "Dispatch", Icons.Filled.AddTask, Icons.Outlined.AddTask, "tab_tasks"),
                            TabItem("docs", "Claims", Icons.Filled.Paid, Icons.Outlined.Paid, "tab_docs"),
                            TabItem("map", "Live Radar", Icons.Filled.Radar, Icons.Outlined.Radar, "tab_map"),
                            TabItem("prd", "Specs Center", Icons.Filled.FactCheck, Icons.Outlined.FactCheck, "tab_prd")
                        )
                        else -> listOf( // EXECUTIVE
                            TabItem("dashboard", "Shift Track", Icons.Filled.Fingerprint, Icons.Outlined.Fingerprint, "tab_dashboard"),
                            TabItem("tasks", "My Jobs", Icons.Filled.Assignment, Icons.Outlined.Assignment, "tab_tasks"),
                            TabItem("docs", "Claims Submit", Icons.Filled.CloudUpload, Icons.Outlined.CloudUpload, "tab_docs"),
                            TabItem("map", "Geo-Map", Icons.Filled.Map, Icons.Outlined.Map, "tab_map"),
                            TabItem("prd", "Specs Doc", Icons.Filled.FactCheck, Icons.Outlined.FactCheck, "tab_prd")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentTab == tab.id
                            val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { currentTab = tab.id }
                                    .testTag(tab.testTag),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                        tint = contentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = tab.label,
                                        fontSize = 9.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        color = contentColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentUser != null) {
                when (currentUser!!.role) {
                    "ADMIN", "MANAGER" -> {
                        if (currentTab == "tasks") {
                            ExtendedFloatingActionButton(
                                onClick = { showAddTaskDialog = true },
                                modifier = Modifier.testTag("assign_task_fab"),
                                icon = { Icon(Icons.Filled.Add, "Assign Task") },
                                text = { Text("Assign Job") }
                            )
                        }
                    }
                    "EXECUTIVE" -> {
                        if (currentTab == "docs") {
                            ExtendedFloatingActionButton(
                                onClick = { showAddFileDialog = true },
                                modifier = Modifier.testTag("upload_file_fab"),
                                icon = { Icon(Icons.Filled.CloudUpload, "Upload File") },
                                text = { Text("Submit File") }
                            )
                        } else if (currentTab == "dashboard") {
                            if (activeAttendance == null) {
                                ExtendedFloatingActionButton(
                                    onClick = { showCheckInDialog = true },
                                    modifier = Modifier.testTag("check_in_fab"),
                                    icon = { Icon(Icons.Filled.Login, "Check In") },
                                    text = { Text("Check In") },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            } else {
                                ExtendedFloatingActionButton(
                                    onClick = { showCheckOutDialog = true },
                                    modifier = Modifier.testTag("check_out_fab"),
                                    icon = { Icon(Icons.Filled.Logout, "Check Out") },
                                    text = { Text("Check Out") },
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Offline Warning Top Banner
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(visible = !isOnline) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CloudOff, contentDescription = "Offline", tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Offline Mode Active. Queuing actions local...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // GPS Simulation Controller (Pruned, Collapsible, Polished)
                var gpsSimExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (gpsSimExpanded) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { gpsSimExpanded = !gpsSimExpanded }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.GpsFixed,
                                    contentDescription = "GPS Sim",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Live GPS Walk Simulator",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (simulatedLat > 37.7850) Color(0xFFEF4444).copy(alpha = 0.1f)
                                            else Color(0xFF10B981).copy(alpha = 0.1f)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (simulatedLat > 37.7850) "OutOfBounds" else "Inside sector",
                                        color = if (simulatedLat > 37.7850) Color(0xFFEF4444) else Color(0xFF10B981),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (gpsSimExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = "Expand",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (gpsSimExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Sector Center (Work zone ready)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Out of Zone",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = simulatedLat.toFloat(),
                                onValueChange = {
                                    simulatedLat = it.toDouble()
                                    // Smoothly offset Lng relative for simulation walk
                                    simulatedLng = -122.4194 + (it - 37.7725) * 1.5
                                },
                                valueRange = 37.7500f..37.7950f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Current Coords: ${String.format("%.4f", simulatedLat)}, ${String.format("%.4f", simulatedLng)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                toastMessage?.let {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = it, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { viewModel.clearToast() }) {
                                Text("Okey")
                            }
                        }
                    }
                }

                // Switch Dashboard Tabs
                when (currentTab) {
                    "dashboard" -> DashboardView(
                        currentUser = currentUser,
                        activeAttendance = activeAttendance,
                        myAttendance = myAttendance,
                        allAttendance = allAttendance,
                        allTasks = allTasks,
                        allFileRecords = allFileRecords,
                        executives = executives,
                        simulatedLat = simulatedLat,
                        simulatedLng = simulatedLng,
                        onTriggerCheckIn = { showCheckInDialog = true },
                        onTriggerCheckOut = { showCheckOutDialog = true }
                    )
                    "tasks" -> TasksAndVisitsView(
                        currentUser = currentUser,
                        myTasks = myTasks,
                        myVisits = myVisits,
                        allTasks = allTasks,
                        allVisits = allVisits,
                        executives = executives,
                        simulatedLat = simulatedLat,
                        simulatedLng = simulatedLng,
                        onViewTaskDetails = { id -> showTaskDetailsId = id },
                        onViewVisitDetails = { id -> showVisitDetailsId = id },
                        viewModel = viewModel
                    )
                    "docs" -> FileTrackingCenterView(
                        currentUser = currentUser,
                        myFiles = myFiles,
                        allFiles = allFileRecords,
                        onApproveFile = { file -> viewModel.updateFileStatus(file, "APPROVED") },
                        onRejectFile = { file, reason -> viewModel.updateFileStatus(file, "REJECTED", reason) },
                        viewModel = viewModel
                    )
                    "map" -> InteractiveSimulationMapView(
                        currentUser = currentUser,
                        executives = executives,
                        activeAttendance = activeAttendance,
                        allAttendance = allAttendance,
                        allVisits = allVisits,
                        simulatedLat = simulatedLat,
                        simulatedLng = simulatedLng
                    )
                    "prd" -> PrdScopeTodoView(
                        viewModel = viewModel,
                        onNavigateTab = { currentTab = it }
                    )
                }
            }

            // --- ALL FLOATING DIALOGS / BOTTOM SHEETS ---

            // User Profile Switcher Dialog
            if (showUserSwitcherDir) {
                AlertDialog(
                    onDismissRequest = { showUserSwitcherDir = false },
                    title = { Text("Choose Simulation Profile") },
                    text = {
                        Column {
                            Text("Demonstrates full Dashboard experience of each PRD Role:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 12.dp))
                            
                            // Arthur Admin Row
                            Card(
                                onClick = {
                                    viewModel.loginWithId("admin_1")
                                    showUserSwitcherDir = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = "Admin", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Arthur Pendragon", fontWeight = FontWeight.Bold)
                                        Text("Admin (Supervisory/KPIs Reports)", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            // Morgan Manager Row
                            Card(
                                onClick = {
                                    viewModel.loginWithId("manager_1")
                                    showUserSwitcherDir = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.SupervisorAccount, contentDescription = "Manager", tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Morgan LeFay", fontWeight = FontWeight.Bold)
                                        Text("Manager (Teams workflow & Doc Approvals)", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            // Lancelot Executive Row
                            Card(
                                onClick = {
                                    viewModel.loginWithId("exec_1")
                                    showUserSwitcherDir = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.DirectionsWalk, contentDescription = "Executive", tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Lancelot DuLac", fontWeight = FontWeight.Bold)
                                        Text("Field Executive (Selfie Checkin & Task Tracker)", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            // Guinevere Executive Row
                            Card(
                                onClick = {
                                    viewModel.loginWithId("exec_2")
                                    showUserSwitcherDir = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.DirectionsRun, contentDescription = "Executive 2", tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Guinevere Row", fontWeight = FontWeight.Bold)
                                        Text("Field Executive (SF Core sector)", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showUserSwitcherDir = false }) { Text("Close") }
                    }
                )
            }

            // Notification Center Dialog Mode
            if (showNotificationCenter) {
                AlertDialog(
                    onDismissRequest = { showNotificationCenter = false },
                    title = {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Notifications Center")
                            IconButton(onClick = { viewModel.clearAllNotifications() }) {
                                Icon(Icons.Filled.DoneAll, contentDescription = "Mark all Read")
                            }
                        }
                    },
                    text = {
                        Box(modifier = Modifier.height(350.dp)) {
                            if (myNotifications.isEmpty()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Outlined.NotificationsActive, contentDescription = "Empty", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("All clear! No pending alerts.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn {
                                    items(myNotifications) { item ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = if (item.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                                Text(
                                                    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(item.timestamp)),
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.End,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showNotificationCenter = false }) { Text("Dismiss") }
                    }
                )
            }

            // Check-In Details Dialog with Selfie Generator
            if (showCheckInDialog) {
                var manualNote by remember { mutableStateOf("") }
                var checkInSelfieOption by remember { mutableStateOf("https://images.unsplash.com/photo-1540569014015-19a7be504e3a?auto=format&fit=crop&w=128&q=80") }
                val checkInPhotos = listOf(
                    "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?auto=format&fit=crop&w=128&q=80",
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=128&q=80",
                    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=128&q=80",
                    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=128&q=80"
                )

                // Geofence calculations
                // Verify distance to SF central area 37.7749 / -122.4194 vs simulatedLat/Lng
                val distance = calculateDistanceInMeters(simulatedLat, simulatedLng, 37.7749, -122.4194)
                val outsideGeofence = distance > 500.0 // 500m geofence radius for SF core simulation

                AlertDialog(
                    onDismissRequest = { showCheckInDialog = false },
                    title = { Text("Daily Attendance Check-In") },
                    text = {
                        Column {
                            Text("Please confirm your start-of-day details:", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Simulated selfie selector
                            Text("Take Check-In Selfie photo:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                checkInPhotos.forEach { url ->
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                width = if (checkInSelfieOption == url) 3.dp else 1.dp,
                                                color = if (checkInSelfieOption == url) MaterialTheme.colorScheme.primary else Color.Gray,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { checkInSelfieOption = url }
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Selfie options",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = manualNote,
                                onValueChange = { manualNote = it },
                                label = { Text("Manual Note (e.g. Starting Shift)") },
                                placeholder = { Text("Add comment...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("check_in_note_input")
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (outsideGeofence) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row {
                                        Icon(
                                            imageVector = if (outsideGeofence) Icons.Filled.ReportGmailerrorred else Icons.Filled.CheckCircle,
                                            contentDescription = "Geofence State",
                                            tint = if (outsideGeofence) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (outsideGeofence) "GEOFENCE OUTSIDE SECTOR!" else "Inside working sector limits.",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (outsideGeofence) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = "Current distance: ${String.format("%.1f", distance)}m to Zone Center coordinates.",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.checkIn(
                                    selfieUri = checkInSelfieOption,
                                    note = manualNote.ifEmpty { "Commencing route on schedule" },
                                    lat = simulatedLat,
                                    lng = simulatedLng,
                                    isOutsideGeofence = distance > 500.0
                                )
                                showCheckInDialog = false
                            },
                            modifier = Modifier.testTag("submit_check_in")
                        ) {
                            Text("Confirm Check-In")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCheckInDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Check-Out Dialog (End-of-day inputs)
            if (showCheckOutDialog) {
                var notes by remember { mutableStateOf("") }
                var tasksCompleted by remember { mutableStateOf("1") }
                var expensesSpent by remember { mutableStateOf("0.0") }

                AlertDialog(
                    onDismissRequest = { showCheckOutDialog = false },
                    title = { Text("End Of Day Shift Summary") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("EOD Progress / Notes") },
                                placeholder = { Text("Comments regarding tasks worked...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("eod_notes")
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = tasksCompleted,
                                onValueChange = { tasksCompleted = it },
                                label = { Text("Total Tasks Completed Today") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = expensesSpent,
                                onValueChange = { expensesSpent = it },
                                label = { Text("Log Reimbursable Miscellaneous Expenses ($)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.checkOut(
                                    note = notes.ifEmpty { "Shift finished successfully." },
                                    tasksCompleted = tasksCompleted.toIntOrNull() ?: 0,
                                    expenses = expensesSpent.toDoubleOrNull() ?: 0.0
                                )
                                showCheckOutDialog = false
                            },
                            modifier = Modifier.testTag("submit_check_out")
                        ) {
                            Text("Check Out & Sync")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCheckOutDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Assign Task Dialog (Supervisor Task Creation)
            if (showAddTaskDialog) {
                var taskTitle by remember { mutableStateOf("") }
                var taskDesc by remember { mutableStateOf("") }
                var selectedPriority by remember { mutableStateOf("MEDIUM") }
                var locationAddr by remember { mutableStateOf("") }
                var targetExecId by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showAddTaskDialog = false },
                    title = { Text("Assign Work Job Task") },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            OutlinedTextField(
                                value = taskTitle,
                                onValueChange = { taskTitle = it },
                                label = { Text("Task / Job Title") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("task_title_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = taskDesc,
                                onValueChange = { taskDesc = it },
                                label = { Text("Detailed Specifications") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Select Priority Level:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                                    FilterChip(
                                        selected = selectedPriority == p,
                                        onClick = { selectedPriority = p },
                                        label = { Text(p) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = locationAddr,
                                onValueChange = { locationAddr = it },
                                label = { Text("Destination Address Worksite") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Assign Executive Member:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            executives.forEach { member ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { targetExecId = member.id }
                                        .background(if (targetExecId == member.id) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Person, "Member")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(member.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(member.workZoneName, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (taskTitle.isNotEmpty() && targetExecId.isNotEmpty()) {
                                    viewModel.assignTask(
                                        title = taskTitle,
                                        description = taskDesc,
                                        priority = selectedPriority,
                                        dueDate = System.currentTimeMillis() + 86400000,
                                        address = locationAddr.ifEmpty { "Default Sector Depot" },
                                        execId = targetExecId
                                    )
                                    showAddTaskDialog = false
                                } else {
                                    viewModel.showToast("Title and Executive assignment is mandatory!")
                                }
                            },
                            modifier = Modifier.testTag("submit_assign_task")
                        ) {
                            Text("Create Job")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Submit File Submission Dialog (Expense/bill reimbursements or PODs)
            if (showAddFileDialog) {
                var docName by remember { mutableStateOf("") }
                var fileCategory by remember { mutableStateOf("EXPENSE") }
                var totalAmount by remember { mutableStateOf("") }
                var tagsVal by remember { mutableStateOf("") }
                var selectPhotoUri by remember { mutableStateOf("https://images.unsplash.com/photo-1554415707-6e8cfc93fe23?auto=format&fit=crop&w=128&q=80") }

                val filePhotosSamples = listOf(
                    "https://images.unsplash.com/photo-1554415707-6e8cfc93fe23?auto=format&fit=crop&w=128&q=80", // receipt bill
                    "https://images.unsplash.com/photo-1450133064473-71024230f91b?auto=format&fit=crop&w=128&q=80", // construction delivery pod
                    "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=128&q=80"  // warehouse/dispatch
                )

                AlertDialog(
                    onDismissRequest = { showAddFileDialog = false },
                    title = { Text("Submit File / Document Voucher") },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            OutlinedTextField(
                                value = docName,
                                onValueChange = { docName = it },
                                label = { Text("Document Voucher Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("fdoc_name")
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Select Document Category:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                listOf("EXPENSE", "POD", "INCIDENT", "TIMESHEET").forEach { cat ->
                                    FilterChip(
                                        selected = fileCategory == cat,
                                        onClick = { fileCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            if (fileCategory == "EXPENSE") {
                                OutlinedTextField(
                                    value = totalAmount,
                                    onValueChange = { totalAmount = it },
                                    label = { Text("Total Amount Claim ($)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            OutlinedTextField(
                                value = tagsVal,
                                onValueChange = { tagsVal = it },
                                label = { Text("Tags (comma separated e.g., transport, repair)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Select Camera Capture Preview Image:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                filePhotosSamples.forEach { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                width = if (selectPhotoUri == uri) 3.dp else 1.dp,
                                                color = if (selectPhotoUri == uri) MaterialTheme.colorScheme.primary else Color.Gray,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectPhotoUri = uri }
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Invoice proof photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (docName.isNotEmpty()) {
                                    viewModel.fileDocument(
                                        fileName = docName,
                                        category = fileCategory,
                                        amount = totalAmount.toDoubleOrNull(),
                                        tags = tagsVal.ifEmpty { "general" },
                                        fileUri = selectPhotoUri
                                    )
                                    showAddFileDialog = false
                                } else {
                                    viewModel.showToast("Enter a document description name!")
                                }
                            },
                            modifier = Modifier.testTag("submit_file_button")
                        ) {
                            Text("Submit Document")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddFileDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Task Details / Progression Sheet with Digital Signature
            showTaskDetailsId?.let { taskId ->
                val task = allTasks.find { it.id == taskId } ?: return@let
                var showSignatureArea by remember { mutableStateOf(false) }
                var signaturePathData by remember { mutableStateOf<Path?>(null) }
                var viewSelfieLink by remember { mutableStateOf("https://images.unsplash.com/photo-1540569014015-19a7be504e3a?auto=format&fit=crop&w=128&q=80") }

                AlertDialog(
                    onDismissRequest = { showTaskDetailsId = null },
                    title = { Text(task.title) },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("Job Specification Info:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(task.description, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Priority: ${task.priority}") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.PriorityHigh, "Priority",
                                            tint = when(task.priority) {
                                                "HIGH" -> Color.Red
                                                "MEDIUM" -> Color.Yellow
                                                else -> Color.Blue
                                            }
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Status: ${task.status}") }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Site Position: ${task.locationAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons according to Executive progress flow
                            if (currentUser?.role == "EXECUTIVE") {
                                when (task.status) {
                                    "PENDING" -> {
                                        Button(
                                            onClick = { viewModel.updateTask(task, "IN_PROGRESS") },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Set to: In Progress")
                                        }
                                    }
                                    "IN_PROGRESS" -> {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Submit Finish Proof Logs:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Filled.PhotoCamera, "Selfie Capture")
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Working Proof Image Attached", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Touch Signature toggle
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Client Completion Sign:", fontSize = 11.sp)
                                                    TextButton(onClick = { showSignatureArea = !showSignatureArea }) {
                                                        Text(if (showSignatureArea) "Hide Drawing Pad" else "Draw Signature")
                                                    }
                                                }

                                                if (showSignatureArea) {
                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(100.dp)
                                                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        SignaturePad(
                                                            modifier = Modifier.fillMaxSize(),
                                                            onPathChanged = { signaturePathData = it }
                                                        )
                                                    }
                                                    Text("Please draw customer initials directly above.", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.updateTask(
                                                    task = task,
                                                    newStatus = "COMPLETED",
                                                    proofPhoto = viewSelfieLink,
                                                    signature = if (signaturePathData != null) "DemoSignatureBase64String" else null
                                                )
                                                showTaskDetailsId = null
                                            },
                                            modifier = Modifier.fillMaxWidth().testTag("mark_task_completed")
                                        ) {
                                            Text("Finish & Resolve Job")
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showTaskDetailsId = null }) { Text("Dismiss") }
                    }
                )
            }

            // Customer Visit Detail Sheet with Automatic PDF Report generator
            showVisitDetailsId?.let { visitId ->
                val visit = allVisits.find { it.id == visitId } ?: return@let

                AlertDialog(
                    onDismissRequest = { showVisitDetailsId = null },
                    title = { Text("Customer Visit Report (Summary)") },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("PDF GENERATED SUCCESS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                    Text(text = visit.reportPdfName ?: "Report_Visit.pdf", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Customer Representative Details:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(visit.customerName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(visit.address, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Visit Timeline Metadata:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("Checked-In: ${SimpleDateFormat("hh:mm a (dd MMM yyyy)", Locale.getDefault()).format(Date(visit.checkInTime))}", fontSize = 11.sp)
                            visit.checkOutTime?.let {
                                Text("Checked-Out: ${SimpleDateFormat("hh:mm a (dd MMM yyyy)", Locale.getDefault()).format(Date(it))}", fontSize = 11.sp)
                                val durationMins = (it - visit.checkInTime) / 60000
                                Text("Duration: $durationMins mins on customer site", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Visit Notes Taken:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(visit.notes ?: "N/A - Visit ongoing", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

                            if (visit.photoUri != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Inspection Selfie Photo Attached:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    AsyncImage(
                                        model = visit.photoUri,
                                        contentDescription = "Visit Proof",
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }

                            if (visit.signatureBase64 != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Authorized Customer Sign Off:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .background(Color.White)
                                        .border(2.dp, MaterialTheme.colorScheme.primaryContainer),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                        // Draw simulated signature graphics
                                        Text("[Digitally Signed: Authorized Rep]", color = Color(0xFF1E3A8A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showVisitDetailsId = null }) { Text("Dismiss") }
                    }
                )
            }
        }
    }
}

// Distance Calculation helper of Geofencing checking limits
fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0 // meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

// Touch Signature Drawing Component Canvas
@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onPathChanged: (Path) -> Unit
) {
    val path = remember { Path() }
    var changeTrigger by remember { mutableStateOf(0) }

    Canvas(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        path.moveTo(offset.x, offset.y)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        path.lineTo(change.position.x, change.position.y)
                        changeTrigger++
                        onPathChanged(path)
                    }
                )
            }
    ) {
        // Redraw canvas item
        changeTrigger
        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(width = 6f)
        )
    }
}

// FORMAT DATE UTILS
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private data class TabItem(
    val id: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
