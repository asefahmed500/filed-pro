package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entityType: String, // "attendance", "task", "visit", "file"
    val entityId: Int,
    val operation: String, // "create", "update", "delete"
    val dataJson: String, // JSON of the entity
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: String = "pending" // "pending", "synced", "failed"
)
