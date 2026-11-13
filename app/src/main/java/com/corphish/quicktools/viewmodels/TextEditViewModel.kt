package com.corphish.quicktools.viewmodels

import androidx.lifecycle.ViewModel
import com.corphish.quicktools.usecases.ClipboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TextEditViewModel @Inject constructor(
    private val clipboardUseCase: ClipboardUseCase
): ViewModel() {
    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()

    fun setText(newText: String) {
        _text.value = newText
    }

    fun copyToClipboard() {
        clipboardUseCase.copyToClipboard(_text.value)
    }
}
