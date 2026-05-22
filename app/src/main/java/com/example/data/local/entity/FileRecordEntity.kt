package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing a file/expense record submitted for approval.
 *
 * @property id Auto-generated primary key
 * @property fileName Name/description of the file
 * @property category Category: EXPENSE, POD, INCIDENT, or TIMESHEET
 * @property fileUri URI to the uploaded file
 * @property uploadedBy ID of the user who uploaded the file
 * @property uploadedByName Name of the user who uploaded the file
 * @property timestamp Timestamp of file submission
 * @property latitude Latitude of submission location
 * @property longitude Longitude of submission location
 * @property tags Comma-separated tags for categorization
 * @property amount Monetary amount (for expense claims)
 * @property status Approval status: PENDING, APPROVED, or REJECTED
 * @property rejectionReason Reason for rejection (null if not rejected)
 */
@Entity(tableName = "file_records")
data class FileRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String,
    val category: String,
    val fileUri: String,
    val uploadedBy: String,
    val uploadedByName: String,
    val timestamp: Long,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val tags: String,
    val amount: Double? = null,
    val status: String,
    val rejectionReason: String? = null
) {
    companion object {
        /** Category constants */
        const val CATEGORY_EXPENSE = "EXPENSE"
        const val CATEGORY_POD = "POD"
        const val CATEGORY_INCIDENT = "INCIDENT"
        const val CATEGORY_TIMESHEET = "TIMESHEET"

        /** Status constants */
        const val STATUS_PENDING = "PENDING"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_REJECTED = "REJECTED"
    }

    /**
     * Checks if the file is pending approval.
     */
    fun isPending(): Boolean = status == STATUS_PENDING

    /**
     * Checks if the file has been approved.
     */
    fun isApproved(): Boolean = status == STATUS_APPROVED

    /**
     * Checks if the file has been rejected.
     */
    fun isRejected(): Boolean = status == STATUS_REJECTED

    /**
     * Checks if this is an expense claim with an amount.
     */
    fun isExpenseClaim(): Boolean = category == CATEGORY_EXPENSE && amount != null

    /**
     * Returns the tags as a list.
     */
    fun getTagsList(): List<String> {
        return tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
