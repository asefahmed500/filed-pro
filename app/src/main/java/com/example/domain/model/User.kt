package com.example.domain.model

import com.example.data.local.entity.UserEntity

/**
 * Domain model representing a user in the FieldForce system.
 * This is separate from the Room entity to decouple domain from data layer.
 *
 * @property id Unique identifier for the user
 * @property email User's email address
 * @property name Display name
 * @property role User role (ADMIN, MANAGER, EXECUTIVE)
 * @property phone Contact phone number
 * @property photoUri URI to profile photo
 * @property reportingManagerId ID of the user's manager
 * @property workZone Assigned work zone information
 */
data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: Role,
    val phone: String,
    val photoUri: String,
    val reportingManagerId: String?,
    val workZone: WorkZone
) {
    /**
     * Enum representing user roles.
     */
    enum class Role {
        ADMIN,
        MANAGER,
        EXECUTIVE;

        companion object {
            fun fromString(value: String): Role {
                return values().find { it.name == value } ?: EXECUTIVE
            }
        }
    }

    /**
     * Checks if the user is an administrator.
     */
    fun isAdmin(): Boolean = role == Role.ADMIN

    /**
     * Checks if the user is a manager.
     */
    fun isManager(): Boolean = role == Role.MANAGER

    /**
     * Checks if the user is an executive.
     */
    fun isExecutive(): Boolean = role == Role.EXECUTIVE

    companion object {
        /**
         * Creates a domain User from a UserEntity.
         */
        fun fromEntity(entity: UserEntity): User {
            return User(
                id = entity.id,
                email = entity.email,
                name = entity.name,
                role = Role.fromString(entity.role),
                phone = entity.phone,
                photoUri = entity.photoUri,
                reportingManagerId = entity.reportingManagerId,
                workZone = WorkZone(
                    name = entity.workZoneName,
                    latitude = entity.workZoneLat,
                    longitude = entity.workZoneLng,
                    radiusMeters = entity.workZoneRadiusMeters
                )
            )
        }
    }
}

/**
 * Data class representing a work zone/sector for geofencing.
 */
data class WorkZone(
    val name: String,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val radiusMeters: Double = 150.0
) {
    companion object {
        const val DEFAULT_LAT = 37.7749
        const val DEFAULT_LNG = -122.4194
        const val DEFAULT_RADIUS = 150.0
    }

    /**
     * Checks if a coordinate is within the work zone.
     */
    fun contains(latitude: Double, longitude: Double): Boolean {
        val distance = calculateDistance(
            latitude, longitude,
            this.latitude, this.longitude
        )
        return distance <= radiusMeters
    }

    /**
     * Calculates distance between two coordinates in meters using Haversine formula.
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
}
