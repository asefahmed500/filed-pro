package com.example.domain.usecase

import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.NotificationEntity
import com.example.domain.model.Attendance
import com.example.domain.model.Result
import com.example.domain.model.User

/**
 * Use case for checking in an executive.
 * Handles validation, geofence checking, and notification creation.
 */
class CheckInUseCase(
    private val attendanceDao: AttendanceDao,
    private val notificationDao: NotificationDao,
    private val userDao: UserDao
) {
    /**
     * Executes the check-in use case.
     *
     * @param userId The ID of the user checking in
     * @param selfieUri URI to the check-in selfie
     * @param note Check-in note
     * @param latitude Current GPS latitude
     * @param longitude Current GPS longitude
     * @return Result containing the created Attendance if successful
     */
    suspend operator fun invoke(
        userId: String,
        selfieUri: String,
        note: String,
        latitude: Double,
        longitude: Longitude
    ): Result<Attendance> {
        // Validate inputs
        val validationError = validateInputs(userId, note, latitude, longitude)
        if (validationError != null) {
            return Result.Error(validationError)
        }

        // Check if user exists
        val user = userDao.getUserById(userId)
        if (user == null) {
            return Result.Error(Result.UiError.Validation("User not found", "userId"))
        }

        // Check if already checked in
        val activeAttendance = attendanceDao.getActiveAttendance(userId)
        if (activeAttendance != null) {
            return Result.Error(
                Result.UiError.Validation("Already checked in. Please check out first.", "attendance")
            )
        }

        // Check geofence
        val isOutsideGeofence = !user.workZoneContains(latitude, longitude)

        return try {
            // Create attendance record
            val attendance = AttendanceEntity(
                employeeId = userId,
                checkInTime = System.currentTimeMillis(),
                checkInLat = latitude,
                checkInLng = longitude,
                checkInSelfieUri = selfieUri.ifEmpty { "ic_default_avatar" },
                checkInNote = note.ifEmpty { "Commencing route on schedule" },
                isOutsideGeofence = isOutsideGeofence
            )

            val id = attendanceDao.insertAttendance(attendance)

            // Create notifications
            createCheckInNotifications(userId, user, isOutsideGeofence)

            Result.Success(
                Attendance.fromEntity(attendance.copy(id = id.toInt()))
            )
        } catch (e: Exception) {
            Result.Error(Result.UiError.Database("Failed to check in: ${e.message}", e))
        }
    }

    private fun validateInputs(
        userId: String,
        note: String,
        latitude: Double,
        longitude: Double
    ): Result.UiError? {
        if (userId.isBlank()) {
            return Result.UiError.Validation("User ID is required", "userId")
        }

        if (latitude < -90 || latitude > 90) {
            return Result.UiError.Validation("Invalid latitude", "latitude")
        }

        if (longitude < -180 || longitude > 180) {
            return Result.UiError.Validation("Invalid longitude", "longitude")
        }

        return null
    }

    private suspend fun createCheckInNotifications(
        userId: String,
        user: com.example.data.local.entity.UserEntity,
        isOutsideGeofence: Boolean
    ) {
        // Self notification
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Check In Complete",
                description = "Started shift. Location verified.",
                timestamp = System.currentTimeMillis()
            )
        )

        // Manager notification
        user.reportingManagerId?.let { managerId ->
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = managerId,
                    title = "Team Check-In",
                    description = "${user.name} checked in at ${user.workZoneName}" +
                            if (isOutsideGeofence) " (GEOFENCE OUTSIDE!)" else "",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}

private fun com.example.data.local.entity.UserEntity.workZoneContains(
    latitude: Double,
    longitude: Double
): Boolean {
    val distance = calculateDistance(
        latitude, longitude,
        workZoneLat, workZoneLng
    )
    return distance <= workZoneRadiusMeters
}

private fun calculateDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val earthRadius = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}

private typealias Longitude = Double
