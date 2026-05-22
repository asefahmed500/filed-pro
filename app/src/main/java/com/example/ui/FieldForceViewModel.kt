package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FieldForceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FieldForceDatabase.getDatabase(application, viewModelScope)
    private val repository = FieldForceRepository(db)

    // Current logged-in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Offline mode simulation switch
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Status notifications for the UI (e.g. sync toast, success toast)
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Live active attendance for the currently logged-in executive
    private val _activeAttendance = MutableStateFlow<Attendance?>(null)
    val activeAttendance: StateFlow<Attendance?> = _activeAttendance.asStateFlow()

    // All executives list
    val executives: StateFlow<List<User>> = repository.getExecutivesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All registered users (Admins, Managers, Executives)
    val allUsers: StateFlow<List<User>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All attendance records
    val allAttendance: StateFlow<List<Attendance>> = repository.getAllAttendanceFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All file submissions
    val allFileRecords: StateFlow<List<FileRecord>> = repository.getAllFilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All tasks
    val allTasks: StateFlow<List<Task>> = repository.getAllTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All customer visits
    val allVisits: StateFlow<List<Visit>> = repository.getAllVisitsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current user's specific data (Reactive Flows)
    val myAttendance: StateFlow<List<Attendance>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getAttendanceForEmployee(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myTasks: StateFlow<List<Task>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getTasksForEmployee(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myVisits: StateFlow<List<Visit>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getVisitsForEmployee(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myFiles: StateFlow<List<FileRecord>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getFilesForEmployee(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myNotifications: StateFlow<List<NotificationModel>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getNotificationsForUser(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending sync items from persistent queue
    val pendingSyncItems: StateFlow<List<SyncQueueItem>> = repository.getPendingSyncItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Fetch initial data from server
        refreshFromServer()
    }

    // Fetch all data from server
    fun refreshFromServer() = viewModelScope.launch {
        try {
            repository.refreshFromServer()
        } catch (e: Exception) {
            // Server might not be available, that's ok - we'll work offline
            showToast("Working offline - server not available")
        }
    }

    fun logout() = viewModelScope.launch {
        val user = _currentUser.value
        if (user != null) {
            repository.insertNotification(
                NotificationModel(
                    userId = user.id,
                    title = "Session Ended",
                    description = "Logged out of ${user.name}.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        _currentUser.value = null
        _activeAttendance.value = null
        showToast("Logged out successfully.")
    }

    fun signUp(name: String, email: String, role: String, phone: String, workZone: String) = viewModelScope.launch {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty() || name.trim().isEmpty() || phone.trim().isEmpty()) {
            showToast("Please fill in all mandatory fields.")
            return@launch
        }
        val existing = repository.getUserByEmail(trimmedEmail)
        if (existing != null) {
            showToast("User with this email already exists.")
            return@launch
        }
        val id = "user_${System.currentTimeMillis()}"
        val newUser = User(
            id = id,
            email = trimmedEmail,
            name = name.trim(),
            role = role.uppercase(), // "ADMIN", "MANAGER", "EXECUTIVE"
            phone = phone.trim(),
            photoUri = when (role.uppercase()) {
                "ADMIN" -> "ic_admin_avatar"
                "MANAGER" -> "ic_manager_avatar"
                else -> "ic_executive_avatar"
            },
            reportingManagerId = if (role.uppercase() == "EXECUTIVE") "manager_1" else if (role.uppercase() == "MANAGER") "admin_1" else null,
            workZoneName = workZone.ifEmpty { "Default Sector" },
            workZoneLat = 37.7749,
            workZoneLng = -122.4194,
            workZoneRadiusMeters = 200.0
        )
        repository.insertUser(newUser)
        loginAs(newUser)
        showToast("Welcome ${newUser.name}! Mode: ${newUser.role} active.")
    }

    fun adminCreateUser(name: String, email: String, role: String, phone: String, workZone: String) = viewModelScope.launch {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty() || name.trim().isEmpty() || phone.trim().isEmpty()) {
            showToast("Failed to provision: Please fill all credentials.")
            return@launch
        }
        val existing = repository.getUserByEmail(trimmedEmail)
        if (existing != null) {
            showToast("User with email $trimmedEmail already exists.")
            return@launch
        }
        val id = "user_${System.currentTimeMillis()}"
        val newUser = User(
            id = id,
            email = trimmedEmail,
            name = name.trim(),
            role = role.uppercase(), // "ADMIN", "MANAGER", "EXECUTIVE"
            phone = phone.trim(),
            photoUri = when (role.uppercase()) {
                "ADMIN" -> "ic_admin_avatar"
                "MANAGER" -> "ic_manager_avatar"
                else -> "ic_executive_avatar"
            },
            reportingManagerId = if (role.uppercase() == "EXECUTIVE") "manager_1" else if (role.uppercase() == "MANAGER") "admin_1" else null,
            workZoneName = workZone.ifEmpty { "Default Sector" },
            workZoneLat = 37.7749,
            workZoneLng = -122.4194,
            workZoneRadiusMeters = 200.0
        )
        repository.insertUser(newUser)
        showToast("Successfully provisioned ${newUser.name} as ${newUser.role}!")
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun toggleOnline() {
        val nextOnline = !_isOnline.value
        _isOnline.value = nextOnline
        
        if (nextOnline) {
            // Trigger automatic sync of offline elements
            syncOfflineData()
        } else {
            showToast("Offline Mode Active. Check-ins, EODs, uploads will be cached safely.")
        }
    }

    private fun syncOfflineData() = viewModelScope.launch {
        try {
            // Process all pending items from the persistent sync queue
            val result = repository.processPendingSyncItems()
            if (result.isSuccess) {
                showToast(result.getOrNull() ?: "Sync complete!")
                // Also refresh data from server
                repository.refreshFromServer()
            } else {
                showToast("Sync failed: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            showToast("Sync error: ${e.message}")
        }
    }

    fun loginWithEmail(email: String) = viewModelScope.launch {
        // First try to fetch from server
        if (_isOnline.value) {
            try {
                repository.fetchUsersFromServer()
            } catch (e: Exception) {
                // Continue with local data if server fails
            }
        }

        val user = repository.getUserByEmail(email.trim().lowercase())
        if (user != null) {
            loginAs(user)
        } else {
            showToast("User not found. Please register or check your email.")
        }
    }

    fun loginWithId(userId: String) = viewModelScope.launch {
        // First try to fetch from server
        if (_isOnline.value) {
            try {
                repository.fetchUsersFromServer()
            } catch (e: Exception) {
                // Continue with local data if server fails
            }
        }

        val user = repository.getUserById(userId)
        if (user != null) {
            loginAs(user)
        } else {
            showToast("User not found. Please register first.")
        }
    }

    fun loginAs(user: User) = viewModelScope.launch {
        _currentUser.value = user
        refreshActiveAttendance(user.id)
        
        // Add a login notice
        repository.insertNotification(
            NotificationModel(
                userId = user.id,
                title = "Session Started",
                description = "Logged in as ${user.name} (${user.role})",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun refreshActiveAttendance(employeeId: String) {
        _activeAttendance.value = repository.getActiveAttendance(employeeId)
    }

    // Attendance actions (Selfie, GPS, Note)
    fun checkIn(selfieUri: String, note: String, lat: Double, lng: Double, isOutsideGeofence: Boolean) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        val attendance = Attendance(
            employeeId = user.id,
            checkInTime = System.currentTimeMillis(),
            checkInLat = lat,
            checkInLng = lng,
            checkInSelfieUri = selfieUri,
            checkInNote = note,
            isOutsideGeofence = isOutsideGeofence,
            isSyncedOffline = _isOnline.value
        )

        if (_isOnline.value) {
            // Try to sync to server first
            val result = repository.syncAttendanceToServer(attendance)
            if (result.isSuccess) {
                showToast("Check-in successful and synced!")
            } else {
                // If sync fails, still save locally and add to sync queue
                repository.insertAttendance(attendance)
                showToast("Check-in saved locally (server sync failed).")
            }
        } else {
            // Offline mode: save locally and enqueue for sync
            repository.insertAttendance(attendance)
            // Enqueue for sync (using a simple JSON representation)
            repository.enqueueForSync("attendance", attendance.id, "create", "{}")
            showToast("Checked in locally (Offline mode).")
        }

        // Send Notification
        repository.insertNotification(
            NotificationModel(
                userId = user.id,
                title = "Check In Complete",
                description = "Started shift at ${note.ifEmpty { "Default Location" }}. Location verified.",
                timestamp = System.currentTimeMillis()
            )
        )

        // Notify reporting manager
        if (user.reportingManagerId != null) {
            repository.insertNotification(
                NotificationModel(
                    userId = user.reportingManagerId,
                    title = "Team Check-In",
                    description = "${user.name} checked in at ${user.workZoneName}${if (isOutsideGeofence) " (GEOFENCE OUTSIDE!)" else ""}",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        refreshActiveAttendance(user.id)
    }

    fun checkOut(note: String, tasksCompleted: Int, expenses: Double) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        val active = _activeAttendance.value ?: return@launch

        val updated = active.copy(
            checkOutTime = System.currentTimeMillis(),
            checkOutNote = note,
            checkOutTasksCompleted = tasksCompleted,
            checkOutExpenses = expenses,
            isSyncedOffline = _isOnline.value
        )

        if (_isOnline.value) {
            // Try to sync to server first
            val result = repository.syncAttendanceToServer(updated)
            if (result.isSuccess) {
                showToast("Check-out successful and synced!")
            } else {
                // If sync fails, still save locally
                repository.updateAttendance(updated)
                showToast("Check-out saved locally (server sync failed).")
            }
        } else {
            // Offline mode: save locally and enqueue for sync
            repository.updateAttendance(updated)
            repository.enqueueForSync("attendance", updated.id, "update", "{}")
            showToast("Checked out locally (Offline mode saved).")
        }

        // Notify self & manager
        repository.insertNotification(
            NotificationModel(
                userId = user.id,
                title = "Shift Completed",
                description = "Checked out. Submitted expenses: $$expenses.",
                timestamp = System.currentTimeMillis()
            )
        )

        if (user.reportingManagerId != null) {
            repository.insertNotification(
                NotificationModel(
                    userId = user.reportingManagerId,
                    title = "Team Check-Out",
                    description = "${user.name} checked out. Completed: $tasksCompleted tasks. Note: $note",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        _activeAttendance.value = null
    }

    // Tasks and updates
    fun assignTask(title: String, description: String, priority: String, dueDate: Long, address: String, execId: String) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        val newTask = Task(
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            locationAddress = address,
            status = "PENDING",
            assignedTo = execId,
            assignedByName = user.name
        )

        repository.insertTask(newTask)
        showToast("Task assigned to Executive successfully.")

        // Notify employee
        repository.insertNotification(
            NotificationModel(
                userId = execId,
                title = "New Task Assigned",
                description = "\"$title\" - Priority: $priority. Due: ${formatDate(dueDate)}",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun updateTask(task: Task, newStatus: String, proofPhoto: String? = null, signature: String? = null) = viewModelScope.launch {
        val updated = task.copy(
            status = newStatus,
            proofPhotoUri = proofPhoto ?: task.proofPhotoUri,
            proofSignatureBase64 = signature ?: task.proofSignatureBase64,
            actualStart = if (newStatus == "IN_PROGRESS" && task.actualStart == null) System.currentTimeMillis() else task.actualStart,
            actualEnd = if (newStatus == "COMPLETED" || newStatus == "REJECTED") System.currentTimeMillis() else task.actualEnd
        )

        if (_isOnline.value) {
            // Try to sync to server first
            val result = repository.syncTaskToServer(updated)
            if (result.isSuccess) {
                showToast("Task status synced to server: $newStatus.")
            } else {
                // If sync fails, still save locally
                repository.updateTask(updated)
                showToast("Task updated locally (server sync failed).")
            }
        } else {
            // Offline mode: save locally and enqueue for sync
            repository.updateTask(updated)
            repository.enqueueForSync("task", updated.id, "update", "{}")
            showToast("Task status updated locally (Offline Queue).")
        }

        // Notify reporting managers
        val user = _currentUser.value ?: return@launch
        if (user.reportingManagerId != null) {
            repository.insertNotification(
                NotificationModel(
                    userId = user.reportingManagerId,
                    title = "Task Updated",
                    description = "${user.name} marked task \"${task.title}\" as $newStatus.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Customer visits check-in and customer checks
    fun startVisit(customerName: String, address: String, lat: Double, lng: Double, selfieUri: String?) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        val visit = Visit(
            executiveId = user.id,
            customerName = customerName,
            address = address,
            checkInTime = System.currentTimeMillis(),
            latitude = lat,
            longitude = lng,
            photoUri = selfieUri
        )

        if (_isOnline.value) {
            // Try to sync to server first
            val result = repository.syncVisitToServer(visit)
            if (result.isSuccess) {
                showToast("Customer visit started and synced!")
            } else {
                // If sync fails, still save locally
                repository.insertVisit(visit)
                showToast("Visit started locally (server sync failed).")
            }
        } else {
            // Offline mode: save locally and enqueue for sync
            repository.insertVisit(visit)
            repository.enqueueForSync("visit", visit.id, "create", "{}")
            showToast("Started customer visit locally (Offline).")
        }

        repository.insertNotification(
            NotificationModel(
                userId = user.id,
                title = "Visit Started",
                description = "Checked in at customer $customerName.",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun endVisit(visit: Visit, notes: String, signatureBase64: String?, photoUri: String?) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        // Generate a simulated report PDF name
        val reportName = "VisitReport_${visit.customerName.replace(" ", "_")}_${System.currentTimeMillis() % 10000}.pdf"

        val updated = visit.copy(
            checkOutTime = System.currentTimeMillis(),
            notes = notes,
            signatureBase64 = signatureBase64,
            photoUri = photoUri ?: visit.photoUri,
            reportPdfName = reportName
        )

        if (_isOnline.value) {
            // Try to sync to server first
            val result = repository.syncVisitToServer(updated)
            if (result.isSuccess) {
                showToast("Visit completed and synced! PDF Report saved.")
            } else {
                // If sync fails, still save locally
                repository.updateVisit(updated)
                showToast("Visit ended locally (server sync failed).")
            }
        } else {
            // Offline mode: save locally and enqueue for sync
            repository.updateVisit(updated)
            repository.enqueueForSync("visit", updated.id, "update", "{}")
            showToast("Visit ended locally (Report compiled).")
        }

        repository.insertNotification(
            NotificationModel(
                userId = user.id,
                title = "Visit Completed",
                description = "Visit to ${visit.customerName} wrapped up. $reportName generated.",
                timestamp = System.currentTimeMillis()
            )
        )

        if (user.reportingManagerId != null) {
            repository.insertNotification(
                NotificationModel(
                    userId = user.reportingManagerId,
                    title = "Visit Completed",
                    description = "${user.name} completed visit to ${visit.customerName}. Summary: $notes",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Document & bill filing tracker
    fun fileDocument(fileName: String, category: String, amount: Double?, tags: String, fileUri: String) = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        val record = FileRecord(
            fileName = fileName,
            category = category,
            fileUri = fileUri,
            uploadedBy = user.id,
            uploadedByName = user.name,
            timestamp = System.currentTimeMillis(),
            tags = tags,
            amount = amount,
            status = "PENDING"
        )

        if (_isOnline.value) {
            // Try to sync to server first
            val result = repository.syncFileRecordToServer(record)
            if (result.isSuccess) {
                showToast("Document filed and synced successfully!")
            } else {
                // If sync fails, still save locally
                repository.insertFileRecord(record)
                showToast("Document saved locally (server sync failed).")
            }
        } else {
            // Offline mode: save locally and enqueue for sync
            repository.insertFileRecord(record)
            repository.enqueueForSync("file", record.id, "create", "{}")
            showToast("Document saved locally (Sync on network restoral).")
        }

        repository.insertNotification(
            NotificationModel(
                userId = user.id,
                title = "Document Submitted",
                description = "Expense/Bill \"$fileName\" submitted for approval.",
                timestamp = System.currentTimeMillis()
            )
        )

        if (user.reportingManagerId != null) {
            repository.insertNotification(
                NotificationModel(
                    userId = user.reportingManagerId,
                    title = "Document Submitted",
                    description = "${user.name} submitted $category: \"$fileName\".",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Helper function to format timestamps
    fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    fun updateFileStatus(record: FileRecord, status: String, reason: String? = null) = viewModelScope.launch {
        val updated = record.copy(
            status = status,
            rejectionReason = reason
        )
        repository.updateFileRecord(updated)
        showToast("Document status set to $status.")

        // Notify uploader
        repository.insertNotification(
            NotificationModel(
                userId = record.uploadedBy,
                title = "File Status Update",
                description = "Your file \"${record.fileName}\" has been $status${if (reason != null) ": $reason" else ""}.",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun deleteFileRecord(id: Int) = viewModelScope.launch {
        repository.deleteFileRecord(id)
        showToast("File deleted.")
    }

    fun clearAllNotifications() = viewModelScope.launch {
        val user = _currentUser.value ?: return@launch
        repository.markAllAsRead(user.id)
        showToast("All notifications marked as read.")
    }
}
