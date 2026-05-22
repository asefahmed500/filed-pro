package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY timestamp ASC")
    fun getPendingItems(): Flow<List<SyncQueueItem>>

    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY timestamp ASC")
    suspend fun getPendingItemsList(): List<SyncQueueItem>

    @Query("SELECT * FROM sync_queue WHERE entityType = :entityType AND status = 'pending'")
    suspend fun getPendingItemsByType(entityType: String): List<SyncQueueItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueItem): Long

    @Query("UPDATE sync_queue SET status = 'synced' WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Query("UPDATE sync_queue SET status = 'failed', retryCount = retryCount + 1 WHERE id = :id")
    suspend fun markAsFailed(id: Int)

    @Query("DELETE FROM sync_queue WHERE status = 'synced'")
    suspend fun clearSynced()

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM sync_queue")
    suspend fun clearAll()
}
