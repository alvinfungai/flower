package com.alvinfungai.flower.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectTechJoin(
    // The ID of the join row
    @SerialName("id")
    val id: Int? = null,

    // The foreign key to the projects table
    @SerialName("project_id")
    val projectId: String,

    // The foreign key to the technologies table
    @SerialName("tech_id")
    val techId: String
)