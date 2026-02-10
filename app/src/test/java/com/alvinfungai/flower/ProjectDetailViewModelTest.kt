package com.alvinfungai.flower

import app.cash.turbine.test
import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.Technology
import com.alvinfungai.flower.data.repository.FakeProjectRepository
import com.alvinfungai.flower.ui.common.UiState
import com.alvinfungai.flower.ui.project.detail.ProjectDetailViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ProjectDetailViewModelTest {

    // This rule swaps Dispatchers.Main for a TestDispatcher
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ProjectDetailViewModel
    private lateinit var fakeRepo: FakeProjectRepository

    @Before
    fun setup() {
        fakeRepo = FakeProjectRepository()
        viewModel = ProjectDetailViewModel(fakeRepo)
    }

    @Test
    fun `when fetching details fails, state is Error`() = runTest {

        // Arrange
        fakeRepo.shouldReturnError = true // Simulate a failure

        // Act
        viewModel.fetchProjectDetails("any-id")

        // Assert
        viewModel.state.test {
            val item = awaitItem()
            assert(item is UiState.Error)
            assertEquals("Test error", (item as UiState.Error).message)
        }
    }

    @Test
    fun `when fetching details is successful, state is Success`() = runTest {
        // Arrange
        val tech1 = Technology("t1", "Kotlin")
        val project = Project("p1", "user1", "url", "test", "desc")

        fakeRepo.seedData(listOf(project), listOf(tech1))
        fakeRepo.createProject(project, listOf("t1"))

        fakeRepo.shouldReturnError = false
        fakeRepo.getProjectById("p1")

        // Act
        viewModel.fetchProjectDetails("p1")

        // Assert
        viewModel.state.test {
            val item = awaitItem()
            assert(item is UiState.Success)
            assertEquals("p1", (item as UiState.Success).data.id)
            assertEquals("Kotlin", item.data.technologies[0].name)
        }
    }

    @Test
    fun `when delete fails, state is Error`() = runTest {
        // Arrange
        fakeRepo.shouldReturnError = true

        // Act
        viewModel.deleteProject("any-id") {}

        // Assert
        viewModel.state.test {
            val item = awaitItem()
            assert(item is UiState.Error)
            assertEquals("Error deleting", (item as UiState.Error).message)
        }
    }

    @Test
    fun `when delete is successful, state updates to Deleted`() = runTest {
        // Arrange
        fakeRepo.shouldReturnError = false

        // Act & Assert => state first emits Loading, awaitItem() #1 then Deleted awaitItem() #2
        viewModel.state.test {
            viewModel.deleteProject("any-id") {}
//            val item = awaitItem()
            // Assert we hit Loading first
            assertEquals(UiState.Loading, awaitItem())

            // Assert we hit Deleted next
            assertEquals(UiState.Deleted, awaitItem())
        }
    }
}