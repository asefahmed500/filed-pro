package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing a customer site visit.
 *
 * @property id Auto-generated primary key
 * @property executiveId ID of the executive making the visit
 * @property customerName Name of the customer being visited
 * @property address Address of the customer location
 * @property checkInTime Timestamp of visit check-in
 * @property checkOutTime Timestamp of visit check-out (null while in progress)
 * @property notes Notes taken during the visit
 * @property latitude Latitude of visit location
 * @property longitude Longitude of visit location
 * @property signatureBase64 Base64 encoded customer signature
 * @property photoUri URI to photo taken during visit
 * @property reportPdfName Filename of generated PDF report
 */
@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val executiveId: String,
    val customerName: String,
    val address: String,
    val checkInTime: Long,
    val checkOutTime: Long? = null,
    val notes: String? = null,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val signatureBase64: String? = null,
    val photoUri: String? = null,
    val reportPdfName: String? = null
) {
    companion object {
        const val MILLIS_PER_MINUTE = 60000L
    }

    /**
     * Checks if the visit is currently in progress.
     */
    fun isInProgress(): Boolean = checkOutTime == null

    /**
     * Checks if the visit has been completed.
     */
    fun isCompleted(): Boolean = checkOutTime != null

    /**
     * Calculates the duration of the visit in minutes.
     * Returns null if the visit is still in progress.
     */
    fun getDurationMinutes(): Long? {
        return checkOutTime?.let { (it - checkInTime) / MILLIS_PER_MINUTE }
    }

    /**
     * Checks if the visit has all required completion data.
     */
    fun isFullyDocumented(): Boolean {
        return isCompleted() &&
                !notes.isNullOrBlank() &&
                !signatureBase64.isNullOrBlank() &&
                !reportPdfName.isNullOrBlank()
    }
}
