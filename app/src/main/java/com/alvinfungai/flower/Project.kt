package com.alvinfungai.flower

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("repo_url") val repoUrl: String,
    val title: String,
    val description: String?
)
