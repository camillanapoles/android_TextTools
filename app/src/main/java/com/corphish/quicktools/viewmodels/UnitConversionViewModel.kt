package com.corphish.quicktools.viewmodels

import androidx.lifecycle.ViewModel
import com.corphish.quicktools.functions.UnitConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UnitConversionViewModel @Inject constructor(
    private val unitConverter: UnitConverter
) : ViewModel() {

    private val _inputValue = MutableStateFlow("")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()

    private val _fromUnit = MutableStateFlow(UnitConverter.Unit.METER)
    val fromUnit: StateFlow<UnitConverter.Unit> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow(UnitConverter.Unit.KILOMETER)
    val toUnit: StateFlow<UnitConverter.Unit> = _toUnit.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _category = MutableStateFlow(UnitConverter.Category.LENGTH)
    val category: StateFlow<UnitConverter.Category> = _category.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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

    fun onCategoryChange(unitCategory: UnitConverter.Category) {
        _category.value = unitCategory
        // Update units to match category
        val units = UnitConverter.Unit.entries.filter { it.category == unitCategory }
        _fromUnit.value = units.first()
        _toUnit.value = if (units.size > 1) units[1] else units.first()
        
        performConversion()
    }

    fun swapUnits() {
        val temp = _fromUnit.value
        _fromUnit.value = _toUnit.value
        _toUnit.value = temp
        performConversion()
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
        val unitDetection = detections.find { it.fromUnit.category != UnitConverter.Category.CURRENCY }
        if (unitDetection != null) {
            _category.value = unitDetection.fromUnit.category
            _fromUnit.value = unitDetection.fromUnit
            _toUnit.value = unitDetection.toUnit
            _inputValue.value = unitDetection.fromValueStr
            
            performConversion()
        }
    }
}
