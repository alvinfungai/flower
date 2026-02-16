package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.data.model.Profile


interface ProfileRepository {
    suspend fun getUserProfile(userId: String): Profile?
    suspend fun updateProfile(profile: Profile): Boolean
    suspend fun signOut()
}