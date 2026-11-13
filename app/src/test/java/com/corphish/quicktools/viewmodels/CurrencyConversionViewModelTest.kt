package com.corphish.quicktools.viewmodels

import com.corphish.quicktools.functions.UnitConverter
import com.corphish.quicktools.repository.CurrencyRepository
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CurrencyConversionViewModelTest {

    private lateinit var viewModel: CurrencyConversionViewModel
    private val unitConverter = UnitConverter()
    private val currencyRepository = mockk<CurrencyRepository>(relaxed = true)

    @Before
    fun setUp() {
        coEvery { currencyRepository.getExchangeRates() } returns emptyList()
        viewModel = CurrencyConversionViewModel(unitConverter, currencyRepository)
    }

    @Test
    fun testInit_PureNumber() {
        viewModel.init("100")
        assertEquals("100", viewModel.inputValue.value)
        assertEquals(UnitConverter.Unit.USD, viewModel.fromUnit.value)
        assertEquals(UnitConverter.Unit.ILS, viewModel.toUnit.value)
        // Default rate is 1.0 if not fetched
        assertEquals("100", viewModel.result.value)
    }

    @Test
    fun testInit_WithCurrency() {
        viewModel.init("100 USD")
        assertEquals("100", viewModel.inputValue.value)
        assertEquals(UnitConverter.Unit.USD, viewModel.fromUnit.value)
        assertEquals(UnitConverter.Unit.ILS, viewModel.toUnit.value)
    }

    @Test
    fun testOnInputValueChange() {
        viewModel.onInputValueChange("50")
        assertEquals("50", viewModel.result.value)
    }

    @Test
    fun testSwapUnits() {
        viewModel.swapUnits()
        assertEquals(UnitConverter.Unit.ILS, viewModel.fromUnit.value)
        assertEquals(UnitConverter.Unit.USD, viewModel.toUnit.value)
    }
}
