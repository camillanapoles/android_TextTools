package com.corphish.quicktools.viewmodels

import com.corphish.quicktools.repository.DefinitionRepository
import com.corphish.quicktools.repository.WordDefinition
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefinitionViewModelTest {
    private lateinit var viewModel: DefinitionViewModel
    private val repository: DefinitionRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DefinitionViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGetDefinition_Success() = runTest {
        val word = "hello"
        val results = listOf(WordDefinition(word, meanings = emptyList()))
        coEvery { repository.getDefinition(word) } returns results

        viewModel.getDefinition(word)
        
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is DefinitionState.Success)
        assertEquals(results, (viewModel.state.value as DefinitionState.Success).results)
    }

    @Test
    fun testGetDefinition_Error() = runTest {
        val word = "unknownword"
        coEvery { repository.getDefinition(word) } returns null

        viewModel.getDefinition(word)
        
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is DefinitionState.Error)
        assertEquals(word, (viewModel.state.value as DefinitionState.Error).message)
    }
}
