package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing a notification for a user.
 *
 * @property id Auto-generated primary key
 * @property userId ID of the user receiving the notification
 * @property title Brief title of the notification
 * @property description Detailed message content
 * @property timestamp Timestamp when notification was created
 * @property isRead Whether the notification has been read
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val isRead: Boolean = false
) {
    companion object {
        /** Notification type constants for categorization */
        const val TYPE_CHECK_IN = "check_in"
        const val TYPE_CHECK_OUT = "check_out"
        const val TYPE_TASK_ASSIGNED = "task_assigned"
        const val TYPE_TASK_COMPLETED = "task_completed"
        const val TYPE_VISIT_STARTED = "visit_started"
        const val TYPE_VISIT_COMPLETED = "visit_completed"
        const val TYPE_FILE_SUBMITTED = "file_submitted"
        const val TYPE_FILE_APPROVED = "file_approved"
        const val TYPE_FILE_REJECTED = "file_rejected"
        const val TYPE_SESSION_STARTED = "session_started"
        const val TYPE_SESSION_ENDED = "session_ended"
    }

    /**
     * Checks if the notification is unread.
     */
    fun isUnread(): Boolean = !isRead

    /**
     * Returns a formatted timestamp string.
     */
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> "${diff / 604800000}w ago"
        }
    }
}
