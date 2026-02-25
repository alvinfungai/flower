package com.alvinfungai.flower.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoteRequest(
    @SerialName("p_project_id") val projectId: String,
    @SerialName("p_is_upvote") val isUpvote: Boolean
)