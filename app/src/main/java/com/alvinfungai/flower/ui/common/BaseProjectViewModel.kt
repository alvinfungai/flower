package com.alvinfungai.flower.ui.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.repository.ProjectRepository
import kotlinx.coroutines.launch


abstract class BaseProjectViewModel(
    protected val projectRepository: ProjectRepository
) : ViewModel() {
    /**
     * Common voting logic to be used by any Screen that displays projects.
     * [currentProjects]: The current list in your UI state.
     * [onUpdate]: A callback to emit the new list to a specific StateFlow.
     */
    fun performVote(
        projectId: String,
        isUpvote: Boolean,
        currentProjects: List<Project>,
        onUpdate: (List<Project>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        // 1. Optimistic Update
        val updatedProjects = currentProjects.map { project ->
            if (project.id == projectId) {
                val diff = calculateVoteDiff(project.userVote, isUpvote)
                project.copy(
                    voteScore = project.voteScore + diff,
                    userVote = if (project.userVote == isUpvote) null else isUpvote
                )
            } else project
        }

        onUpdate(updatedProjects)

        Log.d("BaseProjectViewModel", "performVote: $updatedProjects")

        // 2. Background Sync
        viewModelScope.launch {
            val result = projectRepository.voteOnProject(projectId, isUpvote)
            if (result.isFailure) {
                // Rollback to original list if server fails
                onUpdate(currentProjects)
                onError(result.exceptionOrNull() ?: Exception("Vote failed"))
            }
        }
    }

    private fun calculateVoteDiff(currentVote: Boolean?, clickedUpvote: Boolean): Int {
        return when (currentVote) {
            null -> if (clickedUpvote) 1 else -1
            clickedUpvote -> if (clickedUpvote) -1 else 1
            else -> if (clickedUpvote) 2 else -2
        }
    }
}