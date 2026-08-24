package com.corphish.quicktools.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corphish.quicktools.repository.DefinitionRepository
import com.corphish.quicktools.repository.WordDefinition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DefinitionState {
    object Idle : DefinitionState()
    object Loading : DefinitionState()
    data class Success(val results: List<WordDefinition>) : DefinitionState()
    data class Error(val message: String) : DefinitionState()
}

@HiltViewModel
class DefinitionViewModel @Inject constructor(
    private val definitionRepository: DefinitionRepository
) : ViewModel() {

    private val _state = MutableStateFlow<DefinitionState>(DefinitionState.Idle)
    val state = _state.asStateFlow()

    fun getDefinition(word: String) {
        if (word.isBlank()) return
        
        viewModelScope.launch {
            _state.value = DefinitionState.Loading
            val result = definitionRepository.getDefinition(word)
            if (result != null && result.isNotEmpty()) {
                _state.value = DefinitionState.Success(result)
            } else {
                _state.value = DefinitionState.Error(word)
            }
        }
    }
}
