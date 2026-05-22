package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SyncQueueEntity operations.
 * Provides methods for querying and manipulating offline sync queue items.
 */
@Dao
interface SyncQueueDao {
    /**
     * Returns a Flow emitting all pending sync items.
     *
     * @return Flow emitting list of pending items
     */
    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY timestamp ASC")
    fun getPendingItems(): Flow<List<SyncQueueEntity>>

    /**
     * Retrieves all pending items synchronously.
     * Used for batch processing during sync operations.
     *
     * @return List of all pending items
     */
    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY timestamp ASC")
    suspend fun getPendingItemsList(): List<SyncQueueEntity>>

    /**
     * Retrieves pending items for a specific entity type.
     *
     * @param entityType The entity type to filter by
     * @return List of pending items for the entity type
     */
    @Query("""
        SELECT * FROM sync_queue
        WHERE entityType = :entityType AND status = 'pending'
        ORDER BY timestamp ASC
    """)
    suspend fun getPendingItemsByType(entityType: String): List<SyncQueueEntity>

    /**
     * Returns a Flow emitting all sync items regardless of status.
     *
     * @return Flow emitting list of all sync items
     */
    @Query("SELECT * FROM sync_queue ORDER BY timestamp DESC")
    fun getAllItemsFlow(): Flow<List<SyncQueueEntity>>

    /**
     * Inserts a new item into the sync queue.
     *
     * @param item The sync queue item to insert
     * @return The row ID of the inserted item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity): Long

    /**
     * Updates an existing sync queue item.
     *
     * @param item The item with updated values
     */
    @Update
    suspend fun update(item: SyncQueueEntity)

    /**
     * Marks an item as successfully synced.
     *
     * @param itemId The item ID to mark
     * @return The number of items updated
     */
    @Query("UPDATE sync_queue SET status = 'success' WHERE id = :itemId")
    suspend fun markAsSynced(itemId: Int): Int

    /**
     * Marks an item as failed.
     *
     * @param itemId The item ID to mark
     * @return The number of items updated
     */
    @Query("UPDATE sync_queue SET status = 'failed' WHERE id = :itemId")
    suspend fun markAsFailed(itemId: Int): Int

    /**
     * Marks an item as currently syncing.
     *
     * @param itemId The item ID to mark
     * @return The number of items updated
     */
    @Query("UPDATE sync_queue SET status = 'syncing' WHERE id = :itemId")
    suspend fun markAsSyncing(itemId: Int): Int

    /**
     * Resets failed items back to pending status.
     * Useful for retry operations.
     *
     * @return The number of items reset
     */
    @Query("UPDATE sync_queue SET status = 'pending' WHERE status = 'failed'")
    suspend fun resetFailedItems(): Int

    /**
     * Deletes all successfully synced items.
     *
     * @return The number of items deleted
     */
    @Query("DELETE FROM sync_queue WHERE status = 'success'")
    suspend fun clearSynced(): Int

    /**
     * Deletes a specific item by ID.
     *
     * @param itemId The item ID to delete
     */
    @Query("DELETE FROM sync_queue WHERE id = :itemId")
    suspend fun deleteById(itemId: Int)

    /**
     * Deletes all items for a specific entity.
     *
     * @param entityType The entity type
     * @param entityId The entity ID
     * @return The number of items deleted
     */
    @Query("""
        DELETE FROM sync_queue
        WHERE entityType = :entityType AND entityId = :entityId
    """)
    suspend fun deleteForEntity(entityType: String, entityId: Int): Int

    /**
     * Counts pending items by status.
     *
     * @param status The status to count
     * @return The count of items with the specified status
     */
    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = :status")
    suspend fun countByStatus(status: String): Int

    /**
     * Retrieves items that have exceeded max retry attempts.
     *
     * @param maxRetries Maximum allowed retries
     * @return List of items that have exceeded retries
     */
    @Query("""
        SELECT * FROM sync_queue
        WHERE retryCount >= :maxRetries AND status = 'failed'
        ORDER BY timestamp DESC
    """)
    suspend fun getExceededRetriesItems(maxRetries: Int = 3): List<SyncQueueEntity>

    /**
     * Deletes old items beyond a certain timestamp.
     * Useful for cleanup operations.
     *
     * @param beforeTimestamp Delete items older than this
     * @return The number of items deleted
     */
    @Query("DELETE FROM sync_queue WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldItems(beforeTimestamp: Long): Int

    /**
     * Gets the oldest pending item timestamp.
     *
     * @return The timestamp of the oldest pending item, or null if queue is empty
     */
    @Query("SELECT MIN(timestamp) FROM sync_queue WHERE status = 'pending'")
    suspend fun getOldestPendingTimestamp(): Long?
}
