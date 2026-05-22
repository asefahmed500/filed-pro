package com.example.domain.model

/**
 * A sealed class representing the outcome of an operation.
 * Used throughout the app for consistent error handling.
 *
 * @param T The type of data contained in a successful result
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data.
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with error information.
     */
    data class Error(val error: UiError) : Result<Nothing>()

    /**
     * Represents an operation in progress.
     */
    object Loading : Result<Nothing>()

    /**
     * Checks if the result is successful.
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Checks if the result is an error.
     */
    fun isError(): Boolean = this is Error

    /**
     * Checks if the result is loading.
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Gets the data if successful, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Executes the given block if successful, returns error otherwise.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }

    /**
     * FlatMaps the result with a function that returns a Result.
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> Loading
    }

    /**
     * Returns the data if successful, or the default value if error/loading.
     */
    fun getOrDefault(defaultValue: T): T = when (this) {
        is Success -> data
        else -> defaultValue
    }

    /**
     * Returns the data if successful, or executes the block to get a value.
     */
    inline fun getOrElse(onError: (UiError) -> T): T = when (this) {
        is Success -> data
        is Error -> onError(error)
        is Loading -> onError(UiError.Unknown("Operation is loading"))
    }
}

/**
 * Sealed class representing different types of UI errors.
 */
sealed class UiError {
    /**
     * Network-related errors (no connection, timeout, etc.)
     */
    data class Network(val message: String, val cause: Throwable? = null) : UiError()

    /**
     * Validation errors (invalid input, missing fields, etc.)
     */
    data class Validation(val message: String, val field: String? = null) : UiError()

    /**
     * Authentication errors (login failed, unauthorized, etc.)
     */
    data class Auth(val message: String) : UiError()

    /**
     * Permission errors (location, camera, etc.)
     */
    data class Permission(val permission: String, val message: String) : UiError()

    /**
     * Database errors (storage failures, etc.)
     */
    data class Database(val message: String, val cause: Throwable? = null) : UiError()

    /**
     * Unknown/unexpected errors.
     */
    data class Unknown(val message: String, val cause: Throwable? = null) : UiError()

    /**
     * Returns a user-friendly error message.
     */
    fun getUserMessage(): String = when (this) {
        is Network -> "Network error: $message"
        is Validation -> "Validation error: $message"
        is Auth -> "Authentication failed: $message"
        is Permission -> "Permission required: $message"
        is Database -> "Database error: $message"
        is Unknown -> "An error occurred: $message"
    }

    /**
     * Checks if this error is recoverable (user can retry).
     */
    fun isRecoverable(): Boolean = when (this) {
        is Network, is Unknown -> true
        is Validation, is Auth, is Permission, is Database -> false
    }
}

/**
 * Extension function to wrap suspending operations in Result.
 */
suspend inline fun <T> resultOf(crossinline block: suspend () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    when (e) {
        is java.net.UnknownHostException,
        is java.net.SocketTimeoutException,
        is java.io.IOException -> Result.Error(UiError.Network(e.message ?: "Network error", e))
        is SecurityException -> Result.Error(UiError.Permission("unknown", e.message ?: "Permission denied"))
        else -> Result.Error(UiError.Unknown(e.message ?: "Unknown error", e))
    }
}

/**
 * Extension function to wrap regular operations in Result.
 */
inline fun <T> resultOfBlocking(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    when (e) {
        is java.net.UnknownHostException,
        is java.net.SocketTimeoutException,
        is java.io.IOException -> Result.Error(UiError.Network(e.message ?: "Network error", e))
        is SecurityException -> Result.Error(UiError.Permission("unknown", e.message ?: "Permission denied"))
        else -> Result.Error(UiError.Unknown(e.message ?: "Unknown error", e))
    }
}
