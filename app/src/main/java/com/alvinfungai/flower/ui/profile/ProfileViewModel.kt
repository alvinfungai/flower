package com.alvinfungai.flower.ui.profile

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.repository.ProfileRepository
import com.alvinfungai.flower.data.repository.ProjectRepository
import com.alvinfungai.flower.ui.common.BaseProjectViewModel
import com.alvinfungai.flower.ui.common.ImageCompressor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    projectRepository: ProjectRepository,
    private val imageCompressor: ImageCompressor
) : BaseProjectViewModel(projectRepository) {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Refactor to use helper so that UI client code remains the same
    fun loadProfileData(userId: String) {
        viewModelScope.launch {
            loadProfileHelper(userId)
        }
    }

    private suspend fun loadProfileHelper(userId: String) {
        _uiState.value = ProfileUiState.Loading
        val profile = profileRepository.getUserProfile(userId)
        val projects = projectRepository.getProjectsByUserId(userId)

        if (profile != null) {
            _uiState.value = ProfileUiState.Success(profile, projects)
        } else {
            _uiState.value = ProfileUiState.Error("Profile not found")
        }
    }

    fun updateUserProfile(newName: String, newBio: String) {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            // val currentProfile = currentState.profile

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
                        isSaving = false,
                        isDoneSaving = true
                    )
                } else {
                    _uiState.value = ProfileUiState.Error("Failed to save changes")
                }
            }
        }
    }

    fun uploadProfileImage(userId: String, contentResolver: ContentResolver, uri: Uri) {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            viewModelScope.launch {
                _uiState.value = currentState.copy(isSaving = true)
                try {
                    // Convert Uri to Bytes
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw Exception("Failed to process the image")

                    // upload to server
                    val isSuccessful = profileRepository.updateProfilePicture(userId, bytes)

                    if (isSuccessful) {
                        // wait for refresh to finish before moving on
                        loadProfileHelper(userId)
                        // uiState is guaranteed to be the Success state from internalLoadProfile
                        val newState = _uiState.value
                        if (newState is ProfileUiState.Success) {
                            _uiState.value = newState.copy(
                                isSaving = false,
                                isDoneSaving = true
                            )
                        }
                    } else {
                        _uiState.value = ProfileUiState.Error("Failed to upload image")
                    }
                } catch (e: Exception) {
                    _uiState.value = ProfileUiState.Error(e.message ?: "An error occurred")
                }
            }
        }
    }

    fun onVoteProject(projectId: String, isUpvote: Boolean) {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            performVote(
                projectId = projectId,
                isUpvote = isUpvote,
                currentProjects = currentState.projects,
                onUpdate = { newList ->
                    _uiState.value = currentState.copy(projects = newList)
                },
                onError = { error ->
                    // emit effect for toast
                }
            )
        }
    }

    fun resetSaveFlag() {
        val state = _uiState.value
        if (state is ProfileUiState.Success) {
            _uiState.value = state.copy(isDoneSaving = false)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            profileRepository.signOut()
        }
    }
}