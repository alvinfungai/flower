package com.alvinfungai.flower.ui.project.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.model.ProjectWithTech
import com.alvinfungai.flower.data.model.Technology
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.data.repository.ProjectRepository
import com.alvinfungai.flower.data.repository.SupabaseProjectRepository
import com.alvinfungai.flower.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class EditProjectViewModel(
    private val repository: ProjectRepository = SupabaseProjectRepository(SupabaseClientProvider.client)
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ProjectWithTech>>(UiState.Loading)
    val state: StateFlow<UiState<ProjectWithTech>> = _state

    private val _technologies = MutableStateFlow<List<Technology>>(emptyList())
    val technologies: StateFlow<List<Technology>> = _technologies

    private val _saveStatus = MutableStateFlow<UiState<Unit>?>(null)
    val saveStatus: StateFlow<UiState<Unit>?> = _saveStatus

    fun loadInitialData(projectId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val projectWithTech = repository.getProjectById(projectId)

                // Update the technologies list (for the chips)
                _technologies.value = repository.getAllTechnologies()

                _state.value = UiState.Success(projectWithTech)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Error getting project")
            }
        }
    }

    fun updateProject(projectId: String, title: String, description: String, url: String, techIds: List<String>) {
        viewModelScope.launch {
            _saveStatus.value = UiState.Loading
            try {
                repository.updateProjectWithTech(projectId, title, description, url, techIds)
                _saveStatus.value = UiState.Deleted
            } catch (e: Exception) {
                _saveStatus.value = UiState.Error(e.message ?: "Update failed")
            }
        }
    }
}