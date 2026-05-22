package com.example.domain.usecase

import com.example.data.local.dao.UserDao
import com.example.domain.model.Result
import com.example.domain.model.User
import kotlinx.coroutines.flow.firstOrNull

/**
 * Use case for logging in a user with their email address.
 * Handles validation and user lookup from the local database.
 */
class LoginWithEmailUseCase(
    private val userDao: UserDao
) {
    companion object {
        private const val MIN_EMAIL_LENGTH = 3
        private const val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    /**
     * Executes the login use case.
     *
     * @param email The user's email address
     * @return Result containing the User if successful, Error otherwise
     */
    suspend operator fun invoke(email: String): Result<User> {
        // Validate email
        val validationError = validateEmail(email)
        if (validationError != null) {
            return Result.Error(validationError)
        }

        // Lookup user
        return try {
            val trimmedEmail = email.trim().lowercase()
            val userEntity = userDao.getUserByEmail(trimmedEmail)

            if (userEntity != null) {
                Result.Success(User.fromEntity(userEntity))
            } else {
                Result.Error(
                    Result.UiError.Validation(
                        message = "User not found. Please register or check your email.",
                        field = "email"
                    )
                )
            }
        } catch (e: Exception) {
            Result.Error(Result.UiError.Database("Failed to lookup user: ${e.message}", e))
        }
    }

    /**
     * Validates the email input.
     */
    private fun validateEmail(email: String): Result.UiError? {
        val trimmed = email.trim()

        if (trimmed.isEmpty()) {
            return Result.UiError.Validation("Email is required", "email")
        }

        if (trimmed.length < MIN_EMAIL_LENGTH) {
            return Result.UiError.Validation("Email is too short", "email")
        }

        if (!EMAIL_PATTERN.matches(trimmed)) {
            return Result.UiError.Validation("Invalid email format", "email")
        }

        return null
    }
}
