package com.example.domain.model

import com.example.data.local.entity.FileRecordEntity

/**
 * Domain model representing a file/expense record submitted for approval.
 */
data class FileRecord(
    val id: Int,
    val fileName: String,
    val category: Category,
    val fileUri: String,
    val uploadedBy: String,
    val uploadedByName: String,
    val timestamp: Long,
    val location: Location,
    val tags: List<String>,
    val amount: Double? = null,
    val status: Status,
    val rejectionReason: String? = null
) {
    /**
     * File category types.
     */
    enum class Category {
        EXPENSE,
        POD,
        INCIDENT,
        TIMESHEET;

        companion object {
            fun fromString(value: String): Category {
                return values().find { it.name == value } ?: EXPENSE
            }
        }
    }

    /**
     * File approval status.
     */
    enum class Status {
        PENDING,
        APPROVED,
        REJECTED;

        companion object {
            fun fromString(value: String): Status {
                return values().find { it.name == value } ?: PENDING
            }
        }
    }

    /**
     * Checks if the file is pending approval.
     */
    fun isPending(): Boolean = status == Status.PENDING

    /**
     * Checks if the file has been approved.
     */
    fun isApproved(): Boolean = status == Status.APPROVED

    /**
     * Checks if the file has been rejected.
     */
    fun isRejected(): Boolean = status == Status.REJECTED

    /**
     * Checks if this is an expense claim with an amount.
     */
    fun isExpenseClaim(): Boolean = category == Category.EXPENSE && amount != null

    companion object {
        /**
         * Creates a domain FileRecord from a FileRecordEntity.
         */
        fun fromEntity(entity: FileRecordEntity): FileRecord {
            return FileRecord(
                id = entity.id,
                fileName = entity.fileName,
                category = Category.fromString(entity.category),
                fileUri = entity.fileUri,
                uploadedBy = entity.uploadedBy,
                uploadedByName = entity.uploadedByName,
                timestamp = entity.timestamp,
                location = Location(
                    address = "",
                    latitude = entity.latitude,
                    longitude = entity.longitude
                ),
                tags = entity.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                amount = entity.amount,
                status = Status.fromString(entity.status),
                rejectionReason = entity.rejectionReason
            )
        }
    }

    /**
     * Creates a new file record with updated status.
     */
    fun withStatus(status: Status, reason: String? = null): FileRecord {
        return copy(
            status = status,
            rejectionReason = if (status == Status.REJECTED) reason else null
        )
    }
}
