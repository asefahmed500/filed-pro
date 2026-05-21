package com.example.data

import kotlinx.coroutines.flow.Flow

class FieldForceRepository(private val db: FieldForceDatabase) {
    private val userDao = db.userDao()
    private val attendanceDao = db.attendanceDao()
    private val taskDao = db.taskDao()
    private val visitDao = db.visitDao()
    private val fileRecordDao = db.fileRecordDao()
    private val notificationDao = db.notificationDao()

    // Users
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun getUserById(userId: String): User? = userDao.getUserById(userId)
    fun getExecutivesFlow(): Flow<List<User>> = userDao.getExecutivesFlow()
    fun getAllUsersFlow(): Flow<List<User>> = userDao.getAllUsersFlow()
    fun getTeamMembersFlow(managerId: String): Flow<List<User>> = userDao.getTeamMembersFlow(managerId)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun getExecutives(): List<User> = userDao.getExecutives()

    // Attendance
    fun getAttendanceForEmployee(employeeId: String): Flow<List<Attendance>> = attendanceDao.getAttendanceForEmployee(employeeId)
    fun getAllAttendanceFlow(): Flow<List<Attendance>> = attendanceDao.getAllAttendanceFlow()
    suspend fun getAllAttendance(): List<Attendance> = attendanceDao.getAllAttendance()
    suspend fun getActiveAttendance(employeeId: String): Attendance? = attendanceDao.getActiveAttendance(employeeId)
    suspend fun insertAttendance(attendance: Attendance) = attendanceDao.insertAttendance(attendance)
    suspend fun updateAttendance(attendance: Attendance) = attendanceDao.updateAttendance(attendance)

    // Tasks
    fun getTasksForEmployee(employeeId: String): Flow<List<Task>> = taskDao.getTasksForEmployee(employeeId)
    fun getAllTasksFlow(): Flow<List<Task>> = taskDao.getAllTasksFlow()
    suspend fun getAllTasks(): List<Task> = taskDao.getAllTasks()
    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTaskById(taskId: Int) = taskDao.deleteTaskById(taskId)

    // Visits
    fun getVisitsForEmployee(employeeId: String): Flow<List<Visit>> = visitDao.getVisitsForEmployee(employeeId)
    fun getAllVisitsFlow(): Flow<List<Visit>> = visitDao.getAllVisitsFlow()
    suspend fun getAllVisits(): List<Visit> = visitDao.getAllVisits()
    suspend fun insertVisit(visit: Visit) = visitDao.insertVisit(visit)
    suspend fun updateVisit(visit: Visit) = visitDao.updateVisit(visit)

    // File Records (expenses, files, reimbursement, POD)
    fun getAllFilesFlow(): Flow<List<FileRecord>> = fileRecordDao.getAllFilesFlow()
    fun getFilesForEmployee(employeeId: String): Flow<List<FileRecord>> = fileRecordDao.getFilesForEmployee(employeeId)
    suspend fun insertFileRecord(record: FileRecord) = fileRecordDao.insertFileRecord(record)
    suspend fun updateFileRecord(record: FileRecord) = fileRecordDao.updateFileRecord(record)
    suspend fun deleteFileRecord(fileId: Int) = fileRecordDao.deleteFileRecord(fileId)

    // Notifications
    fun getNotificationsForUser(userId: String): Flow<List<NotificationModel>> = notificationDao.getNotificationsForUser(userId)
    suspend fun insertNotification(notification: NotificationModel) = notificationDao.insertNotification(notification)
    suspend fun markAllAsRead(userId: String) = notificationDao.markAllAsRead(userId)
    suspend fun markAsRead(id: Int) = notificationDao.markAsRead(id)
}
