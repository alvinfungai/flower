package com.alvinfungai.flower.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("repo_url") val repoUrl: String,
    val title: String,
    val description: String?,
    val stars: Int = 0,
    val forks: Int = 0,
    @SerialName("open_issues") val openIssues: Int = 0,
    @SerialName("repo_updated_at") val repoUpdatedAt: String = "",
    @SerialName("vote_score") val voteScore: Int = 0,
    @SerialName("user_vote") val userVote: Boolean? = null
)