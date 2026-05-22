package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FileRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for FileRecordEntity operations.
 * Provides methods for querying and manipulating file/expense records.
 */
@Dao
interface FileRecordDao {
    /**
     * Returns a Flow emitting all file records in the system,
     * ordered by timestamp (newest first).
     *
     * @return Flow emitting list of all file records
     */
    @Query("SELECT * FROM file_records ORDER BY timestamp DESC")
    fun getAllFilesFlow(): Flow<List<FileRecordEntity>>

    /**
     * Returns a Flow emitting file records for a specific employee,
     * ordered by timestamp (newest first).
     *
     * @param employeeId The employee's user ID
     * @return Flow emitting list of file records
     */
    @Query("SELECT * FROM file_records WHERE uploadedBy = :employeeId ORDER BY timestamp DESC")
    fun getFilesForEmployee(employeeId: String): Flow<List<FileRecordEntity>>

    /**
     * Retrieves all file records synchronously.
     *
     * @return List of all file records
     */
    @Query("SELECT * FROM file_records ORDER BY timestamp DESC")
    suspend fun getAllFiles(): List<FileRecordEntity>

    /**
     * Inserts a new file record.
     *
     * @param record The file record to insert
     * @return The row ID of the inserted record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFileRecord(record: FileRecordEntity): Long

    /**
     * Updates an existing file record.
     *
     * @param record The file record with updated values
     */
    @Update
    suspend fun updateFileRecord(record: FileRecordEntity)

    /**
     * Deletes a file record by ID.
     *
     * @param fileId The file record ID to delete
     */
    @Query("DELETE FROM file_records WHERE id = :fileId")
    suspend fun deleteFileRecord(fileId: Int)

    /**
     * Retrieves file records by category.
     *
     * @param category The category to filter by
     * @return List of file records in the category
     */
    @Query("SELECT * FROM file_records WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getFilesByCategory(category: String): List<FileRecordEntity>

    /**
     * Retrieves file records by status.
     *
     * @param status The status to filter by
     * @return List of file records with the specified status
     */
    @Query("SELECT * FROM file_records WHERE status = :status ORDER BY timestamp DESC")
    suspend fun getFilesByStatus(status: String): List<FileRecordEntity>

    /**
     * Returns a Flow emitting pending file records (awaiting approval).
     *
     * @return Flow emitting list of pending file records
     */
    @Query("SELECT * FROM file_records WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingFilesFlow(): Flow<List<FileRecordEntity>>

    /**
     * Counts file records by status for a specific employee.
     *
     * @param employeeId The employee's user ID
     * @param status The status to count
     * @return The count of file records with the specified status
     */
    @Query("""
        SELECT COUNT(*) FROM file_records
        WHERE uploadedBy = :employeeId AND status = :status
    """)
    suspend fun countFilesByStatusForEmployee(employeeId: String, status: String): Int

    /**
     * Searches file records by tags.
     *
     * @param searchTag The tag to search for
     * @return List of file records containing the tag
     */
    @Query("""
        SELECT * FROM file_records
        WHERE tags LIKE '%' || :searchTag || '%'
        ORDER BY timestamp DESC
    """)
    suspend fun searchByTag(searchTag: String): List<FileRecordEntity>

    /**
     * Retrieves file records for a specific date range.
     *
     * @param startTime Start of the date range (timestamp)
     * @param endTime End of the date range (timestamp)
     * @return List of file records within the range
     */
    @Query("""
        SELECT * FROM file_records
        WHERE timestamp BETWEEN :startTime AND :endTime
        ORDER BY timestamp DESC
    """)
    suspend fun getFilesForDateRange(startTime: Long, endTime: Long): List<FileRecordEntity>

    /**
     * Calculates total amount of approved expense claims.
     *
     * @return The total amount of all approved expenses
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM file_records
        WHERE category = 'EXPENSE' AND status = 'APPROVED'
    """)
    suspend fun getTotalApprovedExpenses(): Double
}
