package com.corphish.quicktools.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corphish.quicktools.functions.UnitConverter
import com.corphish.quicktools.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyConversionViewModel @Inject constructor(
    private val unitConverter: UnitConverter,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _inputValue = MutableStateFlow("")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()

    private val _fromUnit = MutableStateFlow(UnitConverter.Unit.USD)
    val fromUnit: StateFlow<UnitConverter.Unit> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow(UnitConverter.Unit.ILS)
    val toUnit: StateFlow<UnitConverter.Unit> = _toUnit.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchRates()
    }

    fun onInputValueChange(value: String) {
        _inputValue.value = value
        performConversion()
    }

    fun onFromUnitChange(unit: UnitConverter.Unit) {
        _fromUnit.value = unit
        performConversion()
    }

    fun onToUnitChange(unit: UnitConverter.Unit) {
        _toUnit.value = unit
        performConversion()
    }

    fun swapUnits() {
        val temp = _fromUnit.value
        _fromUnit.value = _toUnit.value
        _toUnit.value = temp
        performConversion()
    }

    private fun fetchRates() {
        _isLoading.value = true
        viewModelScope.launch {
            val ratesList = currencyRepository.getExchangeRates()
            if (ratesList != null) {
                unitConverter.updateCurrencyRates(
                    ratesList.associate { it.code to it.rate },
                    ratesList.associate { it.code to it.unit }
                )
                performConversion()
            }
            _isLoading.value = false
        }
    }

    private fun performConversion() {
        val res = unitConverter.convertValueOrDimension(_inputValue.value, _fromUnit.value, _toUnit.value)
        _result.value = res ?: ""
    }

    fun init(text: String) {
        val value = text.replace(',', '.').toDoubleOrNull()
        if (value != null) {
            _inputValue.value = text
            performConversion()
            return
        }
        
        // Try to detect units in the text
        val detections = unitConverter.detectUnits(text)
        val currencyDetection = detections.find { it.fromUnit.category == UnitConverter.Category.CURRENCY }
        if (currencyDetection != null) {
            _fromUnit.value = currencyDetection.fromUnit
            _toUnit.value = currencyDetection.toUnit
            _inputValue.value = currencyDetection.fromValueStr
            performConversion()
        }
    }
}
