package com.alvinfungai.flower.ui.project.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.repository.ProjectRepository
import com.alvinfungai.flower.ui.common.BaseProjectViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class HomeViewModel(
    projectRepository: ProjectRepository
) : BaseProjectViewModel(projectRepository) {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // home feed
                val projects = projectRepository.fetchProjects()
                _uiState.value = HomeUiState.Success(projects)
                Log.d("HomeViewModel", "loadProjects: $projects")
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load feed")
            }
        }
    }

    fun onVoteInHome(projectId: String, isUpvote: Boolean) {
        val state = _uiState.value
        if (state is HomeUiState.Success) {
            // performVote logic from BaseVM
            performVote(
                projectId = projectId,
                isUpvote = isUpvote,
                currentProjects = state.projects,
                onUpdate = { newList ->
                    _uiState.value = state.copy(projects = newList)
                    // Log.d("HomeViewModel", "onVoteInHome: $newList")
                },
                onError = { error ->
                    Log.e("HomeViewModel", "VOTE FAILED REVERTING: ${error.message}")
                }
            )
        }
    }
}