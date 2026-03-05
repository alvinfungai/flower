package com.alvinfungai.flower.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectWithTech(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("repo_url") val repoUrl: String,
    val stars: Int = 0,
    val forks: Int = 0,
    @SerialName("open_issues") val openIssues: Int = 0,
    @SerialName("repo_updated_at") val repoUpdatedAt: String = "",
    @SerialName("vote_score") val voteScore: Int = 0,
    @SerialName("user_vote") val userVote: Boolean? = null,
    // This allows Supabase to nest the joined technologies inside the object
    val technologies: List<Technology> = emptyList(),
    @SerialName("user_id") val userId: String,
)