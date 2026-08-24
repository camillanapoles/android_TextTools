package com.corphish.quicktools.functions

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnitConverter @Inject constructor() {

    enum class Category {
        LENGTH, WEIGHT, TEMPERATURE, VOLUME, CURRENCY
    }

    enum class Unit(val label: String, val category: Category, val factorToBase: Double = 1.0) {
        // Length (Base: Meter)
        METER("Meter (m)", Category.LENGTH, 1.0),
        KILOMETER("Kilometer (km)", Category.LENGTH, 1000.0),
        CENTIMETER("Centimeter (cm)", Category.LENGTH, 0.01),
        MILE("Mile (mi)", Category.LENGTH, 1609.34),
        FOOT("Foot (ft)", Category.LENGTH, 0.3048),
        INCH("Inch (in)", Category.LENGTH, 0.0254),

        // Weight (Base: Gram)
        GRAM("Gram (g)", Category.WEIGHT, 1.0),
        KILOGRAM("Kilogram (kg)", Category.WEIGHT, 1000.0),
        POUND("Pound (lb)", Category.WEIGHT, 453.592),
        OUNCE("Ounce (oz)", Category.WEIGHT, 28.3495),

        // Temperature (Base: Celsius)
        CELSIUS("Celsius (°C)", Category.TEMPERATURE),
        FAHRENHEIT("Fahrenheit (°F)", Category.TEMPERATURE),
        KELVIN("Kelvin (K)", Category.TEMPERATURE),

        // Volume (Base: Milliliter)
        MILLILITER("Milliliter (ml)", Category.VOLUME, 1.0),
        LITER("Liter (l)", Category.VOLUME, 1000.0),
        CUP("Cup (cup)", Category.VOLUME, 236.588),
        TABLESPOON("Tablespoon (tbsp)", Category.VOLUME, 14.7868),
        TEASPOON("Teaspoon (tsp)", Category.VOLUME, 4.92892),
        FLUID_OUNCE("Fluid Ounce (fl oz)", Category.VOLUME, 29.5735),

        // Currency (Base: ILS)
        ILS("Israeli Shekel (ILS)", Category.CURRENCY, 1.0),
        USD("US Dollar (USD)", Category.CURRENCY, 1.0),
        EUR("Euro (EUR)", Category.CURRENCY, 1.0),
        GBP("British Pound (GBP)", Category.CURRENCY, 1.0),
        JPY("Japanese Yen (JPY)", Category.CURRENCY, 1.0),
        CAD("Canadian Dollar (CAD)", Category.CURRENCY, 1.0),
        AUD("Australian Dollar (AUD)", Category.CURRENCY, 1.0),
        CHF("Swiss Franc (CHF)", Category.CURRENCY, 1.0),
        SEK("Swedish Krona (SEK)", Category.CURRENCY, 1.0),
        NOK("Norwegian Krone (NOK)", Category.CURRENCY, 1.0),
        DKK("Danish Krone (DKK)", Category.CURRENCY, 1.0),
        ZAR("South African Rand (ZAR)", Category.CURRENCY, 1.0),
        JOD("Jordanian Dinar (JOD)", Category.CURRENCY, 1.0),
        LBP("Lebanese Pound (LBP)", Category.CURRENCY, 1.0),
        EGP("Egyptian Pound (EGP)", Category.CURRENCY, 1.0),
    }

    private var currencyRates: Map<String, Double> = emptyMap()
    private var currencyUnits: Map<String, Int> = emptyMap()

    fun updateCurrencyRates(rates: Map<String, Double>, units: Map<String, Int>) {
        this.currencyRates = rates
        this.currencyUnits = units
    }

    fun convert(value: Double, from: Unit, to: Unit): Double {
        if (from == to) return value
        if (from.category != to.category) return value

        return when (from.category) {
            Category.TEMPERATURE -> convertTemperature(value, from, to)
            Category.CURRENCY -> convertCurrency(value, from, to)
            else -> {
                val baseValue = value * from.factorToBase
                baseValue / to.factorToBase
            }
        }
    }

    private fun convertCurrency(value: Double, from: Unit, to: Unit): Double {
        if (currencyRates.isEmpty() && from != Unit.ILS && to != Unit.ILS) return value

        val fromRate = if (from == Unit.ILS) 1.0 else (currencyRates[from.name] ?: 1.0) / (currencyUnits[from.name] ?: 1)
        val toRate = if (to == Unit.ILS) 1.0 else (currencyRates[to.name] ?: 1.0) / (currencyUnits[to.name] ?: 1)

        val ilsValue = value * fromRate
        return ilsValue / toRate
    }

    private fun convertTemperature(value: Double, from: Unit, to: Unit): Double {
        val celsiusValue = when (from) {
            Unit.CELSIUS -> value
            Unit.FAHRENHEIT -> (value - 32.0) * 5.0 / 9.0
            Unit.KELVIN -> value - 273.15
            else -> value
        }

        return when (to) {
            Unit.CELSIUS -> celsiusValue
            Unit.FAHRENHEIT -> (celsiusValue * 9.0 / 5.0) + 32.0
            Unit.KELVIN -> celsiusValue + 273.15
            else -> celsiusValue
        }
    }

    private val unitMappings = mapOf(
        "km" to Unit.KILOMETER, "kilometer" to Unit.KILOMETER, "kilometers" to Unit.KILOMETER,
        "m" to Unit.METER, "meter" to Unit.METER, "meters" to Unit.METER,
        "cm" to Unit.CENTIMETER, "centimeter" to Unit.CENTIMETER, "centimeters" to Unit.CENTIMETER,
        "mi" to Unit.MILE, "mile" to Unit.MILE, "miles" to Unit.MILE,
        "ft" to Unit.FOOT, "foot" to Unit.FOOT, "feet" to Unit.FOOT,
        "in" to Unit.INCH, "inch" to Unit.INCH, "inches" to Unit.INCH,
        "g" to Unit.GRAM, "gram" to Unit.GRAM, "grams" to Unit.GRAM,
        "kg" to Unit.KILOGRAM, "kilogram" to Unit.KILOGRAM, "kilograms" to Unit.KILOGRAM,
        "lb" to Unit.POUND, "lbs" to Unit.POUND, "pound" to Unit.POUND, "pounds" to Unit.POUND,
        "oz" to Unit.OUNCE, "ounce" to Unit.OUNCE, "ounces" to Unit.OUNCE,
        "c" to Unit.CELSIUS, "°c" to Unit.CELSIUS, "celsius" to Unit.CELSIUS,
        "f" to Unit.FAHRENHEIT, "°f" to Unit.FAHRENHEIT, "fahrenheit" to Unit.FAHRENHEIT,
        "k" to Unit.KELVIN, "kelvin" to Unit.KELVIN,
        "ml" to Unit.MILLILITER, "milliliter" to Unit.MILLILITER, "milliliters" to Unit.MILLILITER,
        "l" to Unit.LITER, "liter" to Unit.LITER, "liters" to Unit.LITER,
        "cup" to Unit.CUP, "cups" to Unit.CUP,
        "tbsp" to Unit.TABLESPOON, "tablespoon" to Unit.TABLESPOON, "tablespoons" to Unit.TABLESPOON,
        "tsp" to Unit.TEASPOON, "teaspoon" to Unit.TEASPOON, "teaspoons" to Unit.TEASPOON,
        "fl oz" to Unit.FLUID_OUNCE, "fluid ounce" to Unit.FLUID_OUNCE, "fluid ounces" to Unit.FLUID_OUNCE,
        "$" to Unit.USD, "usd" to Unit.USD, "dollar" to Unit.USD, "dollars" to Unit.USD,
        "€" to Unit.EUR, "eur" to Unit.EUR, "euro" to Unit.EUR, "euros" to Unit.EUR,
        "₪" to Unit.ILS, "ils" to Unit.ILS, "nis" to Unit.ILS, "shekel" to Unit.ILS, "shekels" to Unit.ILS,
        "£" to Unit.GBP, "gbp" to Unit.GBP,
        "jpy" to Unit.JPY, "yen" to Unit.JPY,
        "cad" to Unit.CAD, "aud" to Unit.AUD, "chf" to Unit.CHF, "sek" to Unit.SEK,
        "nok" to Unit.NOK, "dkk" to Unit.DKK, "zar" to Unit.ZAR, "jod" to Unit.JOD,
        "lbp" to Unit.LBP, "egp" to Unit.EGP
    )

    private val defaultTargets = mapOf(
        Unit.KILOMETER to Unit.MILE, Unit.MILE to Unit.KILOMETER,
        Unit.METER to Unit.FOOT, Unit.FOOT to Unit.METER,
        Unit.CENTIMETER to Unit.INCH, Unit.INCH to Unit.CENTIMETER,
        Unit.KILOGRAM to Unit.POUND, Unit.POUND to Unit.KILOGRAM,
        Unit.GRAM to Unit.OUNCE, Unit.OUNCE to Unit.GRAM,
        Unit.CELSIUS to Unit.FAHRENHEIT, Unit.FAHRENHEIT to Unit.CELSIUS, Unit.KELVIN to Unit.CELSIUS,
        Unit.MILLILITER to Unit.FLUID_OUNCE, Unit.FLUID_OUNCE to Unit.MILLILITER,
        Unit.LITER to Unit.CUP, Unit.CUP to Unit.LITER,
        Unit.TABLESPOON to Unit.TEASPOON, Unit.TEASPOON to Unit.TABLESPOON,
        Unit.USD to Unit.ILS, Unit.ILS to Unit.USD,
        Unit.EUR to Unit.ILS, Unit.GBP to Unit.ILS
    )

    fun detectUnits(text: String): List<ConversionSuggestion> {
        val unitNames = unitMappings.keys.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }
        val numberRegex = "(\\d+(?:[.,]\\d+)?)"
        val separatorRegex = "(\\s*[x*×]\\s*)"
        val numberPart = "$numberRegex(?:$separatorRegex$numberRegex)?(?:$separatorRegex$numberRegex)?"
        
        // Pattern matches: [unit] 1.23 [x|*|× 4.56 [x|*|× 7.89]] [unit]
        // Boundary-aware regex that supports prefix and suffix units
        // Group 1: prefix unit, Group 2-6: value part, Group 7: suffix unit
        val regex = Regex("(?<!\\w)(?:($unitNames)\\s*)?$numberPart(?:\\s*-?\\s*($unitNames))?(?!\\w)", RegexOption.IGNORE_CASE)
        
        return regex.findAll(text).mapNotNull { match ->
            val prefixUnitStr = match.groups[1]?.value?.lowercase()
            val suffixUnitStr = match.groups[7]?.value?.lowercase()
            
            if (prefixUnitStr == null && suffixUnitStr == null) return@mapNotNull null
            
            // Prefer suffix unit if both are present, otherwise use prefix
            val unitStr = suffixUnitStr ?: prefixUnitStr ?: return@mapNotNull null
            val fromUnit = unitMappings[unitStr] ?: return@mapNotNull null
            val toUnit = defaultTargets[fromUnit] ?: return@mapNotNull null
            
            // Reconstruct the dimension/value part (groups 2, 3, 4, 5, 6)
            val sb = StringBuilder()
            for (i in 2..6) {
                sb.append(match.groupValues[i])
            }
            
            val valuePart = sb.toString()
            val resultStr = convertValueOrDimension(valuePart, fromUnit, toUnit, 2) ?: return@mapNotNull null
            
            // Find v1 for compatibility
            val v1 = match.groupValues[2].replace(',', '.').toDoubleOrNull() ?: 0.0
            
            // Reconstruct fromValueStr
            val fromValueStr = valuePart.replace(',', '.').replace(Regex("\\s+"), " ")
            
            ConversionSuggestion(
                fromValue = v1,
                fromUnit = fromUnit,
                toUnit = toUnit,
                convertedValueStr = resultStr,
                fromValueStr = fromValueStr
            )
        }.toList()
    }

    fun convertValueOrDimension(input: String, from: Unit, to: Unit, precision: Int = 4): String? {
        val numberRegex = "(\\d+(?:[.,]\\d+)?)"
        val separatorRegex = "(\\s*[x*×]\\s*)"
        val regex = Regex("^$numberRegex(?:$separatorRegex$numberRegex)?(?:$separatorRegex$numberRegex)?$", RegexOption.IGNORE_CASE)
        
        val match = regex.find(input.trim()) ?: return null
        
        val values = mutableListOf<Double>()
        val separators = mutableListOf<String>()
        
        val v1 = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        values.add(v1)
        
        if (match.groupValues[3].isNotEmpty()) {
            separators.add(match.groupValues[2])
            values.add(match.groupValues[3].replace(',', '.').toDoubleOrNull() ?: return null)
        }
        
        if (match.groupValues[5].isNotEmpty()) {
            separators.add(match.groupValues[4])
            values.add(match.groupValues[5].replace(',', '.').toDoubleOrNull() ?: return null)
        }
        
        val convertedValues = values.map { value ->
            val converted = convert(value, from, to)
            java.lang.String.format(java.util.Locale.US, "%.${precision}f", converted).trimEnd('0').trimEnd('.')
        }
        
        return convertedValues.indices.joinToString("") { i ->
            if (i == 0) convertedValues[i] else separators[i-1] + convertedValues[i]
        }
    }

    data class ConversionSuggestion(
        val fromValue: Double,
        val fromUnit: Unit,
        val toUnit: Unit,
        val convertedValueStr: String,
        val fromValueStr: String
    )
}
