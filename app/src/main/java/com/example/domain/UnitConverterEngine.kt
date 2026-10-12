package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class UnitCategory(val displayName: String, val iconName: String) {
    LENGTH("Length", "Straighten"),
    MASS("Mass / Weight", "FitnessCenter"),
    TEMPERATURE("Temperature", "Thermostat"),
    VOLUME("Volume", "Opacity"),
    AREA("Area", "SquareFoot"),
    SPEED("Speed", "Speed"),
    DATA("Digital Storage", "SdCard"),
    TIME("Time", "Schedule"),
    ANGLE("Angle", "Explore")
}

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val category: UnitCategory,
    val toBaseMultiplier: Double, // conversion to SI base unit
    val isCustomFormula: Boolean = false
)

object UnitConverterEngine {

    val categories = UnitCategory.values().toList()

    val unitsByCategory: Map<UnitCategory, List<ConversionUnit>> = mapOf(
        UnitCategory.LENGTH to listOf(
            ConversionUnit("m", "Meter", "m", UnitCategory.LENGTH, 1.0),
            ConversionUnit("km", "Kilometer", "km", UnitCategory.LENGTH, 1000.0),
            ConversionUnit("cm", "Centimeter", "cm", UnitCategory.LENGTH, 0.01),
            ConversionUnit("mm", "Millimeter", "mm", UnitCategory.LENGTH, 0.001),
            ConversionUnit("mi", "Mile", "mi", UnitCategory.LENGTH, 1609.344),
            ConversionUnit("yd", "Yard", "yd", UnitCategory.LENGTH, 0.9144),
            ConversionUnit("ft", "Foot", "ft", UnitCategory.LENGTH, 0.3048),
            ConversionUnit("in", "Inch", "in", UnitCategory.LENGTH, 0.0254),
            ConversionUnit("nmi", "Nautical Mile", "NM", UnitCategory.LENGTH, 1852.0)
        ),
        UnitCategory.MASS to listOf(
            ConversionUnit("kg", "Kilogram", "kg", UnitCategory.MASS, 1.0),
            ConversionUnit("g", "Gram", "g", UnitCategory.MASS, 0.001),
            ConversionUnit("mg", "Milligram", "mg", UnitCategory.MASS, 0.000001),
            ConversionUnit("lb", "Pound", "lb", UnitCategory.MASS, 0.45359237),
            ConversionUnit("oz", "Ounce", "oz", UnitCategory.MASS, 0.02834952),
            ConversionUnit("t", "Metric Ton", "t", UnitCategory.MASS, 1000.0),
            ConversionUnit("st", "Stone", "st", UnitCategory.MASS, 6.35029)
        ),
        UnitCategory.TEMPERATURE to listOf(
            ConversionUnit("c", "Celsius", "°C", UnitCategory.TEMPERATURE, 1.0, isCustomFormula = true),
            ConversionUnit("f", "Fahrenheit", "°F", UnitCategory.TEMPERATURE, 1.0, isCustomFormula = true),
            ConversionUnit("k", "Kelvin", "K", UnitCategory.TEMPERATURE, 1.0, isCustomFormula = true)
        ),
        UnitCategory.VOLUME to listOf(
            ConversionUnit("l", "Liter", "L", UnitCategory.VOLUME, 1.0),
            ConversionUnit("ml", "Milliliter", "mL", UnitCategory.VOLUME, 0.001),
            ConversionUnit("gal_us", "Gallon (US)", "gal", UnitCategory.VOLUME, 3.78541),
            ConversionUnit("qt_us", "Quart (US)", "qt", UnitCategory.VOLUME, 0.946353),
            ConversionUnit("pt_us", "Pint (US)", "pt", UnitCategory.VOLUME, 0.473176),
            ConversionUnit("cup_us", "Cup (US)", "cup", UnitCategory.VOLUME, 0.236588),
            ConversionUnit("floz_us", "Fluid Ounce (US)", "fl oz", UnitCategory.VOLUME, 0.0295735),
            ConversionUnit("m3", "Cubic Meter", "m³", UnitCategory.VOLUME, 1000.0)
        ),
        UnitCategory.AREA to listOf(
            ConversionUnit("sqm", "Square Meter", "m²", UnitCategory.AREA, 1.0),
            ConversionUnit("sqkm", "Square Kilometer", "km²", UnitCategory.AREA, 1000000.0),
            ConversionUnit("ha", "Hectare", "ha", UnitCategory.AREA, 10000.0),
            ConversionUnit("ac", "Acre", "ac", UnitCategory.AREA, 4046.86),
            ConversionUnit("sqft", "Square Foot", "ft²", UnitCategory.AREA, 0.092903),
            ConversionUnit("sqyd", "Square Yard", "yd²", UnitCategory.AREA, 0.836127),
            ConversionUnit("sqmi", "Square Mile", "mi²", UnitCategory.AREA, 2589988.11)
        ),
        UnitCategory.SPEED to listOf(
            ConversionUnit("mps", "Meters per sec", "m/s", UnitCategory.SPEED, 1.0),
            ConversionUnit("kmh", "Kilometers per hr", "km/h", UnitCategory.SPEED, 0.277778),
            ConversionUnit("mph", "Miles per hr", "mph", UnitCategory.SPEED, 0.44704),
            ConversionUnit("knot", "Knot", "kn", UnitCategory.SPEED, 0.514444),
            ConversionUnit("fps", "Feet per sec", "ft/s", UnitCategory.SPEED, 0.3048)
        ),
        UnitCategory.DATA to listOf(
            ConversionUnit("b", "Byte", "B", UnitCategory.DATA, 1.0),
            ConversionUnit("kb", "Kilobyte", "KB", UnitCategory.DATA, 1024.0),
            ConversionUnit("mb", "Megabyte", "MB", UnitCategory.DATA, 1048576.0),
            ConversionUnit("gb", "Gigabyte", "GB", UnitCategory.DATA, 1073741824.0),
            ConversionUnit("tb", "Terabyte", "TB", UnitCategory.DATA, 1099511627776.0)
        ),
        UnitCategory.TIME to listOf(
            ConversionUnit("s", "Second", "s", UnitCategory.TIME, 1.0),
            ConversionUnit("min", "Minute", "min", UnitCategory.TIME, 60.0),
            ConversionUnit("h", "Hour", "h", UnitCategory.TIME, 3600.0),
            ConversionUnit("d", "Day", "d", UnitCategory.TIME, 86400.0),
            ConversionUnit("wk", "Week", "wk", UnitCategory.TIME, 604800.0),
            ConversionUnit("yr", "Year (365d)", "yr", UnitCategory.TIME, 31536000.0)
        ),
        UnitCategory.ANGLE to listOf(
            ConversionUnit("deg", "Degree", "°", UnitCategory.ANGLE, 1.0),
            ConversionUnit("rad", "Radian", "rad", UnitCategory.ANGLE, 57.2957795),
            ConversionUnit("grad", "Gradian", "gon", UnitCategory.ANGLE, 0.9)
        )
    )

