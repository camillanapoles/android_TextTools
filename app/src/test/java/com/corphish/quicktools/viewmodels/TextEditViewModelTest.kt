package com.corphish.quicktools.viewmodels

import com.corphish.quicktools.usecases.ClipboardUseCase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TextEditViewModelTest {
    private lateinit var viewModel: TextEditViewModel
    private val clipboardUseCase: ClipboardUseCase = mockk(relaxed = true)

    @Before
    fun setUp() {
        viewModel = TextEditViewModel(clipboardUseCase)
    }

    @Test
    fun testSetText() {
        val newText = "Hello World"
        viewModel.setText(newText)
        assertEquals(newText, viewModel.text.value)
    }

    @Test
    fun testCopyToClipboard() {
        val text = "Copy this"
        viewModel.setText(text)
        viewModel.copyToClipboard()
        verify { clipboardUseCase.copyToClipboard(text) }
    }
}
