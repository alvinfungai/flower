package com.alvinfungai.flower

import app.cash.turbine.test
import com.alvinfungai.flower.data.repository.FakeProfileRepository
import com.alvinfungai.flower.ui.profile.ProfileUiState
import com.alvinfungai.flower.ui.profile.ProfileViewModel
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var fakeRepo: FakeProfileRepository
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        fakeRepo = FakeProfileRepository()
        viewModel = ProfileViewModel(fakeRepo, mockk(relaxed = true))
    }
    @Test
    fun `saveProfile successfully updates the saved one`() = runTest {
        // 1. Initial Load
        viewModel.loadProfileData("1")

        // 2. Use Turbine to observe the "Stream" of states
        viewModel.uiState.test {
            // 1. Clear the initial state(s)
            // If the repo is instant, Loading might have already skipped to Success
            val initial = awaitItem()
            if (initial is ProfileUiState.Loading) awaitItem()

            // 2. Make the call
            viewModel.updateUserProfile("Updated Name", "New Bio")

            // 3. IMPORTANT: Tell the test to execute all pending coroutines
            advanceUntilIdle()

            // 4. Now await the result
            val result = awaitItem() as ProfileUiState.Success

            // Final Assertions inside the block
            assertEquals("Updated Name", result.profile.fullName)
        }
    }

    @Test
    fun `viewModel signOut should trigger repository signOut`() = runTest {
        val fakeRepo = FakeProfileRepository()
        val viewModel = ProfileViewModel(fakeRepo, mockk(relaxed = true))

        viewModel.signOut()

        assert(fakeRepo.signOutCalled)
    }
}


