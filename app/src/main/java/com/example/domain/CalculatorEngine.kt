package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CalculatorEngine(
    private var taxRatePercent: Double = 8.25
) {
    var memoryValue: BigDecimal = BigDecimal.ZERO
        private set
    var grandTotal: BigDecimal = BigDecimal.ZERO
        private set

    fun getTaxRate(): Double = taxRatePercent
    fun setTaxRate(rate: Double) {
        if (rate >= 0) {
            taxRatePercent = rate
        }
    }

    fun hasMemory(): Boolean = memoryValue.compareTo(BigDecimal.ZERO) != 0
    fun hasGrandTotal(): Boolean = grandTotal.compareTo(BigDecimal.ZERO) != 0

    fun addToMemory(valueStr: String) {
        val num = parseNumber(valueStr) ?: return
        memoryValue = memoryValue.add(num)
    }

    fun subtractFromMemory(valueStr: String) {
        val num = parseNumber(valueStr) ?: return
        memoryValue = memoryValue.subtract(num)
    }

    fun recallMemory(): String = formatNumber(memoryValue)

    fun clearMemory() {
        memoryValue = BigDecimal.ZERO
    }

    fun recallGrandTotal(): String = formatNumber(grandTotal)

    fun clearGrandTotal() {
        grandTotal = BigDecimal.ZERO
    }

    fun accumulateGrandTotal(value: BigDecimal) {
        grandTotal = grandTotal.add(value)
    }

    fun calculateTaxPlus(valueStr: String): Pair<String, String> {
        val num = parseNumber(valueStr) ?: return Pair(valueStr, "0")
        val rate = BigDecimal.valueOf(taxRatePercent).divide(BigDecimal("100"), MathContext.DECIMAL64)
        val taxAmount = num.multiply(rate).setScale(2, RoundingMode.HALF_UP)
        val total = num.add(taxAmount)
        return Pair(formatNumber(total), formatNumber(taxAmount))
    }

    fun calculateTaxMinus(valueStr: String): Pair<String, String> {
        val num = parseNumber(valueStr) ?: return Pair(valueStr, "0")
        val rate = BigDecimal.ONE.add(BigDecimal.valueOf(taxRatePercent).divide(BigDecimal("100"), MathContext.DECIMAL64))
        val beforeTax = num.divide(rate, 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP)
        val taxAmount = num.subtract(beforeTax)
        return Pair(formatNumber(beforeTax), formatNumber(taxAmount))
    }

    fun calculatePercent(baseStr: String, operator: String, percentStr: String): String {
        val base = parseNumber(baseStr) ?: BigDecimal.ZERO
        val pct = parseNumber(percentStr) ?: BigDecimal.ZERO
        val hundredth = BigDecimal("100")

        val result = when (operator) {
            "+" -> {
                // e.g. 200 + 10% = 200 + 20 = 220
                val amount = base.multiply(pct).divide(hundredth, MathContext.DECIMAL64)
                base.add(amount)
            }
            "−", "-" -> {
                // e.g. 200 - 10% = 200 - 20 = 180
                val amount = base.multiply(pct).divide(hundredth, MathContext.DECIMAL64)
                base.subtract(amount)
            }
            "×", "*" -> {
                // e.g. 200 * 10% = 20
                base.multiply(pct).divide(hundredth, MathContext.DECIMAL64)
            }
            "÷", "/" -> {
                // e.g. 200 / 10% = 2000
                if (pct.compareTo(BigDecimal.ZERO) == 0) return "ERROR"
                base.divide(pct.divide(hundredth, MathContext.DECIMAL64), MathContext.DECIMAL64)
            }
            else -> {
                // Simple percentage of the number
                base.multiply(pct).divide(hundredth, MathContext.DECIMAL64)
            }
        }
        return formatNumber(result)
    }

    fun calculateSquareRoot(valueStr: String): String {
        val num = parseNumber(valueStr) ?: return "ERROR"
        if (num < BigDecimal.ZERO) return "ERROR"
        val d = num.toDouble()
        val sqrt = Math.sqrt(d)
        return formatNumber(BigDecimal(sqrt, MathContext.DECIMAL64))
    }

    fun calculateSquare(valueStr: String): String {
        val num = parseNumber(valueStr) ?: return "ERROR"
        return formatNumber(num.multiply(num, MathContext.DECIMAL64))
    }

    fun calculateReciprocal(valueStr: String): String {
        val num = parseNumber(valueStr) ?: return "ERROR"
        if (num.compareTo(BigDecimal.ZERO) == 0) return "ERROR"
        return formatNumber(BigDecimal.ONE.divide(num, MathContext.DECIMAL64))
    }

    fun negate(valueStr: String): String {
        if (valueStr == "0" || valueStr.isEmpty()) return "0"
        return if (valueStr.startsWith("-")) {
            valueStr.substring(1)
        } else {
            "-$valueStr"
        }
    }

    /**
     * Evaluates a full mathematical expression like "12 + 3 × 4 - 2"
     */
    fun evaluateExpression(expr: String): CalculationResult {
        try {
            val sanitized = expr
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .trim()

            if (sanitized.isEmpty()) {
                return CalculationResult.Success("0", BigDecimal.ZERO)
            }

            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) {
                return CalculationResult.Success("0", BigDecimal.ZERO)
            }

            val rpn = infixToPostfix(tokens)
            val result = evaluatePostfix(rpn)

            accumulateGrandTotal(result)
            return CalculationResult.Success(formatNumber(result), result)
        } catch (e: ArithmeticException) {
            return CalculationResult.Error("DIV BY 0")
        } catch (e: Exception) {
            return CalculationResult.Error("ERROR")
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            if (c.isWhitespace()) {
                i++
                continue
            }
            if (c.isDigit() || c == '.') {
                sb.append(c)
                while (i + 1 < expr.length && (expr[i + 1].isDigit() || expr[i + 1] == '.')) {
                    i++
                    sb.append(expr[i])
                }
                tokens.add(sb.toString())
                sb.clear()
            } else if (c == '-' && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/", "("))) {
                // Negative unary number
                sb.append('-')
                i++
                while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
                sb.clear()
                continue
            } else if (c in listOf('+', '-', '*', '/', '(', ')')) {
                tokens.add(c.toString())
            }
            i++
        }
        return tokens
    }

    private fun infixToPostfix(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        fun precedence(op: String): Int = when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            else -> 0
        }

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token == "(" -> stack.addLast(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.last() != "(") {
                        output.add(stack.removeLast())
                    }
                    if (stack.isNotEmpty() && stack.last() == "(") {
                        stack.removeLast()
                    }
                }
                else -> {
                    while (stack.isNotEmpty() && stack.last() != "(" && precedence(stack.last()) >= precedence(token)) {
                        output.add(stack.removeLast())
                    }
                    stack.addLast(token)
                }
            }
        }
        while (stack.isNotEmpty()) {
            output.add(stack.removeLast())
        }
        return output
    }

    private fun evaluatePostfix(postfix: List<String>): BigDecimal {
        val stack = ArrayDeque<BigDecimal>()
        for (token in postfix) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.addLast(BigDecimal(token))
            } else {
                if (stack.size < 2) throw IllegalArgumentException("Malformed")
                val b = stack.removeLast()
                val a = stack.removeLast()
                val res = when (token) {
                    "+" -> a.add(b, MathContext.DECIMAL64)
                    "-" -> a.subtract(b, MathContext.DECIMAL64)
                    "*" -> a.multiply(b, MathContext.DECIMAL64)
                    "/" -> {
                        if (b.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Divide by zero")
                        a.divide(b, MathContext.DECIMAL64)
                    }
                    else -> BigDecimal.ZERO
                }
                stack.addLast(res)
            }
        }
        return stack.lastOrNull() ?: BigDecimal.ZERO
    }

    fun parseNumber(str: String): BigDecimal? {
        return try {
            BigDecimal(str.replace(",", ""))
        } catch (e: Exception) {
            null
        }
    }

    fun formatNumber(bd: BigDecimal): String {
        val stripped = bd.stripTrailingZeros()
        val plain = stripped.toPlainString()

        // If it's too long or has scientific notation, cap at 10-12 digits like Casio LCD
        if (plain.length > 14) {
            val d = bd.toDouble()
            if (Math.abs(d) >= 1e12 || (Math.abs(d) < 1e-4 && Math.abs(d) > 0)) {
                return String.format(Locale.US, "%.6E", d)
            }
        }

        // Format with clean commas for integers or decimals
        val parts = plain.split(".")
        val integerPart = parts[0]
        val formattedInt = try {
            val num = integerPart.toLong()
            val symbols = DecimalFormatSymbols(Locale.US)
            val formatter = DecimalFormat("#,###", symbols)
            formatter.format(num)
        } catch (e: Exception) {
            integerPart
        }

        return if (parts.size > 1) {
            // limit decimal places to max 8
            val dec = if (parts[1].length > 8) parts[1].substring(0, 8) else parts[1]
            "$formattedInt.$dec"
        } else {
            formattedInt
        }
    }
}

sealed class CalculationResult {
    data class Success(val display: String, val value: BigDecimal) : CalculationResult()
    data class Error(val message: String) : CalculationResult()
}
