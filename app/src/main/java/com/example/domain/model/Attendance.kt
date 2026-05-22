package com.example.domain.model

import com.example.data.local.entity.AttendanceEntity

/**
 * Domain model representing an attendance record (check-in/check-out).
 */
data class Attendance(
    val id: Int,
    val employeeId: String,
    val checkInTime: Long,
    val checkOutTime: Long? = null,
    val checkInLocation: Location,
    val checkInSelfieUri: String,
    val checkInNote: String,
    val checkOutNote: String? = null,
    val checkOutTasksCompleted: Int = 0,
    val checkOutExpenses: Double = 0.0,
    val isSyncedOffline: Boolean = true,
    val isOutsideGeofence: Boolean = false
) {
    companion object {
        const val MILLIS_PER_HOUR = 3600000L
        const val MILLIS_PER_MINUTE = 60000L

        /**
         * Creates a domain Attendance from an AttendanceEntity.
         */
        fun fromEntity(entity: AttendanceEntity): Attendance {
            return Attendance(
                id = entity.id,
                employeeId = entity.employeeId,
                checkInTime = entity.checkInTime,
                checkOutTime = entity.checkOutTime,
                checkInLocation = Location(
                    address = "",
                    latitude = entity.checkInLat,
                    longitude = entity.checkInLng
                ),
                checkInSelfieUri = entity.checkInSelfieUri,
                checkInNote = entity.checkInNote,
                checkOutNote = entity.checkOutNote,
                checkOutTasksCompleted = entity.checkOutTasksCompleted,
                checkOutExpenses = entity.checkOutExpenses,
                isSyncedOffline = entity.isSyncedOffline,
                isOutsideGeofence = entity.isOutsideGeofence
            )
        }
    }

    /**
     * Checks if the employee is currently checked in.
     */
    fun isCheckedIn(): Boolean = checkOutTime == null

    /**
     * Calculates the duration of the shift in hours.
     */
    fun getDurationHours(): Double? {
        return checkOutTime?.let { (it - checkInTime).toDouble() / MILLIS_PER_HOUR }
    }

    /**
     * Calculates the duration of the shift in minutes.
     */
    fun getDurationMinutes(): Long? {
        return checkOutTime?.let { (it - checkInTime) / MILLIS_PER_MINUTE }
    }

    /**
     * Creates a new attendance instance with check-out data.
     */
    fun withCheckOut(
        note: String,
        tasksCompleted: Int,
        expenses: Double
    ): Attendance {
        return copy(
            checkOutTime = System.currentTimeMillis(),
            checkOutNote = note,
            checkOutTasksCompleted = tasksCompleted,
            checkOutExpenses = expenses
        )
    }
}
