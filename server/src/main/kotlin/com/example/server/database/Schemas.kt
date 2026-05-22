package com.example.server.database

import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

// --- Exposed Table Declarations ---

object Users : Table("users") {
    val id = varchar("id", 128)
    val email = varchar("email", 256)
    val name = varchar("name", 256)
    val role = varchar("role", 64) // ADMIN, MANAGER, EXECUTIVE
    val phone = varchar("phone", 64)
    val photoUri = text("photo_uri")
    val reportingManagerId = varchar("reporting_manager_id", 128).nullable()
    val workZoneName = varchar("work_zone_name", 256)
    val workZoneLat = double("work_zone_lat")
    val workZoneLng = double("work_zone_lng")
    val workZoneRadiusMeters = double("work_zone_radius_meters")

    override val primaryKey = PrimaryKey(id)
}

object Attendances : Table("attendance") {
    val id = integer("id").autoIncrement()
    val employeeId = varchar("employee_id", 128)
    val checkInTime = long("check_in_time")
    val checkOutTime = long("check_out_time").nullable()
    val checkInLat = double("check_in_lat")
    val checkInLng = double("check_in_lng")
    val checkInSelfieUri = text("check_in_selfie_uri")
    val checkInNote = text("check_in_note")
    val checkOutNote = text("check_out_note").nullable()
    val checkOutTasksCompleted = integer("check_out_tasks_completed").default(0)
    val checkOutExpenses = double("check_out_expenses").default(0.0)
    val isSyncedOffline = bool("is_synced_offline").default(true)
    val isOutsideGeofence = bool("is_outside_geofence").default(false)

    override val primaryKey = PrimaryKey(id)
}

object Tasks : Table("tasks") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 256)
    val description = text("description")
    val priority = varchar("priority", 64) // HIGH, MEDIUM, LOW
    val dueDate = long("due_date")
    val locationAddress = varchar("location_address", 512)
    val locationLat = double("location_lat")
    val locationLng = double("location_lng")
    val status = varchar("status", 64) // PENDING, IN_PROGRESS, COMPLETED, REJECTED
    val assignedTo = varchar("assigned_to", 128)
    val assignedByName = varchar("assigned_by_name", 256)
    val actualStart = long("actual_start").nullable()
    val actualEnd = long("actual_end").nullable()
    val proofPhotoUri = text("proof_photo_uri").nullable()
    val proofSignatureBase64 = text("proof_signature_base64").nullable()
    val managerFeedback = text("manager_feedback").nullable()

    override val primaryKey = PrimaryKey(id)
}

object Visits : Table("visits") {
    val id = integer("id").autoIncrement()
    val executiveId = varchar("executive_id", 128)
    val customerName = varchar("customer_name", 256)
    val address = varchar("address", 512)
    val checkInTime = long("check_in_time")
    val checkOutTime = long("check_out_time").nullable()
    val notes = text("notes").nullable()
    val latitude = double("latitude")
    val longitude = double("longitude")
    val signatureBase64 = text("signature_base64").nullable()
    val photoUri = text("photo_uri").nullable()
    val reportPdfName = varchar("report_pdf_name", 256).nullable()

    override val primaryKey = PrimaryKey(id)
}

object FileRecords : Table("file_records") {
    val id = integer("id").autoIncrement()
    val fileName = varchar("file_name", 256)
    val category = varchar("category", 64) // EXPENSE, POD, INCIDENT, TIMESHEET
    val fileUri = text("file_uri")
    val uploadedBy = varchar("uploaded_by", 128)
    val uploadedByName = varchar("uploaded_by_name", 256)
    val timestamp = long("timestamp")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val tags = varchar("tags", 256)
    val amount = double("amount").nullable()
    val status = varchar("status", 64) // PENDING, APPROVED, REJECTED
    val rejectionReason = text("rejection_reason").nullable()

    override val primaryKey = PrimaryKey(id)
}

object Notifications : Table("notifications") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 128)
    val title = varchar("title", 256)
    val description = text("description")
    val timestamp = long("timestamp")
    val isRead = bool("is_read").default(false)

    override val primaryKey = PrimaryKey(id)
}

// --- Serializable DTOs Matching Android Room Entities ---

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val phone: String,
    val photoUri: String,
    val reportingManagerId: String?,
    val workZoneName: String,
    val workZoneLat: Double,
    val workZoneLng: Double,
    val workZoneRadiusMeters: Double
)

@Serializable
data class AttendanceDto(
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
)

@Serializable
data class TaskDto(
    val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String,
    val dueDate: Long,
    val locationAddress: String,
    val locationLat: Double = 37.7749,
    val locationLng: Double = -122.4194,
    val status: String,
    val assignedTo: String,
    val assignedByName: String,
    val actualStart: Long? = null,
    val actualEnd: Long? = null,
    val proofPhotoUri: String? = null,
    val proofSignatureBase64: String? = null,
    val managerFeedback: String? = null
)

@Serializable
data class VisitDto(
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
)

@Serializable
data class FileRecordDto(
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
)

@Serializable
data class NotificationDto(
    val id: Int = 0,
    val userId: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Serializable
data class SyncPayloadDto(
    val attendances: List<AttendanceDto> = emptyList(),
    val tasks: List<TaskDto> = emptyList(),
    val visits: List<VisitDto> = emptyList(),
    val fileRecords: List<FileRecordDto> = emptyList()
)

@Serializable
data class SyncResponseDto(
    val success: Boolean,
    val message: String
)
