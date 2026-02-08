package com.alvinfungai.flower.ui.project.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.Technology
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.data.repository.ProjectRepository
import com.alvinfungai.flower.data.repository.SupabaseProjectRepository
import com.alvinfungai.flower.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AddProjectViewModel(
    private val repository: ProjectRepository = SupabaseProjectRepository(SupabaseClientProvider.client)
) : ViewModel() {
    private val _state = MutableStateFlow<List<Technology>>(emptyList())
    val state: StateFlow<List<Technology>> = _state

    private val _saveStatus = MutableStateFlow<UiState<Boolean>?>(null)
    val saveStatus: StateFlow<UiState<Boolean>?> = _saveStatus

    fun fetchProjectTechnologies() {
        viewModelScope.launch {
            try {
                _state.value = repository.getAllTechnologies()
            } catch (e: Exception) {
                _saveStatus.value = UiState.Error(e.message ?: "Error fetching project technologies")
            }
        }
    }

    fun saveProject(userId: String, title: String, description: String, repoUrl: String, selectedTechIds: List<String>) {
        viewModelScope.launch {
            _saveStatus.value = UiState.Loading
            try {
                val project = Project(
                    userId = userId,
                    title = title,
                    description = description,
                    repoUrl = repoUrl
                )
                repository.createProject(project, selectedTechIds)
                _saveStatus.value = UiState.Success(true)
            } catch (e: Exception) {
                _saveStatus.value = UiState.Error(e.message ?: "Failed to save project")
            }
        }
    }
}