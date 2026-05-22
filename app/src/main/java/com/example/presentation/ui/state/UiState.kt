package com.example.presentation.ui.state

/**
 * Generic UI state contract for all screens.
 * Provides a consistent way to represent screen states.
 */
sealed class UiState<out T> {
    /**
     * Initial state before any data loading.
     */
    object Idle : UiState<Nothing>()

    /**
     * Loading state while data is being fetched.
     */
    object Loading : UiState<Nothing>()

    /**
     * Success state with data.
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Error state with message.
     */
    data class Error(val message: String) : UiState<Nothing>()

    /**
     * Empty state when there's no data to display.
     */
    object Empty : UiState<Nothing>()
}

/**
 * One-time UI events (snackbars, navigation, etc.).
 */
sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : UiEvent()
    data class NavigateBack(val result: Map<String, Any?>? = null) : UiEvent()
    object NavigateUp : UiEvent()
}

/**
 * Reusable UI effects that should happen once.
 */
sealed class UiEffect {
    data class ShowToast(val message: String) : UiEffect()
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : UiEffect()
    data class Navigate(val route: String, val popBackStack: Boolean = false) : UiEffect()
    object NavigateUp : UiEffect()
    data class ShowError(val message: String) : UiEffect()
}
