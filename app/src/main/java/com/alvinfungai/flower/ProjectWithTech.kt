package com.alvinfungai.flower

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectWithTech(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("repo_url") val repoUrl: String,
    // This allows Supabase to nest the joined technologies inside the object
    val technologies: List<Technology> = emptyList(),
    @SerialName("user_id") val userId: String,
)