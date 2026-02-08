package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.Technology
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

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
}