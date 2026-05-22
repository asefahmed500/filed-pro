package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing an attendance record (check-in/check-out).
 *
 * @property id Auto-generated primary key
 * @property employeeId ID of the employee checking in/out
 * @property checkInTime Timestamp of check-in
 * @property checkOutTime Timestamp of check-out (null while checked in)
 * @property checkInLat Latitude of check-in location
 * @property checkInLng Longitude of check-in location
 * @property checkInSelfieUri URI to check-in selfie photo
 * @property checkInNote Note provided during check-in
 * @property checkOutNote Note provided during check-out
 * @property checkOutTasksCompleted Number of tasks completed during shift
 * @property checkOutExpenses Total expenses claimed for the shift
 * @property isSyncedOffline Whether this record was synced while offline
 * @property isOutsideGeofence Whether check-in was outside assigned geofence
 */
@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val employeeId: String,
    val checkInTime: Long,
    val checkOutTime: Long? = null,
    val checkInLat: Double,
    val checkInLng: Double,
    val checkInSelfieUri: String,
    val checkInNote: String,
    val checkOutNote: String? = null,
    val checkOutTasksCompleted: Int = 0,
    val checkOutExpenses: Double = 0.0,
    val isSyncedOffline: Boolean = true,
    val isOutsideGeofence: Boolean = false
) {
    companion object {
        /** Duration constants for calculations */
        const val MILLIS_PER_HOUR = 3600000L
        const val MILLIS_PER_MINUTE = 60000L
    }

    /**
     * Checks if the employee is currently checked in.
     */
    fun isCheckedIn(): Boolean = checkOutTime == null

    /**
     * Calculates the duration of the shift in hours.
     * Returns null if the employee is still checked in.
     */
    fun getDurationHours(): Double? {
        return checkOutTime?.let { (it - checkInTime).toDouble() / MILLIS_PER_HOUR }
    }

    /**
     * Calculates the duration of the shift in minutes.
     * Returns null if the employee is still checked in.
     */
    fun getDurationMinutes(): Long? {
        return checkOutTime?.let { (it - checkInTime) / MILLIS_PER_MINUTE }
    }
}
