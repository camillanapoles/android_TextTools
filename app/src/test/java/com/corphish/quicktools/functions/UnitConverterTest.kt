package com.corphish.quicktools.functions

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    private val unitConverter = UnitConverter()

    @Test
    fun testLengthConversion() {
        // 1 km to meters
        assertEquals(1000.0, unitConverter.convert(1.0, UnitConverter.Unit.KILOMETER, UnitConverter.Unit.METER), 0.001)
        
        // 1 meter to centimeters
        assertEquals(100.0, unitConverter.convert(1.0, UnitConverter.Unit.METER, UnitConverter.Unit.CENTIMETER), 0.001)
        
        // 1 mile to kilometers
        assertEquals(1.60934, unitConverter.convert(1.0, UnitConverter.Unit.MILE, UnitConverter.Unit.KILOMETER), 0.001)
    }

    @Test
    fun testWeightConversion() {
        // 1 kg to grams
        assertEquals(1000.0, unitConverter.convert(1.0, UnitConverter.Unit.KILOGRAM, UnitConverter.Unit.GRAM), 0.001)
        
        // 1 pound to grams
        assertEquals(453.592, unitConverter.convert(1.0, UnitConverter.Unit.POUND, UnitConverter.Unit.GRAM), 0.001)
    }

    @Test
    fun testTemperatureConversion() {
        // 0 Celsius to Fahrenheit
        assertEquals(32.0, unitConverter.convert(0.0, UnitConverter.Unit.CELSIUS, UnitConverter.Unit.FAHRENHEIT), 0.001)
        
        // 100 Celsius to Kelvin
        assertEquals(373.15, unitConverter.convert(100.0, UnitConverter.Unit.CELSIUS, UnitConverter.Unit.KELVIN), 0.001)
        
        // 32 Fahrenheit to Celsius
        assertEquals(0.0, unitConverter.convert(32.0, UnitConverter.Unit.FAHRENHEIT, UnitConverter.Unit.CELSIUS), 0.001)
        
        // 273.15 Kelvin to Celsius
        assertEquals(0.0, unitConverter.convert(273.15, UnitConverter.Unit.KELVIN, UnitConverter.Unit.CELSIUS), 0.001)
    }

    @Test
    fun testVolumeConversion() {
        // 1 Liter to ml
        assertEquals(1000.0, unitConverter.convert(1.0, UnitConverter.Unit.LITER, UnitConverter.Unit.MILLILITER), 0.001)
        
        // 1 cup to ml
        assertEquals(236.588, unitConverter.convert(1.0, UnitConverter.Unit.CUP, UnitConverter.Unit.MILLILITER), 0.001)
        
        // 1 tbsp to tsp
        assertEquals(3.0, unitConverter.convert(1.0, UnitConverter.Unit.TABLESPOON, UnitConverter.Unit.TEASPOON), 0.001)
        
        // 8 fl oz to cup
        assertEquals(1.0, unitConverter.convert(8.0, UnitConverter.Unit.FLUID_OUNCE, UnitConverter.Unit.CUP), 0.001)
    }

    @Test
    fun testSameUnitConversion() {
        assertEquals(10.5, unitConverter.convert(10.5, UnitConverter.Unit.METER, UnitConverter.Unit.METER), 0.001)
    }

    @Test
    fun testDifferentCategoryConversion() {
        // Should return original value if categories don't match (safety check)
        assertEquals(1.0, unitConverter.convert(1.0, UnitConverter.Unit.METER, UnitConverter.Unit.GRAM), 0.001)
    }

    @Test
    fun testDetection() {
        val text = "It is 25C outside and I ran 5km. My height is 1,75m. The box is 9x5-inch."
        val suggestions = unitConverter.detectUnits(text)
        
        assertEquals(4, suggestions.size)
        
        // 25C
        assertEquals(25.0, suggestions[0].fromValue, 0.001)
        assertEquals(UnitConverter.Unit.CELSIUS, suggestions[0].fromUnit)
        assertEquals(UnitConverter.Unit.FAHRENHEIT, suggestions[0].toUnit)
        assertEquals("77", suggestions[0].convertedValueStr)
        
        // 5km
        assertEquals(5.0, suggestions[1].fromValue, 0.001)
        assertEquals(UnitConverter.Unit.KILOMETER, suggestions[1].fromUnit)
        assertEquals(UnitConverter.Unit.MILE, suggestions[1].toUnit)
        assertEquals("3.11", suggestions[1].convertedValueStr)

        // 1,75m
        assertEquals(1.75, suggestions[2].fromValue, 0.001)
        assertEquals(UnitConverter.Unit.METER, suggestions[2].fromUnit)
        assertEquals("5.74", suggestions[2].convertedValueStr)

        // 9x5-inch
        assertEquals(9.0, suggestions[3].fromValue, 0.001)
        assertEquals(UnitConverter.Unit.INCH, suggestions[3].fromUnit)
        assertEquals(UnitConverter.Unit.CENTIMETER, suggestions[3].toUnit)
        assertEquals("22.86x12.7", suggestions[3].convertedValueStr)
        assertEquals("9x5", suggestions[3].fromValueStr)
    }

    @Test
    fun testDimensionDetection() {
        // 3D dimension
        val text = "Room size is 10 x 20 x 8 ft"
        val suggestions = unitConverter.detectUnits(text)
        
        assertEquals(1, suggestions.size)
        assertEquals("3.05 x 6.1 x 2.44", suggestions[0].convertedValueStr)
        assertEquals("10 x 20 x 8", suggestions[0].fromValueStr)
        
        // With different separator
        val text2 = "Area is 2*4m"
        val suggestions2 = unitConverter.detectUnits(text2)
        assertEquals(1, suggestions2.size)
        assertEquals("6.56*13.12", suggestions2[0].convertedValueStr)
    }

    @Test
    fun testCookingDetection() {
        val text = "Add 2 cups of milk and 1 tbsp of sugar."
        val suggestions = unitConverter.detectUnits(text)
        
        assertEquals(2, suggestions.size)
        
        // 2 cups
        assertEquals(2.0, suggestions[0].fromValue, 0.001)
        assertEquals(UnitConverter.Unit.CUP, suggestions[0].fromUnit)
        assertEquals(UnitConverter.Unit.LITER, suggestions[0].toUnit)
        
        // 1 tbsp
        assertEquals(1.0, suggestions[1].fromValue, 0.001)
        assertEquals(UnitConverter.Unit.TABLESPOON, suggestions[1].fromUnit)
        assertEquals(UnitConverter.Unit.TEASPOON, suggestions[1].toUnit)
        assertEquals("3", suggestions[1].convertedValueStr)
    }

    @Test
    fun testCurrencyAndPrefixDetection() {
        val text = "The price is $100 and I have €50 left. Another item is 200$. I also have 50 shekels."
        val suggestions = unitConverter.detectUnits(text)
        
        // $100, €50, 200$, 50 shekels
        assertEquals(4, suggestions.size)
        
        // $100
        assertEquals(100.0, suggestions[0].fromValue, 0.001)
        assertEquals(UnitConverter.Unit.USD, suggestions[0].fromUnit)
        
        // €50
        assertEquals(50.0, suggestions[1].fromValue, 0.001)
        assertEquals(UnitConverter.Unit.EUR, suggestions[1].fromUnit)

        // 200$
        assertEquals(200.0, suggestions[2].fromValue, 0.001)
        assertEquals(UnitConverter.Unit.USD, suggestions[2].fromUnit)

        // 50 shekels
        assertEquals(50.0, suggestions[3].fromValue, 0.001)
        assertEquals(UnitConverter.Unit.ILS, suggestions[3].fromUnit)
    }
}
