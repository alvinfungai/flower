package com.alvinfungai.flower.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.data.repository.AuthRepository
import com.alvinfungai.flower.data.repository.SupabaseAuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val repository: AuthRepository = SupabaseAuthRepository(SupabaseClientProvider.client)
) : ViewModel() {
    val state: StateFlow<AuthState> = repository.authState
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Loading
    )
}