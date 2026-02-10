package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.ProjectWithTech
import com.alvinfungai.flower.data.model.Technology


interface ProjectRepository {
    suspend fun createProject(project: Project, techIds: List<String>)
    suspend fun getAllTechnologies(): List<Technology>
    suspend fun getProjectById(projectId: String): ProjectWithTech
    suspend fun updateProjectWithTech(projectId: String, title: String, description: String, repoUrl: String, techIds: List<String>)
    suspend fun deleteProject(projectId: String)
}