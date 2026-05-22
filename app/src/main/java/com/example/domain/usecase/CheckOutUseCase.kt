package com.example.domain.usecase

import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.NotificationEntity
import com.example.domain.model.Attendance
import com.example.domain.model.Result

/**
 * Use case for checking out an executive.
 * Handles validation, updates attendance record, and creates notifications.
 */
class CheckOutUseCase(
    private val attendanceDao: AttendanceDao,
    private val notificationDao: NotificationDao,
    private val userDao: UserDao
) {
    /**
     * Executes the check-out use case.
     *
     * @param userId The ID of the user checking out
     * @param note End-of-day note
     * @param tasksCompleted Number of tasks completed during shift
     * @param expenses Total expenses claimed
     * @return Result containing the updated Attendance if successful
     */
    suspend operator fun invoke(
        userId: String,
        note: String,
        tasksCompleted: Int,
        expenses: Double
    ): Result<Attendance> {
        // Validate inputs
        val validationError = validateInputs(userId, tasksCompleted, expenses)
        if (validationError != null) {
            return Result.Error(validationError)
        }

        // Get active attendance
        val activeAttendance = attendanceDao.getActiveAttendance(userId)
        if (activeAttendance == null) {
            return Result.Error(
                Result.UiError.Validation("No active check-in found. Please check in first.", "attendance")
            )
        }

        // Get user for notifications
        val user = userDao.getUserById(userId)
        if (user == null) {
            return Result.Error(Result.UiError.Validation("User not found", "userId"))
        }

        return try {
            // Update attendance
            val updatedAttendance = activeAttendance.copy(
                checkOutTime = System.currentTimeMillis(),
                checkOutNote = note.ifEmpty { "Shift finished successfully." },
                checkOutTasksCompleted = tasksCompleted,
                checkOutExpenses = expenses
            )

            attendanceDao.updateAttendance(updatedAttendance)

            // Create notifications
            createCheckOutNotifications(userId, user, tasksCompleted, expenses)

            Result.Success(Attendance.fromEntity(updatedAttendance))
        } catch (e: Exception) {
            Result.Error(Result.UiError.Database("Failed to check out: ${e.message}", e))
        }
    }

    private fun validateInputs(
        userId: String,
        tasksCompleted: Int,
        expenses: Double
    ): Result.UiError? {
        if (userId.isBlank()) {
            return Result.UiError.Validation("User ID is required", "userId")
        }

        if (tasksCompleted < 0) {
            return Result.UiError.Validation("Tasks completed cannot be negative", "tasksCompleted")
        }

        if (expenses < 0) {
            return Result.UiError.Validation("Expenses cannot be negative", "expenses")
        }

        return null
    }

    private suspend fun createCheckOutNotifications(
        userId: String,
        user: com.example.data.local.entity.UserEntity,
        tasksCompleted: Int,
        expenses: Double
    ) {
        // Self notification
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Shift Completed",
                description = "Checked out. Completed: $tasksCompleted tasks. Expenses: $$expenses.",
                timestamp = System.currentTimeMillis()
            )
        )

        // Manager notification
        user.reportingManagerId?.let { managerId ->
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = managerId,
                    title = "Team Check-Out",
                    description = "${user.name} checked out. Completed: $tasksCompleted tasks. Note: ${user.name}",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
