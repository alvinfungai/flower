package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.data.model.Profile
import kotlinx.coroutines.delay


class FakeProfileRepository : ProfileRepository {
    // In-memory DB
    var fakeProfile = Profile(id = "1", fullName = "Test User", bio = "An Android dev")
    var signOutCalled = false
    var shouldReturnError = false
    var wasUpdateAvatarCalled = false

    var shouldUpdatePictureFail = false
    var shouldFetchProfileFail = false

    override suspend fun getUserProfile(userId: String): Profile? {
        return if (shouldFetchProfileFail) null else fakeProfile
    }

    override suspend fun updateProfile(profile: Profile): Boolean {
        if (shouldReturnError) return false

        // Update in memory profile
        fakeProfile = profile
        return true
    }

    override suspend fun updateProfilePicture(
        userId: String,
        imageBytes: ByteArray
    ): Boolean {
        if (shouldUpdatePictureFail) return false

        // Simulates network latency so StateFlow doesn't conflate
        // delay(100)

        // simulate upload to storage
        val generatedUrl = "https://supababse.com/storage/avatars/$userId/profile.jpg"

        // simulate DB update
        fakeProfile = fakeProfile.copy(avatarUrl = generatedUrl)
        wasUpdateAvatarCalled = true
        return true
    }

    override suspend fun signOut() {
        signOutCalled = true
    }
}