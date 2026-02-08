package com.alvinfungai.flower.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state = _state.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Check if a session exists in local persistence
            val supabase = SupabaseClientProvider.client
            val session = supabase.auth.currentSessionOrNull()
            delay(500) // small delay to ensure session object setup correctly
            _state.value = if (session != null) AuthState.Authenticated else AuthState.Unauthenticated
        }
    }

    sealed class AuthState {
        object Loading : AuthState()
        object Authenticated: AuthState()
        object Unauthenticated : AuthState()
    }
}