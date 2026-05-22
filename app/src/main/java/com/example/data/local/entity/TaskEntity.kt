package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing a task assigned to an executive.
 *
 * @property id Auto-generated primary key
 * @property title Brief title/description of the task
 * @property description Detailed task specifications
 * @property priority Priority level: HIGH, MEDIUM, or LOW
 * @property dueDate Deadline timestamp for task completion
 * @property locationAddress Human-readable address of task location
 * @property locationLat Latitude of task location
 * @property locationLng Longitude of task location
 * @property status Current status: PENDING, IN_PROGRESS, COMPLETED, or REJECTED
 * @property assignedTo ID of the executive assigned to this task
 * @property assignedByName Name of the executive
 * @property actualStart Timestamp when task was started (null if not started)
 * @property actualEnd Timestamp when task was completed (null if not completed)
 * @property proofPhotoUri URI to photo proof of task completion
 * @property proofSignatureBase64 Base64 encoded signature of task completion
 * @property managerFeedback Feedback from manager after review
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String,
    val dueDate: Long,
    val locationAddress: String,
    val locationLat: Double = 37.7749,
    val locationLng: Double = -122.4194,
    val status: String,
    val assignedTo: String,
    val assignedByName: String,
    val actualStart: Long? = null,
    val actualEnd: Long? = null,
    val proofPhotoUri: String? = null,
    val proofSignatureBase64: String? = null,
    val managerFeedback: String? = null
) {
    companion object {
        /** Priority constants */
        const val PRIORITY_HIGH = "HIGH"
        const val PRIORITY_MEDIUM = "MEDIUM"
        const val PRIORITY_LOW = "LOW"

        /** Status constants */
        const val STATUS_PENDING = "PENDING"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_REJECTED = "REJECTED"
    }

    /**
     * Checks if the task is in pending state.
     */
    fun isPending(): Boolean = status == STATUS_PENDING

    /**
     * Checks if the task is currently in progress.
     */
    fun isInProgress(): Boolean = status == STATUS_IN_PROGRESS

    /**
     * Checks if the task has been completed.
     */
    fun isCompleted(): Boolean = status == STATUS_COMPLETED

    /**
     * Checks if the task has been rejected.
     */
    fun isRejected(): Boolean = status == STATUS_REJECTED

    /**
     * Checks if the task is overdue (past due date and not completed).
     */
    fun isOverdue(): Boolean {
        return !isCompleted() && System.currentTimeMillis() > dueDate
    }

    /**
     * Returns the priority as a numeric value for sorting.
     * Higher number = higher priority.
     */
    fun getPriorityValue(): Int {
        return when (priority) {
            PRIORITY_HIGH -> 3
            PRIORITY_MEDIUM -> 2
            PRIORITY_LOW -> 1
            else -> 0
        }
    }
}
