package com.alvinfungai.flower.ui.profile

import com.alvinfungai.flower.data.model.Profile
import com.alvinfungai.flower.data.model.Project

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(
        val profile: Profile,
        val projects: List<Project>,
        val isSaving: Boolean = false,
        val isDoneSaving: Boolean = false
        ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}