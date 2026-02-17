package com.alvinfungai.flower

import app.cash.turbine.test
import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.Technology
import com.alvinfungai.flower.data.repository.FakeProjectRepository
import com.alvinfungai.flower.ui.common.UiState
import com.alvinfungai.flower.ui.project.edit.EditProjectViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class EditProjectViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: EditProjectViewModel
    private lateinit var fakeRepo: FakeProjectRepository

    @Before
    fun setup() {
        fakeRepo = FakeProjectRepository()
        fakeRepo.addFakeProject(
            Project(
                id = "1",
                userId = "user-id",
                title = "Title",
                description = "Desc",
                repoUrl = "Url"
        ))
        viewModel = EditProjectViewModel(fakeRepo)
    }

    @Test
    fun `loadInitialData correctly selects linked tech stack`() = runTest {
        // Arrange: Seed one project and 2 tech objects, but only link 1 tech
        val tech1 = Technology("t1", "Kotlin")
        val tech2 = Technology("t2", "Compose")
        val project = Project("p1", "user1", "url", "test", "desc")

        fakeRepo.seedData(listOf(project), listOf(tech1, tech2))
        fakeRepo.createProject(project, listOf("t1")) // only link Kotlin

        // Act
        viewModel.loadInitialData("p1")

        // Assert
        viewModel.state.test {
            val state = awaitItem()
            if (state is UiState.Success) {
                assertEquals(1, state.data.technologies.size)
                assertEquals("Kotlin", state.data.technologies[0].name)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `updateProjectWithTech should update both project details and technology links`() = runTest {
        // 1. GIVEN: Prepare data
        val projectId = "1"

        // 2. ACT & ASSERT: Observe saveStatus specifically
        viewModel.saveStatus.test {
            viewModel.updateProject(projectId = projectId, "Title", "Desc", "Url", listOf("t1"))
            skipItems(1) // Skips the initial state (Loading)

            advanceUntilIdle()
            assert(awaitItem() is UiState.Deleted)
        }
    }
}