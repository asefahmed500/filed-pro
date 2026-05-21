package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val role: String, // "ADMIN", "MANAGER", "EXECUTIVE"
    val phone: String,
    val photoUri: String,
    val reportingManagerId: String?,
    val workZoneName: String,
    val workZoneLat: Double = 37.7749, // Default to San Francisco coords
    val workZoneLng: Double = -122.4194,
    val workZoneRadiusMeters: Double = 150.0
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: String,
    val checkInTime: Long,
    val checkOutTime: Long? = null,
    val checkInLat: Double,
    val checkInLng: Double,
    val checkInSelfieUri: String, // Mock or actual uri
    val checkInNote: String,
    val checkOutNote: String? = null,
    val checkOutTasksCompleted: Int = 0,
    val checkOutExpenses: Double = 0.0,
    val isSyncedOffline: Boolean = true,
    val isOutsideGeofence: Boolean = false
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val dueDate: Long,
    val locationAddress: String,
    val locationLat: Double = 37.7749,
    val locationLng: Double = -122.4194,
    val status: String, // "PENDING", "IN_PROGRESS", "COMPLETED", "REJECTED"
    val assignedTo: String,
    val assignedByName: String,
    val actualStart: Long? = null,
    val actualEnd: Long? = null,
    val proofPhotoUri: String? = null,
    val proofSignatureBase64: String? = null,
    val managerFeedback: String? = null
)

@Entity(tableName = "visits")
data class Visit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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

@Entity(tableName = "file_records")
data class FileRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val category: String, // "EXPENSE", "POD", "INCIDENT", "TIMESHEET"
    val fileUri: String,
    val uploadedBy: String,
    val uploadedByName: String,
    val timestamp: Long,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val tags: String,
    val amount: Double? = null,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val rejectionReason: String? = null
)

@Entity(tableName = "notifications")
data class NotificationModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val isRead: Boolean = false
)
