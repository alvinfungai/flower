package com.alvinfungai.flower.data.repository

import com.alvinfungai.flower.ui.auth.AuthState
import kotlinx.coroutines.flow.Flow


interface AuthRepository {
    val authState: Flow<AuthState>
}