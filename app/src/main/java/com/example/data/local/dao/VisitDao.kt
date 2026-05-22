package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.VisitEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for VisitEntity operations.
 * Provides methods for querying and manipulating customer visit records.
 */
@Dao
interface VisitDao {
    /**
     * Returns a Flow emitting all visits for a specific executive,
     * ordered by check-in time (newest first).
     *
     * @param employeeId The executive's user ID
     * @return Flow emitting list of visits
     */
    @Query("SELECT * FROM visits WHERE executiveId = :employeeId ORDER BY checkInTime DESC")
    fun getVisitsForEmployee(employeeId: String): Flow<List<VisitEntity>>

    /**
     * Returns a Flow emitting all visits in the system,
     * ordered by check-in time (newest first).
     *
     * @return Flow emitting list of all visits
     */
    @Query("SELECT * FROM visits ORDER BY checkInTime DESC")
    fun getAllVisitsFlow(): Flow<List<VisitEntity>>

    /**
     * Retrieves all visits synchronously.
     *
     * @return List of all visits
     */
    @Query("SELECT * FROM visits ORDER BY checkInTime DESC")
    suspend fun getAllVisits(): List<VisitEntity>

    /**
     * Inserts a new visit record.
     *
     * @param visit The visit to insert
     * @return The row ID of the inserted visit
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitEntity): Long

    /**
     * Updates an existing visit record.
     *
     * @param visit The visit with updated values
     */
    @Update
    suspend fun updateVisit(visit: VisitEntity)

    /**
     * Retrieves the active (currently in progress) visit for an executive.
     *
     * @param employeeId The executive's user ID
     * @return The active visit if found, null otherwise
     */
    @Query("""
        SELECT * FROM visits
        WHERE executiveId = :employeeId AND checkOutTime IS NULL
        ORDER BY checkInTime DESC
        LIMIT 1
    """)
    suspend fun getActiveVisit(employeeId: String): VisitEntity?

    /**
     * Returns a Flow emitting the active visit for an executive.
     *
     * @param employeeId The executive's user ID
     * @return Flow emitting the active visit or null
     */
    @Query("""
        SELECT * FROM visits
        WHERE executiveId = :employeeId AND checkOutTime IS NULL
        ORDER BY checkInTime DESC
        LIMIT 1
    """)
    fun getActiveVisitFlow(employeeId: String): Flow<VisitEntity?>

    /**
     * Deletes all visits for a specific executive.
     *
     * @param employeeId The executive's user ID
     */
    @Query("DELETE FROM visits WHERE executiveId = :employeeId")
    suspend fun deleteByEmployeeId(employeeId: String)

    /**
     * Deletes a specific visit by ID.
     *
     * @param visitId The visit ID to delete
     */
    @Query("DELETE FROM visits WHERE id = :visitId")
    suspend fun deleteById(visitId: Int)

    /**
     * Retrieves visits for a specific date range.
     *
     * @param employeeId The executive's user ID
     * @param startTime Start of the date range (timestamp)
     * @param endTime End of the date range (timestamp)
     * @return List of visits within the range
     */
    @Query("""
        SELECT * FROM visits
        WHERE executiveId = :employeeId
        AND checkInTime BETWEEN :startTime AND :endTime
        ORDER BY checkInTime DESC
    """)
    suspend fun getVisitsForDateRange(
        employeeId: String,
        startTime: Long,
        endTime: Long
    ): List<VisitEntity>

    /**
     * Counts completed visits for an executive.
     *
     * @param employeeId The executive's user ID
     * @return The count of completed visits
     */
    @Query("""
        SELECT COUNT(*) FROM visits
        WHERE executiveId = :employeeId AND checkOutTime IS NOT NULL
    """)
    suspend fun countCompletedVisits(employeeId: String): Int

    /**
     * Searches visits by customer name.
     *
     * @param employeeId The executive's user ID
     * @param searchQuery The search query (partial match)
     * @return List of matching visits
     */
    @Query("""
        SELECT * FROM visits
        WHERE executiveId = :employeeId
        AND customerName LIKE '%' || :searchQuery || '%'
        ORDER BY checkInTime DESC
    """)
    suspend fun searchByCustomerName(employeeId: String, searchQuery: String): List<VisitEntity>
}
