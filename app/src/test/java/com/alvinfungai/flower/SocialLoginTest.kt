package com.alvinfungai.flower

import app.cash.turbine.test
import com.alvinfungai.flower.data.repository.FakeAuthRepository
import com.alvinfungai.flower.ui.auth.AuthState
import com.alvinfungai.flower.ui.auth.MainViewModel
import com.alvinfungai.flower.ui.auth.UiState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test


class SocialLoginTest {

    @Test
    fun `loginWithGithub sets loading and then triggers repo`() = runTest {
        val fakeAuthRepository = FakeAuthRepository()
        val viewModel = MainViewModel(fakeAuthRepository)

        viewModel.uiState.test {
            // 1. Simulate login
            viewModel.loginWithGithub()

            // 2. Verify UI shows loading
            assertEquals(UiState.Loading, awaitItem())

            // 3. Verify Repo was actually called
            assert(fakeAuthRepository.githubCalled)
        }

        // 4. Assert auth state change
        viewModel.state.test {
            awaitItem()
            assertEquals(AuthState.Authenticated, awaitItem())
        }
    }

    @Test
    fun `signInWithGoogle passes token to repo and updates state`() = runTest {
        val fakeRepo = FakeAuthRepository()
        val viewModel = MainViewModel(fakeRepo)
        val testToken = "fake_google_id_token"

        viewModel.uiState.test {
            // Credential Manager is an Android framework so for pure unit test call repo
            fakeRepo.signInWithGoogle(testToken)

            awaitItem()

            assertEquals(testToken, fakeRepo.googleTokenReceived)
        }

        viewModel.state.test {
            awaitItem() // Loading AuthState.Loading
            awaitItem() // Next emission expected to be AuthState.Authenticated
            assertEquals(AuthState.Authenticated, viewModel.state.value)
        }
    }
}