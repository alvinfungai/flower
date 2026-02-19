package com.alvinfungai.flower.ui.auth

/**
 * This class is only for tracking state for visual feedback
 */
sealed class UiState {
    object Idle : UiState()    // default state
    object Loading : UiState() // show progress bar and disable buttons
    data class Error(val message: String) : UiState()  // Failure state: show snackbar or toast
    object Success : UiState() // Optional to show success
}