package com.example.domain.model

import com.example.data.local.entity.VisitEntity

/**
 * Domain model representing a customer site visit.
 */
data class Visit(
    val id: Int,
    val executiveId: String,
    val customerName: String,
    val address: String,
    val checkInTime: Long,
    val checkOutTime: Long? = null,
    val notes: String? = null,
    val location: Location,
    val signatureBase64: String? = null,
    val photoUri: String? = null,
    val reportPdfName: String? = null
) {
    companion object {
        const val MILLIS_PER_MINUTE = 60000L

        /**
         * Creates a domain Visit from a VisitEntity.
         */
        fun fromEntity(entity: VisitEntity): Visit {
            return Visit(
                id = entity.id,
                executiveId = entity.executiveId,
                customerName = entity.customerName,
                address = entity.address,
                checkInTime = entity.checkInTime,
                checkOutTime = entity.checkOutTime,
                notes = entity.notes,
                location = Location(
                    address = entity.address,
                    latitude = entity.latitude,
                    longitude = entity.longitude
                ),
                signatureBase64 = entity.signatureBase64,
                photoUri = entity.photoUri,
                reportPdfName = entity.reportPdfName
            )
        }
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

    /**
     * Creates a new visit instance with check-out data.
     */
    fun withCheckOut(
        notes: String,
        signature: String?,
        photoUri: String?,
        reportName: String
    ): Visit {
        return copy(
            checkOutTime = System.currentTimeMillis(),
            notes = notes,
            signatureBase64 = signature,
            photoUri = photoUri,
            reportPdfName = reportName
        )
    }
}
