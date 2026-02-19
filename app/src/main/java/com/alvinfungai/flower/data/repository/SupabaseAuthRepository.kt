package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.ui.auth.AuthState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SupabaseAuthRepository(private val client: SupabaseClient) : AuthRepository {
    override val authState: Flow<AuthState> = client.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            is SessionStatus.Authenticated -> AuthState.Authenticated
            is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
        }
    }

    override suspend fun signInWithGithub() {
        client.auth.signInWith(Github)
    }

    override suspend fun signInWithGoogle(idToken: String) {
        client.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
        }
    }
}