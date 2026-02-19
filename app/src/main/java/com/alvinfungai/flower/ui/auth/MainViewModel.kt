package com.alvinfungai.flower.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.BuildConfig
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.data.repository.AuthRepository
import com.alvinfungai.flower.data.repository.SupabaseAuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val repository: AuthRepository = SupabaseAuthRepository(SupabaseClientProvider.client)
) : ViewModel() {
    val state: StateFlow<AuthState> = repository.authState
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Loading
    )

    // The UI State (For Login Screen ProgressBar/Errors)
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    fun loginWithGithub() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.signInWithGithub()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Github login failed")
            }
        }
    }

    fun loginWithGoogle(context: Context) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // 1. Get Credential from the Android System
                val result = credentialManager.getCredential(context, request)
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

                // 2. Pass the ID Token to the Repository
                repository.signInWithGoogle(googleIdTokenCredential.idToken)

                // 3: Setting Success optional here because MainViewModel's authState flow
                // will automatically see the new session.
                Log.d("Auth", "loginWithGoogle: Google login ok")
            } catch (e: Exception) {
                // handle error
                _uiState.value = UiState.Error(e.message ?: "Google Login Failed")
            }
        }
    }
}