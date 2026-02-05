package com.alvinfungai.flower

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val email: String?,
    val bio: String?,
    @SerialName("full_name") val fullName: String?,
    @SerialName("avatar_url") val avatarUrl: String?
)