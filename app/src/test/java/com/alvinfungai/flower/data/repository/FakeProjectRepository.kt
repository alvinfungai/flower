package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.Technology

class FakeProjectRepository : ProjectRepository {

    // 1. create a local in-memory "DATABASE"
    private val projects = mutableListOf<Project>()

    // 2. control variables for testing error cases
    var shouldReturnError = false
    var lastCreatedProject: Project? = null

    override suspend fun createProject(
        project: Project,
        techIds: List<String>
    ) {
        if (shouldReturnError) throw Exception("Insert failed")

        // Save to local "db" so we can verify later
        projects.add(project)
        lastCreatedProject = project
    }

    override suspend fun getAllTechnologies(): List<Technology> {
        return listOf(
            Technology("1", "Kotlin"),
            Technology("2", "Clean Architecture")
        )
    }

    // helper method to return project count
    fun getProjectCount() = projects.size
}