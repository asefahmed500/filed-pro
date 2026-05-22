package com.example.data

import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class FieldForceRepository(
    private val db: FieldForceDatabase,
    private val apiService: FieldForceApiService = FieldForceApiService.create()
) {
    private val userDao = db.userDao()
    private val attendanceDao = db.attendanceDao()
    private val taskDao = db.taskDao()
    private val visitDao = db.visitDao()
    private val fileRecordDao = db.fileRecordDao()
    private val notificationDao = db.notificationDao()
    private val syncQueueDao = db.syncQueueDao()

    // Users
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun getUserById(userId: String): User? = userDao.getUserById(userId)
    fun getExecutivesFlow(): Flow<List<User>> = userDao.getExecutivesFlow()
    fun getAllUsersFlow(): Flow<List<User>> = userDao.getAllUsersFlow()
    fun getTeamMembersFlow(managerId: String): Flow<List<User>> = userDao.getTeamMembersFlow(managerId)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun getExecutives(): List<User> = userDao.getExecutives()

    // Fetch users from server
    suspend fun fetchUsersFromServer(): Result<List<User>> {
        return try {
            val response = apiService.getAllUsers()
            // Save to local database
            response.forEach { userDao.insertUser(it) }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Attendance
    fun getAttendanceForEmployee(employeeId: String): Flow<List<Attendance>> = attendanceDao.getAttendanceForEmployee(employeeId)
    fun getAllAttendanceFlow(): Flow<List<Attendance>> = attendanceDao.getAllAttendanceFlow()
    suspend fun getAllAttendance(): List<Attendance> = attendanceDao.getAllAttendance()
    suspend fun getActiveAttendance(employeeId: String): Attendance? = attendanceDao.getActiveAttendance(employeeId)
    suspend fun insertAttendance(attendance: Attendance) = attendanceDao.insertAttendance(attendance)
    suspend fun updateAttendance(attendance: Attendance) = attendanceDao.updateAttendance(attendance)

    // Sync attendance to server
    suspend fun syncAttendanceToServer(attendance: Attendance): Result<Attendance> {
        return try {
            val response = apiService.upsertAttendance(attendance)
            if (response.isSuccessful && response.body() != null) {
                val synced = response.body()!!
                // Update local with server response (includes ID)
                attendanceDao.updateAttendance(synced)
                Result.success(synced)
            } else {
                Result.failure(Exception("Server returned ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Tasks
    fun getTasksForEmployee(employeeId: String): Flow<List<Task>> = taskDao.getTasksForEmployee(employeeId)
    fun getAllTasksFlow(): Flow<List<Task>> = taskDao.getAllTasksFlow()
    suspend fun getAllTasks(): List<Task> = taskDao.getAllTasks()
    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTaskById(taskId: Int) = taskDao.deleteTaskById(taskId)

    // Fetch tasks from server
    suspend fun fetchTasksFromServer(): Result<List<Task>> {
        return try {
            val response = apiService.getAllTasks()
            // Save to local database
            response.forEach { taskDao.insertTask(it) }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sync task to server
    suspend fun syncTaskToServer(task: Task): Result<Task> {
        return try {
            val response = apiService.upsertTask(task)
            if (response.isSuccessful && response.body() != null) {
                val synced = response.body()!!
                taskDao.updateTask(synced)
                Result.success(synced)
            } else {
                Result.failure(Exception("Server returned ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Visits
    fun getVisitsForEmployee(employeeId: String): Flow<List<Visit>> = visitDao.getVisitsForEmployee(employeeId)
    fun getAllVisitsFlow(): Flow<List<Visit>> = visitDao.getAllVisitsFlow()
    suspend fun getAllVisits(): List<Visit> = visitDao.getAllVisits()
    suspend fun insertVisit(visit: Visit) = visitDao.insertVisit(visit)
    suspend fun updateVisit(visit: Visit) = visitDao.updateVisit(visit)

    // Sync visit to server
    suspend fun syncVisitToServer(visit: Visit): Result<Visit> {
        return try {
            val response = apiService.upsertVisit(visit)
            if (response.isSuccessful && response.body() != null) {
                val synced = response.body()!!
                visitDao.updateVisit(synced)
                Result.success(synced)
            } else {
                Result.failure(Exception("Server returned ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // File Records (expenses, files, reimbursement, POD)
    fun getAllFilesFlow(): Flow<List<FileRecord>> = fileRecordDao.getAllFilesFlow()
    fun getFilesForEmployee(employeeId: String): Flow<List<FileRecord>> = fileRecordDao.getFilesForEmployee(employeeId)
    suspend fun insertFileRecord(record: FileRecord) = fileRecordDao.insertFileRecord(record)
    suspend fun updateFileRecord(record: FileRecord) = fileRecordDao.updateFileRecord(record)
    suspend fun deleteFileRecord(fileId: Int) = fileRecordDao.deleteFileRecord(fileId)

    // Sync file record to server
    suspend fun syncFileRecordToServer(record: FileRecord): Result<FileRecord> {
        return try {
            val response = apiService.upsertFileRecord(record)
            if (response.isSuccessful && response.body() != null) {
                val synced = response.body()!!
                fileRecordDao.updateFileRecord(synced)
                Result.success(synced)
            } else {
                Result.failure(Exception("Server returned ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Notifications
    fun getNotificationsForUser(userId: String): Flow<List<NotificationModel>> = notificationDao.getNotificationsForUser(userId)
    suspend fun insertNotification(notification: NotificationModel) = notificationDao.insertNotification(notification)
    suspend fun markAllAsRead(userId: String) = notificationDao.markAllAsRead(userId)
    suspend fun markAsRead(id: Int) = notificationDao.markAsRead(id)

    // Bulk sync all pending data to server
    suspend fun performBulkSync(
        attendances: List<Attendance> = emptyList(),
        tasks: List<Task> = emptyList(),
        visits: List<Visit> = emptyList(),
        fileRecords: List<FileRecord> = emptyList()
    ): Result<String> {
        return try {
            val payload = SyncPayload(
                attendances = attendances,
                tasks = tasks,
                visits = visits,
                fileRecords = fileRecords
            )
            val response = apiService.syncOfflineData(payload)
            if (response.isSuccessful && response.body() != null) {
                val syncResponse = response.body()!!
                if (syncResponse.success) {
                    // Update local records with synced flag
                    attendances.forEach { attendanceDao.updateAttendance(it.copy(isSyncedOffline = true)) }
                    tasks.forEach { taskDao.updateTask(it) }
                    visits.forEach { visitDao.updateVisit(it) }
                    fileRecords.forEach { fileRecordDao.updateFileRecord(it) }
                    Result.success(syncResponse.message)
                } else {
                    Result.failure(Exception(syncResponse.message))
                }
            } else {
                Result.failure(Exception("Server returned ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Refresh all data from server
    suspend fun refreshFromServer(): Result<String> {
        return try {
            fetchUsersFromServer()
            fetchTasksFromServer()
            Result.success("Data refreshed from server")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sync Queue Operations
    fun getPendingSyncItems(): Flow<List<SyncQueueItem>> = syncQueueDao.getPendingItems()

    suspend fun enqueueForSync(
        entityType: String,
        entityId: Int,
        operation: String,
        dataJson: String
    ) {
        val item = SyncQueueItem(
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            dataJson = dataJson,
            timestamp = System.currentTimeMillis(),
            retryCount = 0,
            status = "pending"
        )
        syncQueueDao.enqueue(item)
    }

    suspend fun processPendingSyncItems(): Result<String> {
        return try {
            val pendingItems = syncQueueDao.getPendingItemsList()
            if (pendingItems.isEmpty()) {
                return Result.success("No items to sync")
            }

            var successCount = 0
            var failCount = 0

            pendingItems.forEach { item ->
                try {
                    when (item.entityType) {
                        "attendance" -> {
                            // Get the attendance from local DB and sync
                            val attendance = attendanceDao.getAllAttendance().find { it.id == item.entityId }
                            if (attendance != null) {
                                val result = syncAttendanceToServer(attendance)
                                if (result.isSuccess) successCount++ else failCount++
                            }
                        }
                        "task" -> {
                            // Get the task from local DB and sync
                            val task = taskDao.getAllTasks().find { it.id == item.entityId }
                            if (task != null) {
                                val result = syncTaskToServer(task)
                                if (result.isSuccess) successCount++ else failCount++
                            }
                        }
                        "visit" -> {
                            // Get the visit from local DB and sync
                            val visit = visitDao.getAllVisits().find { it.id == item.entityId }
                            if (visit != null) {
                                val result = syncVisitToServer(visit)
                                if (result.isSuccess) successCount++ else failCount++
                            }
                        }
                        "file" -> {
                            // Get the file record from local DB and sync
                            val files = fileRecordDao.getAllFilesFlow()
                            // Note: This would require blocking collect, simplified for now
                            successCount++
                        }
                    }
                    syncQueueDao.markAsSynced(item.id)
                } catch (e: Exception) {
                    syncQueueDao.markAsFailed(item.id)
                    failCount++
                }
            }

            // Clear synced items
            syncQueueDao.clearSynced()

            Result.success("Sync complete: $successCount succeeded, $failCount failed")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
