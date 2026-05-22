package com.example.domain.model

import com.example.data.local.entity.NotificationEntity

/**
 * Domain model representing a notification for a user.
 */
data class Notification(
    val id: Int,
    val userId: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val isRead: Boolean = false
) {
    companion object {
        /**
         * Creates a domain Notification from a NotificationEntity.
         */
        fun fromEntity(entity: NotificationEntity): Notification {
            return Notification(
                id = entity.id,
                userId = entity.userId,
                title = entity.title,
                description = entity.description,
                timestamp = entity.timestamp,
                isRead = entity.isRead
            )
        }

        /**
         * Notification type factory methods.
         */
        fun checkInComplete(userId: String, note: String): Notification {
            return Notification(
                id = 0,
                userId = userId,
                title = "Check In Complete",
                description = "Started shift at $note. Location verified.",
                timestamp = System.currentTimeMillis()
            )
        }

        fun checkOutComplete(userId: String, expenses: Double): Notification {
            return Notification(
                id = 0,
                userId = userId,
                title = "Shift Completed",
                description = "Checked out. Submitted expenses: $$$expenses.",
                timestamp = System.currentTimeMillis()
            )
        }

        fun taskAssigned(userId: String, title: String, priority: String): Notification {
            return Notification(
                id = 0,
                userId = userId,
                title = "New Task Assigned",
                description = "\"$title\" - Priority: $priority.",
                timestamp = System.currentTimeMillis()
            )
        }

        fun taskUpdated(managerId: String, userName: String, taskTitle: String, status: String): Notification {
            return Notification(
                id = 0,
                userId = managerId,
                title = "Task Updated",
                description = "$userName marked task \"$taskTitle\" as $status.",
                timestamp = System.currentTimeMillis()
            )
        }

        fun fileSubmitted(userId: String, fileName: String): Notification {
            return Notification(
                id = 0,
                userId = userId,
                title = "Document Submitted",
                description = "Expense/Bill \"$fileName\" submitted for approval.",
                timestamp = System.currentTimeMillis()
            )
        }

        fun fileStatusChanged(userId: String, fileName: String, status: String): Notification {
            return Notification(
                id = 0,
                userId = userId,
                title = "File Status Update",
                description = "Your file \"$fileName\" has been $status.",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Checks if the notification is unread.
     */
    fun isUnread(): Boolean = !isRead

    /**
     * Returns a formatted relative time string.
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

    /**
     * Creates a new notification marked as read.
     */
    fun markAsRead(): Notification {
        return copy(isRead = true)
    }
}
