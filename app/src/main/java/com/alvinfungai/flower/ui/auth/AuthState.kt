package com.alvinfungai.flower.ui.auth

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated: AuthState()
    object Unauthenticated : AuthState()
}