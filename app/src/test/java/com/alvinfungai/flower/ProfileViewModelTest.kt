package com.alvinfungai.flower

import android.content.ContentResolver
import android.net.Uri
import com.alvinfungai.flower.data.model.Profile
import com.alvinfungai.flower.data.repository.FakeProfileRepository
import com.alvinfungai.flower.data.repository.ProjectRepository
import com.alvinfungai.flower.ui.common.ImageCompressor
import com.alvinfungai.flower.ui.profile.ProfileUiState
import com.alvinfungai.flower.ui.profile.ProfileViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var viewModel: ProfileViewModel
    private lateinit var fakeProfileRepo: FakeProfileRepository
    private val mockProjectRepo = mockk<ProjectRepository>()
    private val mockCompressor = mockk<ImageCompressor>()

    @Before
    fun setup() {
        fakeProfileRepo = FakeProfileRepository()
        // default empty list for projects to avoid MockK errors
        coEvery { mockProjectRepo.getProjectsByUserId(any()) } returns emptyList()
        fakeProfileRepo.fakeProfile = Profile(id = "user123", fullName = "John Doe", bio = "Hello")

        viewModel = ProfileViewModel(fakeProfileRepo, mockProjectRepo, mockCompressor)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uploadProfileImage - successful upload - updates state to reflects success`() = runTest {
        // 1. Arrange
        val userId = "user123"
        val mockUri = mockk<Uri>()
        val mockContentResolver = mockk<ContentResolver>()

        // Ensure mocks return data
        coEvery { mockProjectRepo.getProjectsByUserId(any()) } returns emptyList()
        every { mockContentResolver.openInputStream(mockUri) } returns "data".toByteArray().inputStream()

        fakeProfileRepo.fakeProfile = Profile(id = userId, fullName = "John")
        fakeProfileRepo.shouldUpdatePictureFail = false

        // Initial load
        viewModel.loadProfileData(userId)
        advanceUntilIdle()

        // 2. Act
        viewModel.uploadProfileImage(userId, mockContentResolver, mockUri)
        advanceUntilIdle() // This will now process the entire sequential chain

        // 3. Assert
        val finalState = viewModel.uiState.value
        assertTrue(finalState is ProfileUiState.Success)
        val successState = finalState as ProfileUiState.Success

        assertTrue("isDoneSaving should be true", successState.isDoneSaving)
        assertFalse("isSaving should be false", successState.isSaving)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uploadProfileImage - repository failure - emits error state`() = runTest {
        // --- 1. ARRANGE ---
        val userId = "user123"
        val mockUri = mockk<Uri>()
        val mockContentResolver = mockk<ContentResolver>()

        // Setup Fake: Profile fetch works, but Upload fails
        fakeProfileRepo.fakeProfile = Profile(id = userId, fullName = "John")
        fakeProfileRepo.shouldFetchProfileFail = false
        fakeProfileRepo.shouldUpdatePictureFail = true // <--- Trigger the specific error

        every { mockContentResolver.openInputStream(mockUri) } returns "data".toByteArray().inputStream()

        // Get into Success state first
        viewModel.loadProfileData(userId)
        advanceUntilIdle()

        // --- 2. ACT ---
        viewModel.uploadProfileImage(userId, mockContentResolver, mockUri)
        advanceUntilIdle()

        // --- 3. ASSERT ---
        val finalState = viewModel.uiState.value
        assertTrue("State should be Error", finalState is ProfileUiState.Error)

        // Now this will match!
        assertEquals("Failed to upload image", (finalState as ProfileUiState.Error).message)
    }
}