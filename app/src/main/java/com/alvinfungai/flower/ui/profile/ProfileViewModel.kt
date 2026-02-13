package com.alvinfungai.flower.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.data.repository.ProfileRepository
import com.alvinfungai.flower.data.repository.ProjectRepository
import com.alvinfungai.flower.data.repository.SupabaseProfileRepository
import com.alvinfungai.flower.data.repository.SupabaseProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ProfileViewModel(
    private val profileRepository: ProfileRepository = SupabaseProfileRepository(
        SupabaseClientProvider.client
    ),
    private val projectRepository: ProjectRepository = SupabaseProjectRepository(
        SupabaseClientProvider.client
    )
    ) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadProfileData(userId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            val profile = profileRepository.getUserProfile(userId)
            val projects = projectRepository.getProjectsByUserId(userId)
            if(profile != null) {
                _uiState.value = ProfileUiState.Success(profile, projects)
            } else {
                _uiState.value = ProfileUiState.Error("Profile not found")
            }
        }
    }

    fun updateUserProfile(newName: String, newBio: String) {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            val currentProfile = currentState.profile

            viewModelScope.launch {
                // Emit "Saving" state
                _uiState.value = currentState.copy(isSaving = true)

                val updatedProfile = currentState.profile.copy(
                    fullName = newName,
                    bio = newBio
                )

                val isSuccessful = profileRepository.updateProfile(updatedProfile)

                if (isSuccessful) {
                    // update data and hide saving message
                    _uiState.value = currentState.copy(
                        profile = updatedProfile,
                        isSaving = false
                    )
                } else {
                    _uiState.value = ProfileUiState.Error("Failed to save changes")
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            profileRepository.signOut()
        }
    }
}