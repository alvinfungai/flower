package com.alvinfungai.flower.ui.project.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.model.ProjectWithTech
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.data.repository.ProjectRepository
import com.alvinfungai.flower.data.repository.SupabaseProjectRepository
import com.alvinfungai.flower.ui.common.UiState
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

    fun onVote(isUpVote: Boolean) {
        val currentState = _state.value
        if (currentState is UiState.Success) {
            val project = currentState.data

            // 1. Optimistic voting
            val voteDiff = calculateVoteDiff(project.userVote, isUpVote)
            val updatedProject = project.copy(
                userVote = if (project.userVote == isUpVote) null else isUpVote,
                voteScore = project.voteScore + voteDiff
            )

            // 2. Apply change to UI immediately
            _state.value = currentState.copy(data = updatedProject)

            // 3. Make network call
            viewModelScope.launch {
                val result = repository.voteOnProject(project.id, isUpVote)

                if (result.isFailure) {
                    // Rollback if network fails: revert to init state
                    _state.value = currentState
                }
            }
        }
    }

    // Helper voting logic: see home
    private fun calculateVoteDiff(currentVote: Boolean?, clickedUpvote: Boolean): Int {
        return when (currentVote) {
            null -> if (clickedUpvote) 1 else -1
            clickedUpvote -> if (clickedUpvote) -1 else 1 // Removing vote
            else -> if (clickedUpvote) 2 else -2         // Switching vote
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
            }
        }
    }
}