package com.alvinfungai.flower.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Technology(
    val id: String? = null,
    val name: String,
    @SerialName("icon_url") val iconUrl: String? = null
)