package com.corphish.quicktools.viewmodels

import com.corphish.quicktools.functions.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UnitConversionViewModelTest {

    private lateinit var viewModel: UnitConversionViewModel
    private val unitConverter = UnitConverter()

    @Before
    fun setUp() {
        viewModel = UnitConversionViewModel(unitConverter)
    }

    @Test
    fun testInit_PureNumber() {
        viewModel.init("123.45")
        assertEquals("123.45", viewModel.inputValue.value)
        assertEquals(UnitConverter.Category.LENGTH, viewModel.category.value)
        assertEquals(UnitConverter.Unit.METER, viewModel.fromUnit.value)
        assertEquals(UnitConverter.Unit.KILOMETER, viewModel.toUnit.value)
        assertEquals("0.1235", viewModel.result.value)
    }

    @Test
    fun testInit_PureNumberWithComma() {
        viewModel.init("123,45")
        assertEquals("123,45", viewModel.inputValue.value)
        assertEquals("0.1235", viewModel.result.value)
    }

    @Test
    fun testInit_WithUnit() {
        viewModel.init("5km")
        assertEquals("5", viewModel.inputValue.value)
        assertEquals(UnitConverter.Category.LENGTH, viewModel.category.value)
        assertEquals(UnitConverter.Unit.KILOMETER, viewModel.fromUnit.value)
        assertEquals(UnitConverter.Unit.MILE, viewModel.toUnit.value)
        assertEquals("3.1069", viewModel.result.value)
    }

    @Test
    fun testInit_WithUnitAndComma() {
        viewModel.init("5,5km")
        assertEquals("5.5", viewModel.inputValue.value)
        assertEquals(UnitConverter.Unit.KILOMETER, viewModel.fromUnit.value)
        assertEquals("3.4176", viewModel.result.value)
    }

    @Test
    fun testInit_WithDimension() {
        viewModel.init("9x5-inch")
        assertEquals("9x5", viewModel.inputValue.value)
        assertEquals(UnitConverter.Unit.INCH, viewModel.fromUnit.value)
        assertEquals("22.86x12.7", viewModel.result.value)
    }

    @Test
    fun testOnInputValueChange() {
        viewModel.onInputValueChange("10")
        assertEquals("0.01", viewModel.result.value)
    }

    @Test
    fun testSwapUnits() {
        viewModel.onInputValueChange("1")
        assertEquals("0.001", viewModel.result.value)
        
        viewModel.swapUnits()
        assertEquals(UnitConverter.Unit.KILOMETER, viewModel.fromUnit.value)
        assertEquals(UnitConverter.Unit.METER, viewModel.toUnit.value)
        assertEquals("1000", viewModel.result.value)
    }
}
