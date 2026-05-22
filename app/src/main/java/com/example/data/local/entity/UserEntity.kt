package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing a user in the FieldForce system.
 *
 * @property id Unique identifier for the user
 * @property email User's email address (used for login)
 * @property name Display name of the user
 * @property role User role: ADMIN, MANAGER, or EXECUTIVE
 * @property phone Contact phone number
 * @property photoUri URI to user's profile photo
 * @property reportingManagerId ID of the user's manager (null for ADMIN)
 * @property workZoneName Name of the assigned work zone/sector
 * @property workZoneLat Latitude of the work zone center
 * @property workZoneLng Longitude of the work zone center
 * @property workZoneRadiusMeters Radius of the work zone in meters
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val phone: String,
    val photoUri: String,
    val reportingManagerId: String?,
    val workZoneName: String,
    val workZoneLat: Double = 37.7749,
    val workZoneLng: Double = -122.4194,
    val workZoneRadiusMeters: Double = 150.0
) {
    companion object {
        /** Default work zone coordinates (San Francisco) */
        const val DEFAULT_LAT = 37.7749
        const val DEFAULT_LNG = -122.4194
        const val DEFAULT_RADIUS = 150.0

        /** Role constants */
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_MANAGER = "MANAGER"
        const val ROLE_EXECUTIVE = "EXECUTIVE"
    }

    /**
     * Checks if this user is an administrator.
     */
    fun isAdmin(): Boolean = role == ROLE_ADMIN

    /**
     * Checks if this user is a manager.
     */
    fun isManager(): Boolean = role == ROLE_MANAGER

    /**
     * Checks if this user is an executive.
     */
    fun isExecutive(): Boolean = role == ROLE_EXECUTIVE
}
