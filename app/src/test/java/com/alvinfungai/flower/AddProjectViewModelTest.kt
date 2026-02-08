package com.alvinfungai.flower

import com.alvinfungai.flower.data.repository.FakeProjectRepository
import com.alvinfungai.flower.ui.common.UiState
import com.alvinfungai.flower.ui.project.add.AddProjectViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class AddProjectViewModelTest {

    // This rule swaps Dispatchers.Main for a TestDispatcher
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AddProjectViewModel
    private lateinit var fakeRepo: FakeProjectRepository


    @Before
    fun setup() {
        fakeRepo = FakeProjectRepository()
        viewModel = AddProjectViewModel(fakeRepo) // Pass fake Repo into viewmodel
    }

    @Test
    fun `saveProject successfully updates state to Success`() = runTest {

        // Arrange
        val title = "Example App"

        // Act
        viewModel.saveProject("user1", title, "url", "desc", emptyList())

        // Assert
        assert(viewModel.saveStatus.value  is UiState.Success)

        // verify repo received data
        assertEquals(title, fakeRepo.lastCreatedProject?.title)
        assertEquals(1, fakeRepo.getProjectCount())
    }

    @Test
    fun `saveProject failure updates state to error`() = runTest {
        fakeRepo.shouldReturnError = true // simulate a crash

        viewModel.saveProject("user1", "title", "desc", "url", emptyList())

        assert(viewModel.saveStatus.value is UiState.Error)
        assertEquals("Insert failed", (viewModel.saveStatus.value as UiState.Error).message)
    }

}