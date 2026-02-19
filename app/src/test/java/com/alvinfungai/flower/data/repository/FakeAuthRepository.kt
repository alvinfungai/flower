package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.ui.auth.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow


class FakeAuthRepository : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: Flow<AuthState> = _authState

    var githubCalled = false
    var googleTokenReceived: String? = null

    override suspend fun signInWithGithub() {
        githubCalled = true

        // simulate Supabase updating session
        _authState.value = AuthState.Authenticated
    }

    override suspend fun signInWithGoogle(idToken: String) {
        googleTokenReceived = idToken
        _authState.value = AuthState.Authenticated
    }

    // helper to toggle state in tests
    fun emitState(newStatus: AuthState) {
        _authState.value = newStatus
    }
}