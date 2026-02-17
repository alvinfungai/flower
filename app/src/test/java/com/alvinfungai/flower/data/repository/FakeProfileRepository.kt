package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.data.model.Profile


class FakeProfileRepository : ProfileRepository {
    // In-memory DB
    private var fakeProfile = Profile(id = "1", fullName = "Test User", bio = "An Android dev")
    var signOutCalled = false
    var shouldReturnError = false

    override suspend fun getUserProfile(userId: String): Profile? {
        return if (shouldReturnError) null else fakeProfile
    }

    override suspend fun updateProfile(profile: Profile): Boolean {
        if (shouldReturnError) return false

        // Update in memory profile
        fakeProfile = profile
        return true
    }

    override suspend fun signOut() {
        signOutCalled = true
    }
}