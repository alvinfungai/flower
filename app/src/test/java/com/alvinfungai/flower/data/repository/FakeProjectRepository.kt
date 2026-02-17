package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.ProjectWithTech
import com.alvinfungai.flower.data.model.Technology

class FakeProjectRepository : ProjectRepository {

    // 1. create a local in-memory "DATABASE"
    private val projects = mutableListOf<Project>()
    private val technologies = mutableListOf<Technology>()

    // A map of to simulate the join table: ProjectId -> List<TechId>
    private val projectTechJoins = mutableMapOf<String, MutableList<String>>()

    // 2. control variables for testing error cases
    var shouldReturnError = false
    val lastCreatedProject: Project?
        get() = projects.lastOrNull()

    override suspend fun createProject(
        project: Project,
        techIds: List<String>
    ) {
        if (shouldReturnError) throw Exception("Insert failed")

        // Fallback to prevent crashes in tests:
        // Use the project ID if it exists, otherwise use a default dummy value
        val safeId = project.id ?: "temp_id_${projects.size}"

        // Save to local "db" so we can verify later
        projects.add(project)
        projectTechJoins[safeId] = techIds.toMutableList()
    }

    override suspend fun getAllTechnologies(): List<Technology> {
        return listOf(
            Technology("1", "Kotlin"),
            Technology("2", "Clean Architecture")
        )
    }

    override suspend fun getProjectById(projectId: String): ProjectWithTech {
        if (shouldReturnError) throw Exception("Test error")
        val project = projects.find { it.id == projectId }
            ?: throw Exception("Project not found")

        // find techIds linked to this project
        val linkedTechIds = projectTechJoins[projectId] ?: emptyList()

        // map ids back to Tech objects
        val linkedTechObjects = technologies.filter { it.id in linkedTechIds }

        return ProjectWithTech(
            id = project.id!!,
            title = project.title,
            description = project.description ?: "",
            repoUrl = project.repoUrl,
            userId = project.userId,
            technologies = linkedTechObjects
        )
    }



    override suspend fun getProjectsByUserId(userId: String): List<Project> {
        if (shouldReturnError) throw Exception("Test error")
        return projects.filter { it.userId == userId }
    }

    override suspend fun updateProjectWithTech(
        projectId: String,
        title: String,
        description: String,
        repoUrl: String,
        techIds: List<String>
    ) {
        if (shouldReturnError) throw Exception("Update failed")

        val index = projects.indexOfFirst { it.id == projectId }
        if (index != -1) {
            projects[index] = projects[index].copy(
                title = title,
                description = description,
                repoUrl = repoUrl
            )
            // update join table
            projectTechJoins[projectId] = techIds.toMutableList()
        }
    }

    override suspend fun deleteProject(projectId: String) {
        if (shouldReturnError) throw Exception("Error deleting")
        projects.removeIf { it.id == projectId }
        projectTechJoins.remove(projectId)
    }

    // helper method to return project count
    fun getProjectCount() = projects.size

    // helper to pre-fill the fake db with data
    fun addFakeProject(project: Project) {
        projects.add(project)
    }

    // helper for test setup
    fun seedData(initialProjects: List<Project>, initialTech: List<Technology>) {
        projects.addAll(initialProjects)
        technologies.addAll(initialTech)
    }

    // Helper for project tech joins
    fun getTechForProject(projectId: String): List<String> {
        return projectTechJoins[projectId] ?: emptyList()
    }
}