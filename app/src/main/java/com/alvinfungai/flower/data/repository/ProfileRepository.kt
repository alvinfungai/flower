package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.data.model.Profile
import com.alvinfungai.flower.data.model.Project


interface ProfileRepository {
    suspend fun getUserProfile(userId: String): Profile?
    suspend fun updateProfile(profile: Profile): Boolean
    suspend fun updateProfilePicture(userId: String, imageBytes: ByteArray): Boolean
    suspend fun signOut()
}