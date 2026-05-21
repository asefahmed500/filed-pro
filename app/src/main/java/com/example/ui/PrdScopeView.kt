package com.example.ui

import android.content.Context
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.util.Locale

// Data structures for PRD Checklist
data class PrdSubmodule(
    val id: String,
    val title: String,
    val isDefaultImplemented: Boolean,
    val demoAction: String? = null,
    val targetRole: String? = null, // "ADMIN", "MANAGER", "EXECUTIVE"
    val description: String = ""
)

data class PrdModule(
    val id: Int,
    val layer: String, // "Mobile App", "Backend API", "Web Admin", "Infrastructure"
    val title: String,
    val description: String,
    val submodules: List<PrdSubmodule>
)

data class CustomTodo(
    val id: String,
    val text: String,
    val moduleTitle: String,
    val isDone: Boolean
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PrdScopeTodoView(
    viewModel: FieldForceViewModel,
    modifier: Modifier = Modifier,
    onNavigateTab: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("prd_scope_prefs", Context.MODE_PRIVATE) }
    
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    
    // Search & Category Filters State
    var searchQuery by remember { mutableStateOf("") }
    var selectedLayerFilter by remember { mutableStateOf("All") }
    
    // Submodules completion stats trigger
    var triggerUpdate by remember { mutableStateOf(0) }
    
    // Expanded modules map
    val expandedModules = remember { mutableStateMapOf<Int, Boolean>() }
    
    // Custom requirement builder state
    var customTodoText by remember { mutableStateOf("") }
    var selectedModuleForCustomTodo by remember { mutableStateOf("General Scope") }
    var showCustomTodoDialog by remember { mutableStateOf(false) }
    
    // Custom requirements list state
    val customTodos = remember { mutableStateListOf<CustomTodo>() }
    
    // Load custom requirements once initially
    LaunchedEffect(Unit) {
        val count = prefs.getInt("custom_todo_count", 0)
        customTodos.clear()
        for (i in 0 until count) {
            val id = prefs.getString("custom_todo_id_$i", "") ?: ""
            val text = prefs.getString("custom_todo_text_$i", "") ?: ""
            val moduleTitle = prefs.getString("custom_todo_mod_$i", "General Scope") ?: "General Scope"
            val isDone = prefs.getBoolean("custom_todo_done_$i", false)
            if (id.isNotEmpty() && text.isNotEmpty()) {
                customTodos.add(CustomTodo(id, text, moduleTitle, isDone))
            }
        }
    }
    
    fun saveCustomTodos() {
        val editor = prefs.edit()
        editor.putInt("custom_todo_count", customTodos.size)
        customTodos.forEachIndexed { i, todo ->
            editor.putString("custom_todo_id_$i", todo.id)
            editor.putString("custom_todo_text_$i", todo.text)
            editor.putString("custom_todo_mod_$i", todo.moduleTitle)
            editor.putBoolean("custom_todo_done_$i", todo.isDone)
        }
        editor.apply()
        triggerUpdate++
    }

    // Static master definitions of modules and submodules
    val prdModules = remember {
        listOf(
            // MOBILE APP MODULES (1 - 10)
            PrdModule(
                id = 1,
                layer = "Mobile App",
                title = "Authentication & User Session Module",
                description = "Secures operational boundaries with logins, automated status refreshers, and biometric shortcuts.",
                submodules = listOf(
                    PrdSubmodule("m_auth_1", "Secure login with role-based automatic layout redirection", true, "dashboard", null, "Logs users into Admin, Manager, or Executive visual consoles natively."),
                    PrdSubmodule("m_auth_2", "Live Multi-user mock simulation profile switcher", true, "dashboard", null, "Quickly jump and toggle roles using the top-right toolbar action bar."),
                    PrdSubmodule("m_auth_3", "Token refresh validation with secure logout mechanisms", true, null, null, "Secures server tokens and supports forced logout workflows."),
                    PrdSubmodule("m_auth_4", "Biometric credentials enrollment setup wizard", false, null, null, "Authenticates personnel inside complex terminal areas fast.")
                )
            ),
            PrdModule(
                id = 2,
                layer = "Mobile App",
                title = "Profile Management Module",
                description = "Handles executive dashboard profiles, hierarchy routing, and display settings.",
                submodules = listOf(
                    PrdSubmodule("m_prof_1", "Interactive profile detail viewer & metadata summaries", true, "dashboard", "EXECUTIVE", "Shows work zone limits, photo avatars, and phone stats."),
                    PrdSubmodule("m_prof_2", "Modify reporting manager flow requests", true, null, "EXECUTIVE", "Submits requests up the chain of leadership for fast rerouting."),
                    PrdSubmodule("m_prof_3", "Interface dynamic brightness or theme toggle", true, "dashboard", null, "Supports eye confort on high-brightness field audits.")
                )
            ),
            PrdModule(
                id = 3,
                layer = "Mobile App",
                title = "Geo-Attendance Module",
                description = "Calculates location, cross-references geofence boundaries, and submits proof.",
                submodules = listOf(
                    PrdSubmodule("m_attend_1", "GPS Geofenced boundary entry validation", true, "dashboard", "EXECUTIVE", "Validates check-in coords against assigned sector radii."),
                    PrdSubmodule("m_attend_2", "Front-facing selfie verification validation", true, "dashboard", "EXECUTIVE", "Locks selfie photo records into check-in metadata permanently."),
                    PrdSubmodule("m_attend_3", "Offline actions local SQLite queuing engine", true, "dashboard", "EXECUTIVE", "Queues check-in events when network signals drop to 0%."),
                    PrdSubmodule("m_attend_4", "Geofence violation banner & bypass request alerts", true, "dashboard", "EXECUTIVE", "Permits override reporting for managers when outside bounds.")
                )
            ),
            PrdModule(
                id = 4,
                layer = "Mobile App",
                title = "Task Management Module",
                description = "Tracks checklists, priorities, duration, and captures completion proofs.",
                submodules = listOf(
                    PrdSubmodule("m_task_1", "Interactive jobs list sorting by priority metrics", true, "tasks", null, "Allows executives to organize tasks by High/Medium/Low priority."),
                    PrdSubmodule("m_task_2", "Visual timeline maps detail view and navigation", true, "tasks", null, "Pinpoints customer site location and calculates estimated travel meters."),
                    PrdSubmodule("m_task_3", "Completion proof signatures & site photo submissions", true, "tasks", "EXECUTIVE", "Saves digital canvas signatures and onsite photos directly."),
                    PrdSubmodule("m_task_4", "Active job working elapsed-time duration tracking timer", true, "tasks", "EXECUTIVE", "Registers exact check-in and checkout seconds for time-sheets.")
                )
            ),
            PrdModule(
                id = 5,
                layer = "Mobile App",
                title = "Visit / Customer Meeting Module",
                description = "Logs customer checkins, collects notes/voice briefs, and builds reports on checkout.",
                submodules = listOf(
                    PrdSubmodule("m_visit_1", "Daily scheduled upcoming visits planner", true, "tasks", "EXECUTIVE", "Displays scheduled audits, client assets, and details."),
                    PrdSubmodule("m_visit_2", "Physical GPS Customer check-in selfie tracker", true, "tasks", "EXECUTIVE", "Saves timestamped coordinates directly on the meeting deck."),
                    PrdSubmodule("m_visit_3", "Comprehensive text notes and voice annotations logger", true, "tasks", "EXECUTIVE", "Compiles real-time onsite feedback and issues."),
                    PrdSubmodule("m_visit_4", "Digital signature of field customer representative", true, "tasks", "EXECUTIVE", "Captures representative approval signature instantly on glass."),
                    PrdSubmodule("m_visit_5", "Auto-generate PDF summary client visit report", true, "tasks", "EXECUTIVE", "Assembles digital PDF reports and stores them in local indexes.")
                )
            ),
            PrdModule(
                id = 6,
                layer = "Mobile App",
                title = "File Upload & Tracking Module",
                description = "Submits expenses, incident bills, or proof-of-delivery docs offline.",
                submodules = listOf(
                    PrdSubmodule("m_file_1", "Upload files from gallery, camera, context pickers", true, "docs", "EXECUTIVE", "Supports on-location camera snapshots of receipts and PODs."),
                    PrdSubmodule("m_file_2", "Categorize documents into POD, Expense, Incident, Timesheet", true, "docs", null, "Sorts incoming records to streamline audit workflows."),
                    PrdSubmodule("m_file_3", "Specifications annotations tags and monetary currency totals", true, "docs", "EXECUTIVE", "Integrates numerical expense values for quick reimbursement."),
                    PrdSubmodule("m_file_4", "Direct automated offline re-queuing retry engine", true, "docs", null, "Ensures failed files automatically sync back once internet recovers.")
                )
            ),
            PrdModule(
                id = 7,
                layer = "Mobile App",
                title = "Notification Module (In-app + Push)",
                description = "Keeps executives informed with alerts, reminders, and corporate messages.",
                submodules = listOf(
                    PrdSubmodule("m_notif_1", "Dynamic alert inbox and top menu badges", true, "dashboard", null, "Displays read vs unread indicators for dispatch messages."),
                    PrdSubmodule("m_notif_2", "Live push alerts triggered by role updates", true, "dashboard", null, "Instantly warns users when managers approve or reject files."),
                    PrdSubmodule("m_notif_3", "Corporate broadcast announcements overlay sheets", true, "dashboard", null, "Banner alerts for extreme weather or urgent tasks.")
                )
            ),
            PrdModule(
                id = 8,
                layer = "Mobile App",
                title = "Offline Sync Module",
                description = "Coordinates offline caches and handles multi-part sync streams smoothly.",
                submodules = listOf(
                    PrdSubmodule("m_sync_1", "Physical connection failure listener and state flag", true, "dashboard", null, "Listens to network connections or offline switch status."),
                    PrdSubmodule("m_sync_2", "Offline cache synchronizer for Room entities", true, "dashboard", null, "Keeps local SQL tables fully optimized for fast execution."),
                    PrdSubmodule("m_sync_3", "Status console with bulk queue retrial adapters", true, "dashboard", null, "Enables force-triggering of local syncs manually.")
                )
            ),
            PrdModule(
                id = 9,
                layer = "Mobile App",
                title = "Dashboard (Role-specific)",
                description = "Shows personalized productivity highlights depending on the logged-in role.",
                submodules = listOf(
                    PrdSubmodule("m_dash_1", "Executive action panel and quick checkout stats", true, "dashboard", "EXECUTIVE", "Gives on-the-road executives single-tap shortcuts to active work."),
                    PrdSubmodule("m_dash_2", "Manager summaries widget with supervisor approvals inbox", true, "dashboard", "MANAGER", "Shows team coverage graphs and outstanding doc queues."),
                    PrdSubmodule("m_dash_3", "Floating interactive trigger buttons for attendance", true, "dashboard", null, "Adapts dynamically to the current shift progress.")
                )
            ),
            PrdModule(
                id = 10,
                layer = "Mobile App",
                title = "Map & Location Module",
                description = "Displays live markers of zones, pins targets, and starts routes.",
                submodules = listOf(
                    PrdSubmodule("m_map_1", "Interactive tracking of coordinates with geofence map layers", true, "map", null, "Presents visual rings outlining assigned territories in San Francisco."),
                    PrdSubmodule("m_map_2", "Geocoding reverse addresses query translators", true, "map", null, "Interprets coordinates into readable street labels."),
                    PrdSubmodule("m_map_3", "Deep linking triggers for external navigational routes", true, "map", null, "Launches directions in Google Maps or Apple Maps with 1-click.")
                )
            ),

            // BACKEND API MODULES (11 - 20)
            PrdModule(
                id = 11,
                layer = "Backend API",
                title = "User & Role Management Service",
                description = "Handles users schemas, supervisory assignments, and account lockouts securely.",
                submodules = listOf(
                    PrdSubmodule("b_user_1", "CRUD database management endpoints for admin nodes", true, null, "ADMIN", "Saves profile, avatar photo references, and role states."),
                    PrdSubmodule("b_user_2", "Hierarchical tree managers mapping service", true, null, "ADMIN", "Hooks executives to specific supervisors for approvals flow."),
                    PrdSubmodule("b_user_3", "Forced remote session lockout security APIs", true, null, "ADMIN", "Allows immediate account disabling from the CRM console.")
                )
            ),
            PrdModule(
                id = 12,
                layer = "Backend API",
                title = "Authentication & Authorization Service",
                description = "Validates secure sessions, handles OTPs, and verifies permissions.",
                submodules = listOf(
                    PrdSubmodule("b_auth_1", "SMS OTP dispatcher wrapper service (Twilio/Mock)", true, null, null, "Dispatches OTP codes to verify mobile devices on first log."),
                    PrdSubmodule("b_auth_2", "JWT secure session parameters generator", true, null, null, "Generates secure claims for tamper-proof API client requests."),
                    PrdSubmodule("b_auth_3", "RBAC endpoint validators validation middleware", true, null, null, "Ensures executives can never trigger document approvals.")
                )
            ),
            PrdModule(
                id = 13,
                layer = "Backend API",
                title = "Attendance Service",
                description = "Stores timestamps, coordinates, and verifies working times.",
                submodules = listOf(
                    PrdSubmodule("b_attend_1", "Timesheet clocking endpoints recorder", true, null, null, "Captures check-in coordinate pairs, photos, and timestamps."),
                    PrdSubmodule("b_attend_2", "Worked hours math calculus processor", true, null, null, "Deducts check-out from check-in while offsetting pauses."),
                    PrdSubmodule("b_attend_3", "Late or missing check-out anomalies analyzer", true, null, null, "Flags days with open check-ins as missing checkout anomalies.")
                )
            ),
            PrdModule(
                id = 14,
                layer = "Backend API",
                title = "Task Service",
                description = "Supplies remote task triggers, assignments, and trackers.",
                submodules = listOf(
                    PrdSubmodule("b_task_1", "Task scheduling APIs dispatcher", true, null, "MANAGER", "Engages tasks by dispatching JSON targets down to mobile app client."),
                    PrdSubmodule("b_task_2", "Delegation distribution lists builder", true, null, "MANAGER", "Configures assignments to specific executive pools in 1-click."),
                    PrdSubmodule("b_task_3", "Performance rates metrics aggregates tracker", true, null, "MANAGER", "Computes metrics showing on-time performance averages dynamically.")
                )
            ),
            PrdModule(
                id = 15,
                layer = "Backend API",
                title = "Visit Service",
                description = "Ingests customer audits details and logs meeting states.",
                submodules = listOf(
                    PrdSubmodule("b_visit_1", "Customer visit planners database indexes", true, null, null, "Logs scheduled targets, customer addresses, and visit histories."),
                    PrdSubmodule("b_visit_2", "Automated PDF summary reports builder", true, null, null, "Assembles customer audit summaries with embedded photos on save."),
                    PrdSubmodule("b_visit_3", "External CRM synchronization pipelines", false, null, null, "Forwards logged visits into Salesforce or Zoho CRM engines.")
                )
            ),
            PrdModule(
                id = 16,
                layer = "Backend API",
                title = "File Storage & Processing Service",
                description = "Accepts file streams, runs image optimization, and triggers state flows.",
                submodules = listOf(
                    PrdSubmodule("b_file_1", "AWS S3 file streams storage adapter", true, null, null, "Inbound uploads storage pipeline for safety audit PDFs and invoices."),
                    PrdSubmodule("b_file_2", "Self-acting media compression & visual scaling", true, null, null, "Reduces oversized snapshots to save cellular team bandwidth."),
                    PrdSubmodule("b_file_3", "File states workflow engine (Pending/Approved/Rejected)", true, null, "MANAGER", "Triggers notifications depending on manager approval selections.")
                )
            ),
            PrdModule(
                id = 17,
                layer = "Backend API",
                title = "Notification Service",
                description = "Funnels notifications down to clients via push gateways.",
                submodules = listOf(
                    PrdSubmodule("b_notif_1", "FCM server payload packaging connector", true, null, null, "Funnels alert payloads down to Google Play endpoints."),
                    PrdSubmodule("b_notif_2", "Role-based database routing triggers", true, null, null, "Fires automated warnings when invoices undergo updates."),
                    PrdSubmodule("b_notif_3", "In-app notifications state indexes", true, null, null, "Records notifications log per user ID in remote database tables.")
                )
            ),
            PrdModule(
                id = 18,
                layer = "Backend API",
                title = "Reporting & Analytics Service",
                description = "Aggregates productivity telemetry into exportable sheets.",
                submodules = listOf(
                    PrdSubmodule("b_rep_1", "Spreadsheet exporter service (XLS/CSV compiler)", true, null, "ADMIN", "Generates reports for attendance timesheets and expenses."),
                    PrdSubmodule("b_rep_2", "Performance indicator trends database builder", true, null, "ADMIN", "Crunches aggregate numbers to outline team velocity charts."),
                    PrdSubmodule("b_rep_3", "Administrative dashboards dashboard aggregators", true, null, "ADMIN", "Pre-computes KPI figures for real-time manager loading.")
                )
            ),
            PrdModule(
                id = 19,
                layer = "Backend API",
                title = "Geofencing Service",
                description = "Performs polygon coordinate mapping and fires override alarms.",
                submodules = listOf(
                    PrdSubmodule("b_geo_1", "Polygon radius boundary coordinate math", true, null, null, "Determines if lat/lng coords intersect circle sector radii."),
                    PrdSubmodule("b_geo_2", "Boundary trespass tracking alerts", true, null, null, "Flags checked-in events conducted outside geo-fences."),
                    PrdSubmodule("b_geo_3", "Exemption bypass requests approver routing", true, null, "MANAGER", "Lets managers authorize and clear necessary out-of-zone checkins.")
                )
            ),
            PrdModule(
                id = 20,
                layer = "Backend API",
                title = "Sync & Offline Support API",
                description = "Processes offline uploads and ensures consistent databases.",
                submodules = listOf(
                    PrdSubmodule("b_sync_1", "Incremental delta-updates fetch end-point", true, null, null, "Sends only recent database rows to save mobile bandwidth."),
                    PrdSubmodule("b_sync_2", "Server-wins conflicts resolving engine", true, null, null, "Resolves simultaneous updates safely with rollback guards."),
                    PrdSubmodule("b_sync_3", "Bulk sync batch loader queue receiver", true, null, null, "Ingests lists of cached records compiled by offline executors.")
                )
            ),

            // WEB ADMIN PANEL MODULES (21 - 29)
            PrdModule(
                id = 21,
                layer = "Web Admin",
                title = "Admin Dashboard UX",
                description = "Shows live maps of the active field workforce and tracks alerts.",
                submodules = listOf(
                    PrdSubmodule("w_dash_1", "Live interactive supervisors map of personnel pins", true, "map", "MANAGER", "Draws live locations of active personnel in San Francisco."),
                    PrdSubmodule("w_dash_2", "Aggregated company KPI tracker analytics", true, "dashboard", "ADMIN", "Charts task volumes, attendance ratios, and outstanding items."),
                    PrdSubmodule("w_dash_3", "Outstanding administrative approvals alerts widget", true, "dashboard", "MANAGER", "Shows immediate summaries of pending expense files to review.")
                )
            ),
            PrdModule(
                id = 22,
                layer = "Web Admin",
                title = "Employee Management UI",
                description = "Edits profiles, manager trees, and resets credentials.",
                submodules = listOf(
                    PrdSubmodule("w_emp_1", "Direct employee details controller screens", true, "docs", "ADMIN", "Lists active members and supports toggling status switches."),
                    PrdSubmodule("w_emp_2", "Supervisor assignments visual structure editor", true, "docs", "ADMIN", "Connects new personnel to selected team managers."),
                    PrdSubmodule("w_emp_3", "Manual console credentials security overrides", true, null, "ADMIN", "Triggers immediate password adjustments or remote signouts.")
                )
            ),
            PrdModule(
                id = 23,
                layer = "Web Admin",
                title = "Attendance Review Module",
                description = "Inspects physical timesheets and manages bypass logs.",
                submodules = listOf(
                    PrdSubmodule("w_att_1", "Spreadsheet visual table with interactive filters", true, "dashboard", "MANAGER", "Sorts timesheets by date range, department, and error tags."),
                    PrdSubmodule("w_att_2", "Geofence violation coordinates visualization layers", true, "map", "MANAGER", "Map highlighter showing precise coordinates where breaches occurred."),
                    PrdSubmodule("w_att_3", "Timesheet override editor adjusting timesheets manual", true, "dashboard", "MANAGER", "Authorizes manual entry overrides of attendance check-in stamps.")
                )
            ),
            PrdModule(
                id = 24,
                layer = "Web Admin",
                title = "Task Assignment UI",
                description = "Creates, pins, and drags tasks to field personnel.",
                submodules = listOf(
                    PrdSubmodule("w_tsk_1", "Pinnable coordinates client task constructor", true, "tasks", "MANAGER", "Pins jobs directly onto coordinates on map screens."),
                    PrdSubmodule("w_tsk_2", "Interactive drag-and-drop assigner table", true, "tasks", "MANAGER", "Fast-assigns tasks into selected executive columns."),
                    PrdSubmodule("w_tsk_3", "Priority visual tag classifier and dates manager", true, "tasks", "MANAGER", "Sets scheduling variables and high-priority alarms.")
                )
            ),
            PrdModule(
                id = 25,
                layer = "Web Admin",
                title = "Visit Planner (Calendar view)",
                description = "Schedules meetings on timelines and visualizes routes.",
                submodules = listOf(
                    PrdSubmodule("w_vst_1", "Corporate calendar meeting agenda calendar", true, "tasks", "MANAGER", "Visual schedule board showcasing customer inspection dates."),
                    PrdSubmodule("w_vst_2", "Fluid scheduling timeline modification handles", true, "tasks", "MANAGER", "Enables rapid rescheduling of customer audit slots."),
                    PrdSubmodule("w_vst_3", "On-side data details inspector frame", true, "tasks", "MANAGER", "Lists client history, notes, and previous audit briefs.")
                )
            ),
            PrdModule(
                id = 26,
                layer = "Web Admin",
                title = "File Approval Panel",
                description = "Reviews, zooms into receipts, and writes reasons for rejection.",
                submodules = listOf(
                    PrdSubmodule("w_fil_1", "Multi-category incoming document review console", true, "docs", "MANAGER", "Presents expense vouchers, incident pictures, and POD signatures side-by-side."),
                    PrdSubmodule("w_fil_2", "Image canvas inline slider and zoom tool", true, "docs", "MANAGER", "Presents photo inspect sheets and expands signatures of client reps."),
                    PrdSubmodule("w_fil_3", "Rejection response dialog box with guidelines options", true, "docs", "MANAGER", "Captures reason text for rejection. Informs workers instantly.")
                )
            ),
            PrdModule(
                id = 27,
                layer = "Web Admin",
                title = "Report Export Center",
                description = "Compiles reports, exports spreadsheets, and schedules summaries.",
                submodules = listOf(
                    PrdSubmodule("w_rep_1", "Custom XLS / CSV table report download portal", true, "docs", "MANAGER", "Compiles field outcomes, expense totals, and working hours."),
                    PrdSubmodule("w_rep_2", "Scheduled reports email automated dispatcher", true, "docs", "MANAGER", "Automates weekly emails containing team KPI trends to executives."),
                    PrdSubmodule("w_rep_3", "Old files archives indices cleaner", true, "docs", null, "Keeps historical document records accessible on secure CDNs.")
                )
            ),
            PrdModule(
                id = 28,
                layer = "Web Admin",
                title = "Notification Broadcast Module",
                description = "Composes global banners and inspects open rates.",
                submodules = listOf(
                    PrdSubmodule("w_not_1", "Company-wide alerts composer editor", true, "dashboard", "MANAGER", "Composes notification titles and descriptions in 1-click."),
                    PrdSubmodule("w_not_2", "Targeted audience filter selection console", true, "dashboard", "MANAGER", "Dispatches broadcast messages only to active field teams."),
                    PrdSubmodule("w_not_3", "Dispatched alerts logs statistics telemetry", true, "dashboard", "MANAGER", "Charts delivery numbers and open statuses for notifications.")
                )
            ),
            PrdModule(
                id = 29,
                layer = "Web Admin",
                title = "Role & Permission Editor",
                description = "Configures custom groups and security scopes.",
                submodules = listOf(
                    PrdSubmodule("w_pe_1", "Capabilities permissions Matrix editor", false, null, "ADMIN", "Defines access permissions on sensitive corporate records."),
                    PrdSubmodule("w_pe_2", "Interface modules display limit configuration", false, null, "ADMIN", "Omit buttons or menus from clients that miss permissions."),
                    PrdSubmodule("w_pe_3", "Admin console actions audit log", false, null, "ADMIN", "Keeps persistent records of administrative profile updates.")
                )
            ),

            // INFRASTRUCTURE & CROSS-CUTTING (30 - 36)
            PrdModule(
                id = 30,
                layer = "Infrastructure",
                title = "Push Notification Gateway",
                description = "Schedules notifications, retries failures, and monitors clients.",
                submodules = listOf(
                    PrdSubmodule("i_ng_1", "FCM server API connector package layer", true, null, null, "Configures secure outbound connection handles to Google Firebase."),
                    PrdSubmodule("i_ng_2", "Subscribers device credentials tracking cache", true, null, null, "Maps client user logins to current device security tokens."),
                    PrdSubmodule("i_ng_3", "Delivery tracking analyzer with automatic retries", false, null, null, "Triggers backoff schedules if downstream gateways are congested.")
                )
            ),
            PrdModule(
                id = 31,
                layer = "Infrastructure",
                title = "File Storage Adapter",
                description = "Abstracts cloud drives, signs access URLs, and clears CDN caches.",
                submodules = listOf(
                    PrdSubmodule("i_fs_1", "Abstract multi-cloud driver interface", true, "docs", null, "Supports transparent swaps between AWS S3 and Local storage simulation."),
                    PrdSubmodule("i_fs_2", "Self-invalidating secure temporary URL tokens generator", true, "docs", null, "Prevents un-authorized public distribution of expense photos."),
                    PrdSubmodule("i_fs_3", "Automated edge CDN invalidator triggers", true, null, null, "Flushes out outdated profile drawings across CDNs.")
                )
            ),
            PrdModule(
                id = 32,
                layer = "Infrastructure",
                title = "Map & Geocoding Service",
                description = "Converts coordinates into addresses and performs distance math.",
                submodules = listOf(
                    PrdSubmodule("i_mg_1", "Reverse geo-coders coordinates address translator", true, "map", null, "Looks up coordinates on map indexes for neat text addresses."),
                    PrdSubmodule("i_mg_2", "Geofencing circle radii mathematical checkers", true, "map", null, "Executes fast checks whether coordinate pairs sit inside fences."),
                    PrdSubmodule("i_mg_3", "Map directions routing link compiler adapter", true, "map", null, "Translates coordinate coordinates into formatted Google/Apple Maps links.")
                )
            ),
            PrdModule(
                id = 33,
                layer = "Infrastructure",
                title = "PDF Generation Service",
                description = "Compiles HTML templates, stamps signatures, and seals files.",
                submodules = listOf(
                    PrdSubmodule("i_pdf_1", "Dynamic report HTML-to-PDF compilers driver", true, "tasks", null, "Converts database variables into branded corporate audit summaries."),
                    PrdSubmodule("i_pdf_2", "Aesthetic meeting templates design module", true, "tasks", null, "Includes metadata, coordinate grids, and logo watermarks in PDFs."),
                    PrdSubmodule("i_pdf_3", "Authoritative digital signature validation seals", true, "tasks", null, "Embeds base64 representative signatures right inside the document page.")
                )
            ),
            PrdModule(
                id = 34,
                layer = "Infrastructure",
                title = "Background Job Scheduler",
                description = "Runs cron schedules, triggers alert routines, and flushes old logs.",
                submodules = listOf(
                    PrdSubmodule("i_js_1", "EOD automatic timesheets validator scheduler", true, null, null, "An End-Of-Day cron job verifying active shift durations."),
                    PrdSubmodule("i_js_2", "Weekly email summarized PDF reports dispatcher", true, null, null, "Triggers dispatch of aggregate spreadsheet PDFs down to supervisors."),
                    PrdSubmodule("i_js_3", "Old secure session cleaner", true, null, null, "Clears expired login tokens to block terminal attacks.")
                )
            ),
            PrdModule(
                id = 35,
                layer = "Infrastructure",
                title = "Logging & Monitoring Module",
                description = "Traces sync payloads, logs errors, and measures API delay.",
                submodules = listOf(
                    PrdSubmodule("i_lm_1", "Local diagnostics tracker for synchronization packages", true, "dashboard", null, "Logs local JSON structures before sync attempts."),
                    PrdSubmodule("i_lm_2", "Central exception recorder parser", true, "dashboard", null, "Catches syntax anomalies or network failures securely."),
                    PrdSubmodule("i_lm_3", "API requests latency tracker metrics dashboard", true, null, null, "Monitors database access delays and sync execution times.")
                )
            ),
            PrdModule(
                id = 36,
                layer = "Infrastructure",
                title = "Data Seeder & Testing Module",
                description = "Seeds database profiles, launches code inspections, and tests builds.",
                submodules = listOf(
                    PrdSubmodule("i_ds_1", "Pre-built demo database profiles and task seeder", true, "dashboard", null, "Seeds representative users, tasks, and alerts automatically on creation."),
                    PrdSubmodule("i_ds_2", "High-volume load generation factory scripts", false, null, null, "Provides mockup generators for thousand-user location tracking."),
                    PrdSubmodule("i_ds_3", "Client integration tests validation suite", true, null, null, "Runs unit tests and linter audits to verify applet compilation.")
                )
            )
        )
    }

    // Read toggled checkboxes states dynamically from SharedPreferences
    val checkmarkStates = remember(triggerUpdate) {
        val statesMap = mutableStateMapOf<String, Boolean>()
        prdModules.forEach { module ->
            module.submodules.forEach { sub ->
                val defaultVal = sub.isDefaultImplemented
                // If not set yet, use the defaultVal
                if (!prefs.contains(sub.id)) {
                    prefs.edit().putBoolean(sub.id, defaultVal).apply()
                }
                statesMap[sub.id] = prefs.getBoolean(sub.id, defaultVal)
            }
        }
        statesMap
    }

    // Math metrics
    val totalSubmodules = prdModules.sumOf { it.submodules.size }
    val totalCustomTodos = customTodos.size
    val totalCheckedSubmodules = checkmarkStates.values.count { it }
    val totalCheckedCustomTodos = customTodos.count { it.isDone }
    
    val totalAllScope = totalSubmodules + totalCustomTodos
    val totalAllChecked = totalCheckedSubmodules + totalCheckedCustomTodos
    
    val overallPercentage = if (totalAllScope > 0) {
        (totalAllChecked.toFloat() / totalAllScope * 100).toInt()
    } else 0

    // Filter modules by Layer type and Search query
    val filteredModules = remember(selectedLayerFilter, searchQuery) {
        prdModules.filter { module ->
            val matchLayer = selectedLayerFilter == "All" || module.layer == selectedLayerFilter
            val matchSearch = searchQuery.isEmpty() || 
                    module.title.contains(searchQuery, ignoreCase = true) || 
                    module.description.contains(searchQuery, ignoreCase = true) ||
                    module.submodules.any { it.title.contains(searchQuery, ignoreCase = true) }
            matchLayer && matchSearch
        }
    }

    // Dynamic Greeting Background Brush
    val mainBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        // Master PRD Progress Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(mainBrush)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.FactCheck,
                                contentDescription = "Specs verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "FIELD FORCE SYSTEM PRD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Modules & Submodules Checklist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Linear indicator
                        LinearProgressIndicator(
                            progress = { totalAllChecked.toFloat() / totalAllScope.coerceAtLeast(1) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Completed $totalAllChecked/$totalAllScope features and submodules ($overallPercentage%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Large radial display
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$overallPercentage%",
                                modifier = Modifier.testTag("specs_percentage_text"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "READY",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Search Interface with Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search 36 Modules & Requirements...") },
                placeholder = { Text("e.g. selfie, maps, pdf...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("prd_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors()
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Button(
                onClick = { showCustomTodoDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("add_custom_item_bullet"),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Filled.PlaylistAdd, contentDescription = "Add Todo")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Todo")
            }
        }

        // Layer horizontal scrolls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val layers = listOf("All", "Mobile App", "Backend API", "Web Admin", "Infrastructure")
            layers.forEach { layer ->
                val isSelected = selectedLayerFilter == layer
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedLayerFilter = layer },
                    label = { 
                        val countLabel = when(layer) {
                            "All" -> "($totalSubmodules)"
                            "Mobile App" -> "(${prdModules.count { it.layer == "Mobile App" }.run { this * 4 }} Features)"
                            "Backend API" -> "(${prdModules.count { it.layer == "Backend API" }.run { this * 3 }} APIs)"
                            "Web Admin" -> "(${prdModules.count { it.layer == "Web Admin" }.run { this * 3 }} UIs)"
                            "Infrastructure" -> "(${prdModules.count { it.layer == "Infrastructure" }.run { this * 3 }} Core)"
                            else -> ""
                        }
                        Text("$layer $countLabel") 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("filter_chip_$layer")
                )
            }
        }

        // Main modules lists
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Admin-only User Directory & Provisioning Console
            if (currentUser?.role == "ADMIN") {
                item {
                    AdminUserManagementPanel(viewModel = viewModel, allUsers = allUsers)
                }
            }

            // Custom requirements list, show first if any exist
            if (customTodos.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.EditCalendar, contentDescription = "Custom Requirements", tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Custom PRD Submodules ($totalCheckedCustomTodos/$totalCustomTodos)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            customTodos.forEachIndexed { idx, todo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = todo.isDone,
                                        onCheckedChange = { isChecked ->
                                            customTodos[idx] = todo.copy(isDone = isChecked)
                                            saveCustomTodos()
                                        },
                                        modifier = Modifier.testTag("custom_todo_check_$idx")
                                    )
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = todo.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Layer context: ${todo.moduleTitle}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            customTodos.removeAt(idx)
                                            saveCustomTodos()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete custom todo",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Standard modules list
            items(filteredModules, key = { it.id }) { module ->
                val isExpanded = expandedModules[module.id] ?: false
                
                // Track aggregate checks for this module
                val moduleSubIds = module.submodules.map { it.id }
                val moduleCheckedCount = moduleSubIds.count { checkmarkStates[it] == true }
                val moduleTotalCount = moduleSubIds.size
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prd_module_card_${module.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (moduleCheckedCount == moduleTotalCount) {
                            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        } else {
                            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        }
                    ),
                    border = if (moduleCheckedCount == moduleTotalCount) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    } else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedModules[module.id] = !isExpanded }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Badge showing Module ID
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (moduleCheckedCount == moduleTotalCount) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        },
                                        CircleShape
                                    )
                            ) {
                                Text(
                                    text = if (moduleCheckedCount == moduleTotalCount) "✓" else "${module.id}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (moduleCheckedCount == moduleTotalCount) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = module.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(module.layer) },
                                        modifier = Modifier.height(20.dp),
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            labelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$moduleCheckedCount/$moduleTotalCount Checked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (moduleCheckedCount == moduleTotalCount) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Expand details"
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = module.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                module.submodules.forEach { submodule ->
                                    val isChecked = checkmarkStates[submodule.id] ?: false
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 2.dp)
                                            .background(
                                                if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                                else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { isNowChecked ->
                                                prefs.edit().putBoolean(submodule.id, isNowChecked).apply()
                                                triggerUpdate++
                                            },
                                            modifier = Modifier.testTag("check_${submodule.id}")
                                        )
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = submodule.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                )
                                                
                                                Spacer(modifier = Modifier.width(6.dp))
                                                
                                                // Predefined status markers
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (submodule.isDefaultImplemented) {
                                                        Color(0xFF0F766E).copy(alpha = 0.1f)
                                                    } else {
                                                        Color(0xFFD97706).copy(alpha = 0.1f)
                                                    },
                                                    contentColor = if (submodule.isDefaultImplemented) {
                                                        Color(0xFF0F766E)
                                                    } else {
                                                        Color(0xFFD97706)
                                                    },
                                                    modifier = Modifier.height(16.dp)
                                                ) {
                                                    Text(
                                                        text = if (submodule.isDefaultImplemented) "SIMULATED" else "PLANNED",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            
                                            if (submodule.description.isNotEmpty()) {
                                                Text(
                                                    text = submodule.description,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 11.sp
                                                )
                                            }
                                        }

                                        // Launch Demo Action
                                        if (submodule.demoAction != null) {
                                            IconButton(
                                                onClick = {
                                                    // Quick smart login redirect based on role
                                                    if (submodule.targetRole != null) {
                                                        viewModel.loginWithId(
                                                            when (submodule.targetRole) {
                                                                "ADMIN" -> "admin_1"
                                                                "MANAGER" -> "manager_1"
                                                                else -> "exec_1"
                                                            }
                                                        )
                                                    }
                                                    onNavigateTab(submodule.demoAction)
                                                    viewModel.showToast("Routed directly to PRD Feature: ${submodule.title}")
                                                },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .testTag("demo_btn_${submodule.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = "Launch module demo integration",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
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
    }

    // Modal dialog to add custom PRD requirements
    if (showCustomTodoDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTodoDialog = false },
            title = { Text("Add Custom Requirement") },
            text = {
                Column {
                    Text(
                        "Add user stories or custom modules to verify during the sprint checkouts:",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = customTodoText,
                        onValueChange = { customTodoText = it },
                        label = { Text("Task / Feature Requirement") },
                        placeholder = { Text("e.g. Integrate biometrics with hardware keys API") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_todo_input_desc")
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Target Layer Context:", style = MaterialTheme.typography.labelSmall)
                    
                    val contextOptions = listOf("General Scope", "Mobile App Client", "Backend Service APIs", "Web Administration", "Core Cross-cutting")
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_context_dropdown")
                        ) {
                            Text(selectedModuleForCustomTodo)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                        }
                        
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            contextOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        selectedModuleForCustomTodo = opt
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customTodoText.trim().isNotEmpty()) {
                            customTodos.add(
                                CustomTodo(
                                    id = "custom_" + System.currentTimeMillis(),
                                    text = customTodoText,
                                    moduleTitle = selectedModuleForCustomTodo,
                                    isDone = false
                                )
                            )
                            saveCustomTodos()
                            customTodoText = ""
                            showCustomTodoDialog = false
                            viewModel.showToast("Custom requirement successfully appended to Todo scope!")
                        }
                    },
                    modifier = Modifier.testTag("confirm_custom_todo_btn")
                ) {
                    Text("Add to Scope")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTodoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminUserManagementPanel(
    viewModel: FieldForceViewModel,
    allUsers: List<User>,
    modifier: Modifier = Modifier
) {
    var isCreationExpanded by remember { mutableStateOf(false) }
    var isDirectoryExpanded by remember { mutableStateOf(false) }

    // Insertion Form States
    var newUserName by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserPhone by remember { mutableStateOf("") }
    var newUserWorkZone by remember { mutableStateOf("") }
    var newUserRole by remember { mutableStateOf("EXECUTIVE") } // EXECUTIVE, MANAGER, ADMIN

    // Directory Search State
    var directorySearchQuery by remember { mutableStateOf("") }

    val filteredAllUsers = allUsers.filter {
        directorySearchQuery.isEmpty() ||
                it.name.contains(directorySearchQuery, ignoreCase = true) ||
                it.email.contains(directorySearchQuery, ignoreCase = true) ||
                it.role.contains(directorySearchQuery, ignoreCase = true) ||
                it.workZoneName.contains(directorySearchQuery, ignoreCase = true)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Title block for corporate Administrator privileges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = "Admin Area",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PRIVILEGED OPERATOR PROVISIONS Desk",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Enroll users & config any privilege levels (Admin / Manager / Exec).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-Card 1: Create/Register User Profile Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCreationExpanded = !isCreationExpanded }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = "Add User", tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Provision New Operator Account",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = if (isCreationExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "Expand Creation"
                        )
                    }

                    if (isCreationExpanded) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            OutlinedTextField(
                                value = newUserName,
                                onValueChange = { newUserName = it },
                                label = { Text("Operator Full Name") },
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("admin_prod_name")
                            )

                            OutlinedTextField(
                                value = newUserEmail,
                                onValueChange = { newUserEmail = it },
                                label = { Text("System Email Address") },
                                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("admin_prod_email")
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newUserPhone,
                                    onValueChange = { newUserPhone = it },
                                    label = { Text("Phone") },
                                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = "Phone") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.3f).testTag("admin_prod_phone")
                                )
                                OutlinedTextField(
                                    value = newUserWorkZone,
                                    onValueChange = { newUserWorkZone = it },
                                    label = { Text("Work Sector Zone") },
                                    leadingIcon = { Icon(Icons.Outlined.Map, contentDescription = "Zone") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.7f).testTag("admin_prod_zone")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "Override Role Permissions Security Level:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("EXECUTIVE" to "Exec", "MANAGER" to "Manager", "ADMIN" to "Admin").forEach { (roleCode, label) ->
                                    val isSelected = newUserRole == roleCode
                                    val btnColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(btnColor)
                                            .clickable { newUserRole = roleCode }
                                            .testTag("admin_prod_role_$roleCode"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (newUserName.trim().isEmpty() || newUserEmail.trim().isEmpty()) {
                                        viewModel.showToast("Failed to provision: Name and Email are mandatory fields.")
                                    } else {
                                        viewModel.adminCreateUser(
                                            name = newUserName,
                                            email = newUserEmail,
                                            role = newUserRole,
                                            phone = newUserPhone,
                                            workZone = newUserWorkZone
                                        )
                                        // Reset fields
                                        newUserName = ""
                                        newUserEmail = ""
                                        newUserPhone = ""
                                        newUserWorkZone = ""
                                        newUserRole = "EXECUTIVE"
                                        isCreationExpanded = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_submit_provision_btn")
                            ) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = "Submit")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Instantly Provision & Enroll User", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-Card 2: Directory List
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDirectoryExpanded = !isDirectoryExpanded }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Group, contentDescription = "Dir", tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Registered System Operators List (${allUsers.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = if (isDirectoryExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "Expand Directory"
                        )
                    }

                    if (isDirectoryExpanded) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                            OutlinedTextField(
                                value = directorySearchQuery,
                                onValueChange = { directorySearchQuery = it },
                                placeholder = { Text("Filter operators list...") },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Users") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                filteredAllUsers.forEach { user ->
                                    val badgeColor = when (user.role) {
                                        "ADMIN" -> Color(0xFFEF4444)
                                        "MANAGER" -> Color(0xFFF59E0B)
                                        else -> Color(0xFF10B981)
                                    }
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Box(
                                                    modifier = Modifier
                                                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = user.role,
                                                        color = badgeColor,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Email: ${user.email}", style = MaterialTheme.typography.bodySmall)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Phone: ${user.phone.ifEmpty { "N/A" }}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("Zone: ${user.workZoneName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                                if (filteredAllUsers.isEmpty()) {
                                    Text(
                                        text = "No matching operators found in directory Database.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
