package com.alvinfungai.flower.data.repository

import android.util.Log
import android.widget.Toast
import com.alvinfungai.flower.data.model.Profile
import com.alvinfungai.flower.data.remote.SupabaseClientProvider.client
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import timber.log.Timber


class SupabaseProfileRepository(private val supabase: SupabaseClient) : ProfileRepository {
    override suspend fun getUserProfile(userId: String): Profile? {
        return try {
            supabase.from("profiles").select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            Timber.e("Error fetching profile: ${e.message}")
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
            Timber.e("Update profile failed: ${e.message}")
            false
        }
    }

    override suspend fun updateProfilePicture(
        userId: String,
        imageBytes: ByteArray
    ): Boolean {
        return try {
            val bucket = client.storage.from("avatars")
            val fileName ="$userId/profile.jpg"

            // 1. Upload to storage
            // The SDK provides a DSL for these options
            bucket.upload(fileName, imageBytes) {
                upsert = true
            }

            // 2. Get the public URL
            // No network call yet, just build the string
            val imageUrl = bucket.publicUrl(fileName)

            // 3. Update the 'profiles' table in the DB
            // find by 'id' and update 'avatar_url' field
            client.from("profiles").update(
                mapOf("avatar_url" to imageUrl)
            ) {
                filter {
                    eq("id", userId)
                }
            }
            true
        } catch (e: Exception) {
            println("Profile upload error: ${e.localizedMessage}")
            false
        }

    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }
}