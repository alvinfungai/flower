package com.alvinfungai.flower.ui.project.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alvinfungai.flower.data.repository.ProjectRepository


class HomeViewModelFactory(
    private val projectRepository: ProjectRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(projectRepository) as T
    }
}