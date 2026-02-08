package com.alvinfungai.flower

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Hint:
 * Problem:
 * tests are most likely to fail due to threading.
 * In Android, viewModelScope.launch uses Dispatchers.Main by default.
 * When you run a Unit Test, the "Main" thread (which belongs to the Android OS) doesn't exist,
 * and the coroutine might not finish before your assert line runs.
 *
 * Solution:
 * define the 'MainDispatcherRule' class to handle threading during unit tests
 */

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}