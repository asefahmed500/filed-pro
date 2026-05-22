package com.example.domain.model

import com.example.data.local.entity.TaskEntity

/**
 * Domain model representing a task assigned to an executive.
 */
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val priority: Priority,
    val dueDate: Long,
    val location: Location,
    val status: Status,
    val assignedTo: String,
    val assignedByName: String,
    val actualStart: Long? = null,
    val actualEnd: Long? = null,
    val proofPhotoUri: String? = null,
    val proofSignatureBase64: String? = null,
    val managerFeedback: String? = null
) {
    /**
     * Task priority levels.
     */
    enum class Priority {
        HIGH,
        MEDIUM,
        LOW;

        companion object {
            fun fromString(value: String): Priority {
                return values().find { it.name == value } ?: MEDIUM
            }
        }
    }

    /**
     * Task status values.
     */
    enum class Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        REJECTED;

        companion object {
            fun fromString(value: String): Status {
                return values().find { it.name == value } ?: PENDING
            }
        }
    }

    /**
     * Checks if the task is pending.
     */
    fun isPending(): Boolean = status == Status.PENDING

    /**
     * Checks if the task is in progress.
     */
    fun isInProgress(): Boolean = status == Status.IN_PROGRESS

    /**
     * Checks if the task is completed.
     */
    fun isCompleted(): Boolean = status == Status.COMPLETED

    /**
     * Checks if the task is rejected.
     */
    fun isRejected(): Boolean = status == Status.REJECTED

    /**
     * Checks if the task is overdue.
     */
    fun isOverdue(): Boolean {
        return !isCompleted() && System.currentTimeMillis() > dueDate
    }

    /**
     * Returns the priority as a numeric value for sorting.
     */
    fun getPriorityValue(): Int {
        return when (priority) {
            Priority.HIGH -> 3
            Priority.MEDIUM -> 2
            Priority.LOW -> 1
        }
    }

    companion object {
        /**
         * Creates a domain Task from a TaskEntity.
         */
        fun fromEntity(entity: TaskEntity): Task {
            return Task(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                priority = Priority.fromString(entity.priority),
                dueDate = entity.dueDate,
                location = Location(
                    address = entity.locationAddress,
                    latitude = entity.locationLat,
                    longitude = entity.locationLng
                ),
                status = Status.fromString(entity.status),
                assignedTo = entity.assignedTo,
                assignedByName = entity.assignedByName,
                actualStart = entity.actualStart,
                actualEnd = entity.actualEnd,
                proofPhotoUri = entity.proofPhotoUri,
                proofSignatureBase64 = entity.proofSignatureBase64,
                managerFeedback = entity.managerFeedback
            )
        }
    }
}

/**
 * Data class representing a location.
 */
data class Location(
    val address: String,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194
)
