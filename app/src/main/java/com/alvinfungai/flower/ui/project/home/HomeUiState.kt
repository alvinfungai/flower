package com.alvinfungai.flower.ui.project.home

import com.alvinfungai.flower.data.model.Project


sealed class HomeUiState {
    object Loading: HomeUiState()
    data class Success(val projects: List<Project>): HomeUiState()
    data class Error(val message: String): HomeUiState()
}