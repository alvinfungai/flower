package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.ProjectWithTech
import com.alvinfungai.flower.data.model.Technology
import com.alvinfungai.flower.data.model.VoteRequest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc

class SupabaseProjectRepository(private val client: SupabaseClient) : ProjectRepository {
    override suspend fun createProject(
        project: Project,
        techIds: List<String>
    ) {
        val newProject = client.from("projects").insert(project) {
            select()
        }.decodeSingle<Project>()

        if (techIds.isNotEmpty()) {
            val joinRows = techIds.map { techId ->
                mapOf("project_id" to newProject.id, "tech_id" to techId)
            }
            client.from("project_tech").insert(joinRows)
        }
    }

    override suspend fun getAllTechnologies(): List<Technology> {
        return client.from("technologies").select().decodeList<Technology>()
    }

    override suspend fun getProjectById(projectId: String): ProjectWithTech {
        return client.from("projects")
            .select(columns = Columns.raw("*, technologies(*)")) {
                filter { eq("id", projectId) }
            }.decodeSingle<ProjectWithTech>()
    }

    override suspend fun getProjectsByUserId(userId: String): List<Project> {
        return try {
            client.from("projects_with_user_votes").select {
                filter { eq("user_id", userId) }
            }.decodeList<Project>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateProjectWithTech(
        projectId: String,
        title: String,
        description: String,
        repoUrl: String,
        techIds: List<String>
    ) {
        // update project details
        client.from("projects").update({
            set("title", title)
            set("description", description)
            set("repo_url", repoUrl)
        }) { filter { eq("id", projectId) }}

        // sync technologies: Delete existing joins for this project
        client.from("project_tech").delete {
            filter { eq("project_id", projectId) }
        }

        // insert new selections
        if (techIds.isNotEmpty()) {
            val newJoins = techIds.map { mapOf("project_id" to projectId, "tech_id" to it) }
            client.from("project_tech").insert(newJoins)
        }
    }

    override suspend fun deleteProject(projectId: String) {
        client.from("projects").delete {
            filter { eq("id", projectId) }
        }
    }

    override suspend fun voteOnProject(
        projectId: String,
        isUpvote: Boolean
    ): Result<Unit> {
        return try {
            client.postgrest.rpc(
                function = "handle_project_vote",
                parameters = VoteRequest(projectId, isUpvote)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchProjects(): List<Project> {
        return client.from("projects_with_user_votes") // Query the VIEW, not the table
            .select()
            .decodeList<Project>()
    }
}