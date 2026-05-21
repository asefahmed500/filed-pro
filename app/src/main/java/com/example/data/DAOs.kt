package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE role = 'EXECUTIVE'")
    fun getExecutivesFlow(): Flow<List<User>>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = 'EXECUTIVE'")
    suspend fun getExecutives(): List<User>

    @Query("SELECT * FROM users WHERE reportingManagerId = :managerId")
    fun getTeamMembersFlow(managerId: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId ORDER BY checkInTime DESC")
    fun getAttendanceForEmployee(employeeId: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance ORDER BY checkInTime DESC")
    fun getAllAttendanceFlow(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance ORDER BY checkInTime DESC")
    suspend fun getAllAttendance(): List<Attendance>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId AND checkOutTime IS NULL ORDER BY checkInTime DESC LIMIT 1")
    suspend fun getActiveAttendance(employeeId: String): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Update
    suspend fun updateAttendance(attendance: Attendance)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE assignedTo = :employeeId ORDER BY dueDate ASC")
    fun getTasksForEmployee(employeeId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    suspend fun getAllTasks(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)
}

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits WHERE executiveId = :employeeId ORDER BY checkInTime DESC")
    fun getVisitsForEmployee(employeeId: String): Flow<List<Visit>>

    @Query("SELECT * FROM visits ORDER BY checkInTime DESC")
    fun getAllVisitsFlow(): Flow<List<Visit>>

    @Query("SELECT * FROM visits ORDER BY checkInTime DESC")
    suspend fun getAllVisits(): List<Visit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: Visit)

    @Update
    suspend fun updateVisit(visit: Visit)
}

@Dao
interface FileRecordDao {
    @Query("SELECT * FROM file_records ORDER BY timestamp DESC")
    fun getAllFilesFlow(): Flow<List<FileRecord>>

    @Query("SELECT * FROM file_records WHERE uploadedBy = :employeeId ORDER BY timestamp DESC")
    fun getFilesForEmployee(employeeId: String): Flow<List<FileRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFileRecord(record: FileRecord)

    @Update
    suspend fun updateFileRecord(record: FileRecord)

    @Query("DELETE FROM file_records WHERE id = :fileId")
    suspend fun deleteFileRecord(fileId: Int)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationModel)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)
}
