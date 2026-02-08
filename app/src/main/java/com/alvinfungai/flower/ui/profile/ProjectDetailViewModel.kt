package com.alvinfungai.flower.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.ProjectWithTech
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.ui.common.UiState
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectDetailViewModel : ViewModel() {
    private val supabase = SupabaseClientProvider.client

    private val _state = MutableStateFlow<UiState<ProjectWithTech>>(UiState.Loading)
    val state: StateFlow<UiState<ProjectWithTech>> = _state

    fun fetchProjectDetails(projectId: String) {
        viewModelScope.launch {
            try {
                // Fetch project with technologies
                val project = supabase.from("projects")
                    .select(columns = Columns.Companion.raw("*, technologies(*)")) {
                        filter { Project::id eq projectId }
                    }.decodeSingle<ProjectWithTech>()
                _state.value = UiState.Success(project)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun deleteProject(projectId: String, onDelete: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = UiState.Loading

                supabase.from("projects").delete {
                    filter { eq("id", projectId) }
                }

                // Switch back to Main thread to ensure UI navigation works
                withContext(Dispatchers.Main) {
                    onDelete()
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Delete failed")
                Log.e("DeleteError", "Error deleting: ${e.message}")
            }
        }
    }
}