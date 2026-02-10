package com.alvinfungai.flower.ui.project.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.ProjectWithTech
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.data.repository.ProjectRepository
import com.alvinfungai.flower.data.repository.SupabaseProjectRepository
import com.alvinfungai.flower.ui.common.UiState
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectDetailViewModel(
    private val repository: ProjectRepository = SupabaseProjectRepository(SupabaseClientProvider.client)
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ProjectWithTech>>(UiState.Loading)
    val state: StateFlow<UiState<ProjectWithTech>> = _state

    fun fetchProjectDetails(projectId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                // Fetch project with technologies
                val data = repository.getProjectById(projectId)
                _state.value = UiState.Success(data)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to fetch details")
            }
        }
    }

    fun deleteProject(projectId: String, onDelete: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = UiState.Loading

                repository.deleteProject(projectId)

                // Update the state
                _state.value = UiState.Deleted

                // Switch back to Main thread to ensure UI navigation works
                withContext(Dispatchers.Main) {
                    onDelete()
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Delete failed")
//                Log.e("DeleteError", "Error deleting: ${e.message}")
            }
        }
    }
}