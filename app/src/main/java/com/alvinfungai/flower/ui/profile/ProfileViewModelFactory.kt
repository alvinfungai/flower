package com.alvinfungai.flower.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alvinfungai.flower.data.repository.ProfileRepository
import com.alvinfungai.flower.data.repository.ProjectRepository
import com.alvinfungai.flower.ui.auth.MainViewModel
import com.alvinfungai.flower.ui.common.ImageCompressor


class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val projectRepository: ProjectRepository,
    private val imageCompressor: ImageCompressor
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(profileRepository, projectRepository, imageCompressor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}