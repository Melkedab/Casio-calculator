package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.CalculatorEngine
import com.example.domain.CalculationResult
import com.example.domain.UnitCategory
import com.example.domain.UnitConverterEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SolarCalc", appName)
    }

    @Test
    fun `calculator engine basic operations`() {
        val engine = CalculatorEngine(taxRatePercent = 10.0)
        val res = engine.evaluateExpression("15 + 25 * 2")
        assertTrue(res is CalculationResult.Success)
        val success = res as CalculationResult.Success
        assertEquals("65", success.display)
    }

    @Test
    fun `calculator engine tax calculation`() {
        val engine = CalculatorEngine(taxRatePercent = 10.0)
        val (total, tax) = engine.calculateTaxPlus("100")
        assertEquals("110", total)
        assertEquals("10", tax)
    }

    @Test
    fun `unit converter length conversion`() {
        val lengthUnits = UnitConverterEngine.unitsByCategory[UnitCategory.LENGTH]!!
        val meter = lengthUnits.first { it.id == "m" }
        val km = lengthUnits.first { it.id == "km" }
        val result = UnitConverterEngine.convert("2500", meter, km)
        assertEquals("2.5", result)
    }
}
