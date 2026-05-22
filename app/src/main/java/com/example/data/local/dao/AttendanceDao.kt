package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for AttendanceEntity operations.
 * Provides methods for querying and manipulating attendance records.
 */
@Dao
interface AttendanceDao {
    /**
     * Returns a Flow emitting all attendance records for a specific employee,
     * ordered by check-in time (newest first).
     *
     * @param employeeId The employee's user ID
     * @return Flow emitting list of attendance records
     */
    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId ORDER BY checkInTime DESC")
    fun getAttendanceForEmployee(employeeId: String): Flow<List<AttendanceEntity>>

    /**
     * Returns a Flow emitting all attendance records in the system,
     * ordered by check-in time (newest first).
     *
     * @return Flow emitting list of all attendance records
     */
    @Query("SELECT * FROM attendance ORDER BY checkInTime DESC")
    fun getAllAttendanceFlow(): Flow<List<AttendanceEntity>>

    /**
     * Retrieves all attendance records synchronously.
     *
     * @return List of all attendance records
     */
    @Query("SELECT * FROM attendance ORDER BY checkInTime DESC")
    suspend fun getAllAttendance(): List<AttendanceEntity>

    /**
     * Retrieves the active (currently checked in) attendance record for an employee.
     *
     * @param employeeId The employee's user ID
     * @return The active attendance record if found, null otherwise
     */
    @Query("""
        SELECT * FROM attendance
        WHERE employeeId = :employeeId AND checkOutTime IS NULL
        ORDER BY checkInTime DESC
        LIMIT 1
    """)
    suspend fun getActiveAttendance(employeeId: String): AttendanceEntity?

    /**
     * Returns a Flow emitting the active attendance record for an employee.
     *
     * @param employeeId The employee's user ID
     * @return Flow emitting the active attendance record or null
     */
    @Query("""
        SELECT * FROM attendance
        WHERE employeeId = :employeeId AND checkOutTime IS NULL
        ORDER BY checkInTime DESC
        LIMIT 1
    """)
    fun getActiveAttendanceFlow(employeeId: String): Flow<AttendanceEntity?>

    /**
     * Inserts a new attendance record.
     *
     * @param attendance The attendance record to insert
     * @return The row ID of the inserted record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    /**
     * Updates an existing attendance record.
     *
     * @param attendance The attendance record with updated values
     */
    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    /**
     * Deletes all attendance records for a specific employee.
     *
     * @param employeeId The employee's user ID
     */
    @Query("DELETE FROM attendance WHERE employeeId = :employeeId")
    suspend fun deleteByEmployeeId(employeeId: String)

    /**
     * Deletes a specific attendance record by ID.
     *
     * @param attendanceId The attendance record ID
     */
    @Query("DELETE FROM attendance WHERE id = :attendanceId")
    suspend fun deleteById(attendanceId: Int)

    /**
     * Retrieves attendance records for a specific date range.
     *
     * @param employeeId The employee's user ID
     * @param startTime Start of the date range (timestamp)
     * @param endTime End of the date range (timestamp)
     * @return List of attendance records within the range
     */
    @Query("""
        SELECT * FROM attendance
        WHERE employeeId = :employeeId
        AND checkInTime BETWEEN :startTime AND :endTime
        ORDER BY checkInTime DESC
    """)
    suspend fun getAttendanceForDateRange(
        employeeId: String,
        startTime: Long,
        endTime: Long
    ): List<AttendanceEntity>

    /**
     * Counts the total number of check-ins for an employee.
     *
     * @param employeeId The employee's user ID
     * @return The count of attendance records
     */
    @Query("SELECT COUNT(*) FROM attendance WHERE employeeId = :employeeId")
    suspend fun getCountForEmployee(employeeId: String): Int
}