    fun convert(valueStr: String, fromUnit: ConversionUnit, toUnit: ConversionUnit): String {
        val rawNum = valueStr.replace(",", "").toDoubleOrNull() ?: return "0"

        if (fromUnit.id == toUnit.id) {
            return formatConverterResult(rawNum)
        }

        if (fromUnit.category == UnitCategory.TEMPERATURE) {
            return convertTemperature(rawNum, fromUnit.id, toUnit.id)
        }

        // Base SI conversion
        val baseValue = rawNum * fromUnit.toBaseMultiplier
        val targetValue = baseValue / toUnit.toBaseMultiplier
        return formatConverterResult(targetValue)
    }

    private fun convertTemperature(value: Double, fromId: String, toId: String): String {
        // Convert to Celsius first
        val c = when (fromId) {
            "c" -> value
            "f" -> (value - 32.0) * 5.0 / 9.0
            "k" -> value - 273.15
            else -> value
        }

        val target = when (toId) {
            "c" -> c
            "f" -> (c * 9.0 / 5.0) + 32.0
            "k" -> c + 273.15
            else -> c
        }
        return formatConverterResult(target)
    }

    private fun formatConverterResult(num: Double): String {
        if (num.isNaN() || num.isInfinite()) return "ERROR"
        if (num == 0.0) return "0"

        val abs = Math.abs(num)
        return if (abs >= 1e9 || (abs < 1e-4 && abs > 0)) {
            String.format(Locale.US, "%.5E", num)
        } else {
            val bd = BigDecimal(num, MathContext.DECIMAL64).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros()
            val plain = bd.toPlainString()
            val parts = plain.split(".")
            val intStr = try {
                val longVal = parts[0].toLong()
                val symbols = DecimalFormatSymbols(Locale.US)
                DecimalFormat("#,###", symbols).format(longVal)
            } catch (e: Exception) {
                parts[0]
            }
            if (parts.size > 1) "$intStr.${parts[1]}" else intStr
        }
    }
}
