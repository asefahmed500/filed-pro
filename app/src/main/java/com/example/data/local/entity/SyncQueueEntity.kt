package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing an item in the offline sync queue.
 * These items are processed when connectivity is restored.
 *
 * @property id Auto-generated primary key
 * @property entityType Type of entity: attendance, task, visit, or file
 * @property entityId ID of the entity to sync
 * @property operation Operation to perform: create, update, or delete
 * @property dataJson JSON string containing entity data
 * @property timestamp Timestamp when the item was queued
 * @property retryCount Number of retry attempts
 * @property status Current status: pending, syncing, success, or failed
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val entityType: String,
    val entityId: Int,
    val operation: String,
    val dataJson: String,
    val timestamp: Long,
    val retryCount: Int = 0,
    val status: String
) {
    companion object {
        /** Entity type constants */
        const val TYPE_ATTENDANCE = "attendance"
        const val TYPE_TASK = "task"
        const val TYPE_VISIT = "visit"
        const val TYPE_FILE = "file"

        /** Operation constants */
        const val OPERATION_CREATE = "create"
        const val OPERATION_UPDATE = "update"
        const val OPERATION_DELETE = "delete"

        /** Status constants */
        const val STATUS_PENDING = "pending"
        const val STATUS_SYNCING = "syncing"
        const val STATUS_SUCCESS = "success"
        const val STATUS_FAILED = "failed"

        /** Maximum retry attempts before marking as failed */
        const val MAX_RETRIES = 3
    }

    /**
     * Checks if the item is pending sync.
     */
    fun isPending(): Boolean = status == STATUS_PENDING

    /**
     * Checks if the item is currently syncing.
     */
    fun isSyncing(): Boolean = status == STATUS_SYNCING

    /**
     * Checks if sync was successful.
     */
    fun isSuccess(): Boolean = status == STATUS_SUCCESS

    /**
     * Checks if sync failed.
     */
    fun isFailed(): Boolean = status == STATUS_FAILED

    /**
     * Checks if maximum retries have been exceeded.
     */
    fun hasExceededMaxRetries(): Boolean = retryCount >= MAX_RETRIES

    /**
     * Increments the retry count and returns a new instance.
     */
    fun incrementRetry(): SyncQueueEntity {
        return copy(retryCount = retryCount + 1)
    }

    /**
     * Returns a display name for the entity type.
     */
    fun getEntityDisplayName(): String {
        return when (entityType) {
            TYPE_ATTENDANCE -> "Attendance"
            TYPE_TASK -> "Task"
            TYPE_VISIT -> "Visit"
            TYPE_FILE -> "File"
            else -> "Unknown"
        }
    }

    /**
     * Returns a display name for the operation.
     */
    fun getOperationDisplayName(): String {
        return when (operation) {
            OPERATION_CREATE -> "Create"
            OPERATION_UPDATE -> "Update"
            OPERATION_DELETE -> "Delete"
            else -> "Unknown"
        }
    }
}
