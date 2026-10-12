package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CalculationEntity
import com.example.domain.AudioHapticsEngine
import com.example.domain.CalculatorEngine
import com.example.domain.CalculationResult
import com.example.domain.ConversionUnit
import com.example.domain.SolarPowerManager
import com.example.domain.SolarState
import com.example.domain.UnitCategory
import com.example.domain.UnitConverterEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

enum class AppMode {
    CALCULATOR,
    CONVERTER,
    HISTORY_TAPE
}

data class CalculatorUiState(
    val displayPrimary: String = "0",
    val displayFormula: String = "",
    val activeOperator: String? = null,
    val hasMemory: Boolean = false,
    val hasGrandTotal: Boolean = false,
    val isError: Boolean = false,
    val taxRate: Double = 8.25,
    val statusBadge: String = "", // e.g. "M", "GT", "TAX+", "ERROR"
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val appMode: AppMode = AppMode.CALCULATOR,
    val isScientificExpanded: Boolean = false
)

data class ConverterUiState(
    val selectedCategory: UnitCategory = UnitCategory.LENGTH,
    val fromUnit: ConversionUnit = UnitConverterEngine.unitsByCategory[UnitCategory.LENGTH]!![0],
    val toUnit: ConversionUnit = UnitConverterEngine.unitsByCategory[UnitCategory.LENGTH]!![6], // Meter to Foot
    val inputValue: String = "1",
    val resultValue: String = "3.28084",
    val isEditingFrom: Boolean = true
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val calculationDao = db.calculationDao()

    private val engine = CalculatorEngine()
    private val solarManager = SolarPowerManager(application)
    private val audioHaptics = AudioHapticsEngine(application)

    val solarState: StateFlow<SolarState> = solarManager.solarState
    val calculationHistory: StateFlow<List<CalculationEntity>> = calculationDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _calcState = MutableStateFlow(CalculatorUiState())
    val calcState: StateFlow<CalculatorUiState> = _calcState.asStateFlow()

    private val _converterState = MutableStateFlow(ConverterUiState())
    val converterState: StateFlow<ConverterUiState> = _converterState.asStateFlow()

    private var currentInput: String = "0"
    private var firstOperand: BigDecimal? = null
    private var pendingOperator: String? = null
    private var isNewNumber: Boolean = true
    private var lastFullExpression: String = ""

    init {
        solarManager.startListening()
        recomputeConverter()
    }

    override fun onCleared() {
        super.onCleared()
        solarManager.stopListening()
    }

    // ==========================================
    // Calculator Keypad Actions
    // ==========================================

    fun onDigit(digit: String) {
        audioHaptics.playKeyClick()
        if (_calcState.value.isError) {
            onAllClear()
        }

        if (isNewNumber) {
            currentInput = if (digit == ".") "0." else digit
            isNewNumber = false
        } else {
            if (digit == ".") {
                if (!currentInput.contains(".")) {
                    currentInput += "."
                }
            } else {
                if (currentInput == "0") {
                    currentInput = digit
                } else if (currentInput.replace(",", "").replace(".", "").replace("-", "").length < 12) {
                    currentInput += digit
                }
            }
        }
        updatePrimaryDisplay()
    }

    fun onOperator(op: String) {
        audioHaptics.playKeyClick()
        if (_calcState.value.isError) return

        val currentVal = engine.parseNumber(currentInput) ?: BigDecimal.ZERO

        if (firstOperand != null && pendingOperator != null && !isNewNumber) {
            // Chained operation: calculate interim result
            executePendingCalculation()
        } else {
            firstOperand = currentVal
        }

        pendingOperator = op
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            activeOperator = op,
            displayFormula = "${engine.formatNumber(firstOperand ?: currentVal)} $op",
            statusBadge = op
        )
    }

    fun onEquals() {
        audioHaptics.playKeyClick()
        if (_calcState.value.isError) return
        if (pendingOperator == null && firstOperand == null) return

        val op = pendingOperator ?: return
        val secondOperand = engine.parseNumber(currentInput) ?: BigDecimal.ZERO
        val first = firstOperand ?: return

        val expr = "${engine.formatNumber(first)} $op ${engine.formatNumber(secondOperand)}"
        val result = engine.evaluateExpression(expr)

        when (result) {
            is CalculationResult.Success -> {
                val formatted = result.display
                lastFullExpression = "$expr ="
                currentInput = formatted
                firstOperand = null
                pendingOperator = null
                isNewNumber = true

                _calcState.value = _calcState.value.copy(
                    displayPrimary = formatted,
                    displayFormula = lastFullExpression,
                    activeOperator = null,
                    isError = false,
                    hasGrandTotal = engine.hasGrandTotal(),
                    statusBadge = if (engine.hasGrandTotal()) "GT" else ""
                )

                saveHistory(expr, formatted, "BASIC")
            }
            is CalculationResult.Error -> {
                triggerError(result.message)
            }
        }
    }

    private fun executePendingCalculation() {
        val op = pendingOperator ?: return
        val secondOperand = engine.parseNumber(currentInput) ?: BigDecimal.ZERO
        val first = firstOperand ?: return

        val expr = "${engine.formatNumber(first)} $op ${engine.formatNumber(secondOperand)}"
        val result = engine.evaluateExpression(expr)

        if (result is CalculationResult.Success) {
            firstOperand = result.value
            currentInput = result.display
            _calcState.value = _calcState.value.copy(
                displayPrimary = result.display,
                hasGrandTotal = engine.hasGrandTotal()
            )
        } else if (result is CalculationResult.Error) {
            triggerError(result.message)
        }
    }

    fun onAllClear() {
        audioHaptics.playKeyClick()
        currentInput = "0"
        firstOperand = null
        pendingOperator = null
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            displayPrimary = "0",
            displayFormula = "",
            activeOperator = null,
            isError = false,
            statusBadge = if (engine.hasMemory()) "M" else ""
        )
    }

    fun onClear() {
        audioHaptics.playKeyClick()
        currentInput = "0"
        isNewNumber = true
        updatePrimaryDisplay()
    }

    fun onBackspace() {
        audioHaptics.playKeyClick()
        if (_calcState.value.isError || isNewNumber) return

        currentInput = if (currentInput.length > 1) {
            currentInput.substring(0, currentInput.length - 1)
        } else {
            "0"
        }
        if (currentInput == "-" || currentInput.isEmpty()) {
            currentInput = "0"
        }
        updatePrimaryDisplay()
    }

    fun onToggleSign() {
        audioHaptics.playKeyClick()
        if (_calcState.value.isError) return
        currentInput = engine.negate(currentInput)
        updatePrimaryDisplay()
    }

    fun onPercent() {
        audioHaptics.playKeyClick()
        if (_calcState.value.isError) return

        val base = firstOperand
        val op = pendingOperator

        if (base != null && op != null) {
            val res = engine.calculatePercent(base.toPlainString(), op, currentInput)
            val expr = "${engine.formatNumber(base)} $op $currentInput%"
            currentInput = res
            firstOperand = null
            pendingOperator = null
            isNewNumber = true
            _calcState.value = _calcState.value.copy(
                displayPrimary = res,
                displayFormula = "$expr =",
                activeOperator = null
            )
            saveHistory(expr, res, "PERCENT")
        } else {
            val res = engine.calculatePercent(currentInput, "*", "1")
            saveHistory("$currentInput %", res, "PERCENT")
            currentInput = res
            isNewNumber = true
            updatePrimaryDisplay()
        }
    }

    fun onSquareRoot() {
        audioHaptics.playKeyClick()
        if (_calcState.value.isError) return
        val raw = currentInput
        val res = engine.calculateSquareRoot(raw)
        if (res == "ERROR") {
            triggerError("ERROR")
        } else {
            val expr = "√($raw)"
            saveHistory(expr, res, "SCIENTIFIC")
            currentInput = res
            isNewNumber = true
            _calcState.value = _calcState.value.copy(
                displayPrimary = res,
                displayFormula = "$expr =",
                isError = false
            )
        }
    }

    fun onSquare() {
        audioHaptics.playKeyClick()
        if (_calcState.value.isError) return
        val raw = currentInput
        val res = engine.calculateSquare(raw)
        val expr = "($raw)²"
        saveHistory(expr, res, "SCIENTIFIC")
        currentInput = res
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            displayPrimary = res,
            displayFormula = "$expr =",
            isError = false
        )
    }

    fun onReciprocal() {
        audioHaptics.playKeyClick()
        if (_calcState.value.isError) return
        val raw = currentInput
        val res = engine.calculateReciprocal(raw)
        if (res == "ERROR") {
            triggerError("DIV BY 0")
        } else {
            val expr = "1/($raw)"
            saveHistory(expr, res, "SCIENTIFIC")
            currentInput = res
            isNewNumber = true
            _calcState.value = _calcState.value.copy(
                displayPrimary = res,
                displayFormula = "$expr =",
                isError = false
            )
        }
    }

    // Memory operations
    fun onMemoryAdd() {
        audioHaptics.playKeyClick()
        engine.addToMemory(currentInput)
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            hasMemory = engine.hasMemory(),
            statusBadge = "M+"
        )
    }

    fun onMemorySubtract() {
        audioHaptics.playKeyClick()
        engine.subtractFromMemory(currentInput)
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            hasMemory = engine.hasMemory(),
            statusBadge = "M-"
        )
    }

    fun onMemoryRecall() {
        audioHaptics.playKeyClick()
        val mem = engine.recallMemory()
        currentInput = mem
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            displayPrimary = mem,
            statusBadge = "MR"
        )
    }

    fun onMemoryClear() {
        audioHaptics.playKeyClick()
        engine.clearMemory()
        _calcState.value = _calcState.value.copy(
            hasMemory = false,
            statusBadge = "MC"
        )
    }

    // Grand Total (GT)
    fun onGrandTotal() {
        audioHaptics.playKeyClick()
        val gt = engine.recallGrandTotal()
        currentInput = gt
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            displayPrimary = gt,
            displayFormula = "GRAND TOTAL",
            statusBadge = "GT"
        )
    }

    fun onClearGrandTotal() {
        audioHaptics.playKeyClick()
        engine.clearGrandTotal()
        _calcState.value = _calcState.value.copy(
            hasGrandTotal = false,
            statusBadge = ""
        )
    }

    // Tax calculation keys (Casio standard TAX+ / TAX-)
    fun onTaxPlus() {
        audioHaptics.playKeyClick()
        val (total, tax) = engine.calculateTaxPlus(currentInput)
        val expr = "$currentInput + TAX (${_calcState.value.taxRate}%)"
        saveHistory(expr, total, "TAX")
        currentInput = total
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            displayPrimary = total,
            displayFormula = "$expr [Tax: $tax]",
            statusBadge = "TAX+"
        )
    }

    fun onTaxMinus() {
        audioHaptics.playKeyClick()
        val (beforeTax, tax) = engine.calculateTaxMinus(currentInput)
        val expr = "$currentInput - TAX (${_calcState.value.taxRate}%)"
        saveHistory(expr, beforeTax, "TAX")
        currentInput = beforeTax
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            displayPrimary = beforeTax,
            displayFormula = "$expr [Tax: $tax]",
            statusBadge = "TAX-"
        )
    }

    fun setTaxRate(newRate: Double) {
        engine.setTaxRate(newRate)
        _calcState.value = _calcState.value.copy(taxRate = newRate)
    }

    fun toggleScientific() {
        audioHaptics.playKeyClick()
        _calcState.value = _calcState.value.copy(
            isScientificExpanded = !_calcState.value.isScientificExpanded
        )
    }

    private fun triggerError(msg: String) {
        currentInput = msg
        firstOperand = null
        pendingOperator = null
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            displayPrimary = "E",
            displayFormula = msg,
            isError = true,
            statusBadge = "ERROR"
        )
    }

    private fun updatePrimaryDisplay() {
        val num = engine.parseNumber(currentInput)
        val display = if (num != null) {
            if (currentInput.endsWith(".")) {
                "${engine.formatNumber(num)}."
            } else if (currentInput.contains(".") && currentInput.endsWith("0")) {
                currentInput
            } else {
                engine.formatNumber(num)
            }
        } else {
            currentInput
        }

        _calcState.value = _calcState.value.copy(
            displayPrimary = display,
            statusBadge = if (engine.hasMemory()) "M" else ""
        )
    }

    private fun saveHistory(expr: String, res: String, tag: String) {
        viewModelScope.launch {
            try {
                calculationDao.insertCalculation(
                    CalculationEntity(
                        expression = expr,
                        result = res,
                        tag = tag
                    )
                )
                calculationDao.trimOldHistory()
            } catch (_: Exception) {}
        }
    }

    fun recallHistoryEntry(entity: CalculationEntity) {
        audioHaptics.playKeyClick()
        currentInput = entity.result
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            displayPrimary = entity.result,
            displayFormula = "${entity.expression} =",
            appMode = AppMode.CALCULATOR
        )
    }

    fun deleteHistoryEntry(entity: CalculationEntity) {
        viewModelScope.launch {
            calculationDao.deleteCalculation(entity)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            calculationDao.clearAll()
        }
    }

    // ==========================================
    // Mode Switching
    // ==========================================

    fun setAppMode(mode: AppMode) {
        audioHaptics.playKeyClick()
        _calcState.value = _calcState.value.copy(appMode = mode)
    }

    // ==========================================
    // Unit Converter Logic
    // ==========================================

    fun selectConverterCategory(category: UnitCategory) {
        audioHaptics.playKeyClick()
        val units = UnitConverterEngine.unitsByCategory[category] ?: return
        val from = units[0]
        val to = if (units.size > 1) units[1] else units[0]
        _converterState.value = _converterState.value.copy(
            selectedCategory = category,
            fromUnit = from,
            toUnit = to
        )
        recomputeConverter()
    }

    fun selectFromUnit(unit: ConversionUnit) {
        audioHaptics.playKeyClick()
        _converterState.value = _converterState.value.copy(fromUnit = unit)
        recomputeConverter()
    }

    fun selectToUnit(unit: ConversionUnit) {
        audioHaptics.playKeyClick()
        _converterState.value = _converterState.value.copy(toUnit = unit)
        recomputeConverter()
    }

    fun swapConverterUnits() {
        audioHaptics.playKeyClick()
        val current = _converterState.value
        _converterState.value = current.copy(
            fromUnit = current.toUnit,
            toUnit = current.fromUnit
        )
        recomputeConverter()
    }

    fun onConverterDigit(d: String) {
        audioHaptics.playKeyClick()
        val cur = _converterState.value.inputValue
        val updated = if (cur == "0" && d != ".") {
            d
        } else if (d == "." && cur.contains(".")) {
            cur
        } else if (cur.length < 10) {
            cur + d
        } else {
            cur
        }
        _converterState.value = _converterState.value.copy(inputValue = updated)
        recomputeConverter()
    }

    fun onConverterBackspace() {
        audioHaptics.playKeyClick()
        val cur = _converterState.value.inputValue
        val updated = if (cur.length > 1) cur.substring(0, cur.length - 1) else "0"
        _converterState.value = _converterState.value.copy(inputValue = updated)
        recomputeConverter()
    }

    fun onConverterClear() {
        audioHaptics.playKeyClick()
        _converterState.value = _converterState.value.copy(inputValue = "0")
        recomputeConverter()
    }

    private fun recomputeConverter() {
        val s = _converterState.value
        val res = UnitConverterEngine.convert(s.inputValue, s.fromUnit, s.toUnit)
        _converterState.value = s.copy(resultValue = res)
    }

    fun sendConverterResultToCalc() {
        audioHaptics.playKeyClick()
        val res = _converterState.value.resultValue
        currentInput = res
        isNewNumber = true
        _calcState.value = _calcState.value.copy(
            displayPrimary = res,
            displayFormula = "CONVERTED [${_converterState.value.fromUnit.symbol} → ${_converterState.value.toUnit.symbol}]",
            appMode = AppMode.CALCULATOR
        )
        saveHistory(
            "${_converterState.value.inputValue} ${_converterState.value.fromUnit.symbol} → ${_converterState.value.toUnit.symbol}",
            res,
            "CONVERSION"
        )
    }

    fun importCalcDisplayToConverter() {
        audioHaptics.playKeyClick()
        val primary = _calcState.value.displayPrimary.replace(",", "")
        val num = primary.toDoubleOrNull()
        if (num != null) {
            _converterState.value = _converterState.value.copy(inputValue = primary)
            recomputeConverter()
        }
    }

    // ==========================================
    // Sound / Haptics / Solar
    // ==========================================

    fun toggleSound() {
        val next = !_calcState.value.isSoundEnabled
        audioHaptics.isSoundEnabled = next
        _calcState.value = _calcState.value.copy(isSoundEnabled = next)
    }

    fun toggleHaptics() {
        val next = !_calcState.value.isHapticsEnabled
        audioHaptics.isHapticsEnabled = next
        _calcState.value = _calcState.value.copy(isHapticsEnabled = next)
    }

    fun toggleSolarTorch() {
        audioHaptics.playKeyClick()
        solarManager.toggleSimulatedTorch()
    }

    fun setSimulatedLux(lux: Float) {
        solarManager.setSimulatedLight(lux)
    }
}
