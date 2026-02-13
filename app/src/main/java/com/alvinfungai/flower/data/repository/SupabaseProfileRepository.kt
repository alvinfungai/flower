package com.alvinfungai.flower.data.repository

import android.util.Log
import com.alvinfungai.flower.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from


class SupabaseProfileRepository(private val supabase: SupabaseClient) : ProfileRepository {
    override suspend fun getUserProfile(userId: String): Profile? {
        return try {
            supabase.from("profiles").select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            Log.e("AUTH_DEBUG", "Error fetching profile: ${e.message}")
            null
        }
    }

    override suspend fun updateProfile(profile: Profile): Boolean {
        return try {
            supabase.from("profiles").update(profile) {
                filter { eq("id", profile.id) }
            }
            true
        } catch (e: Exception) {
            Log.e("AUTH_DEBUG", "Update profile failed: ${e.message}")
            false
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }
}