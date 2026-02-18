package com.alvinfungai.flower

import app.cash.turbine.test
import com.alvinfungai.flower.data.repository.FakeAuthRepository
import com.alvinfungai.flower.ui.auth.AuthState
import com.alvinfungai.flower.ui.auth.MainViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test


class SocialLoginTest {

    @Test
    fun `state should transition to Authenticated when login is successful`() = runTest {
        val fakeAuthRepository = FakeAuthRepository()
        val viewModel = MainViewModel(fakeAuthRepository)

        viewModel.state.test {
            // 1. check init state
            assertEquals(AuthState.Loading, awaitItem())

            // 2. Simulate login
            fakeAuthRepository.emitState(AuthState.Authenticated)

            // 3. Assert
            assertEquals(AuthState.Authenticated, awaitItem())
        }
    }
}