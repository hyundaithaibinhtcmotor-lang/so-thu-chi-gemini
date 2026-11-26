package com.example.sothuchi.ui.components

import kotlin.math.roundToLong

object MoneyExpressionEvaluator {

    private val operators = setOf('+', '-', '*', '/')

    fun isOperator(ch: Char): Boolean = ch in operators

    fun appendDigit(expr: String, digit: Char): String {
        if (expr == "0") return digit.toString()
        return expr + digit
    }

    fun appendTripleZero(expr: String): String {
        if (expr.isEmpty() || expr == "0") return "0"
        val lastChar = expr.last()
        if (isOperator(lastChar)) return expr
        return expr + "000"
    }

    fun appendOperator(expr: String, op: Char): String {
        if (expr.isEmpty()) return if (op == '-') "-" else expr
        val lastChar = expr.last()
        if (isOperator(lastChar)) {
            return expr.dropLast(1) + op
        }
        return expr + op
    }

    fun backspace(expr: String): String {
        if (expr.length <= 1) return "0"
        return expr.dropLast(1)
    }

    fun hasPendingOperator(expr: String): Boolean {
        val trimmed = if (expr.startsWith('-')) expr.drop(1) else expr
        return trimmed.any { isOperator(it) }
    }

    fun evaluate(expr: String): Long? {
        val clean = expr.trim()
        if (clean.isEmpty() || clean == "-") return null
        if (isOperator(clean.last())) return null

        val tokens = tokenize(clean) ?: return null
        if (tokens.isEmpty()) return null

        val afterMulDiv = mutableListOf<Any>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token is Char && (token == '*' || token == '/')) {
                val prev = afterMulDiv.removeLastOrNull() as? Double ?: return null
                val next = tokens.getOrNull(i + 1) as? Double ?: return null
                val computed = if (token == '*') {
                    prev * next
                } else {
                    if (next == 0.0) return null else prev / next
                }
                afterMulDiv.add(computed)
                i += 2
            } else {
                afterMulDiv.add(token)
                i += 1
            }
        }

        var acc = afterMulDiv.firstOrNull() as? Double ?: return null
        var idx = 1
        while (idx < afterMulDiv.size) {
            val op = afterMulDiv[idx] as? Char ?: return null
            val rhs = afterMulDiv.getOrNull(idx + 1) as? Double ?: return null
            acc = when (op) {
                '+' -> acc + rhs
                '-' -> acc - rhs
                else -> return null
            }
            idx += 2
        }

        if (acc.isNaN() || acc.isInfinite()) return null
        return acc.roundToLong()
    }

    fun formatExpressionDisplay(expr: String): String {
        val sb = StringBuilder()
        val numBuf = StringBuilder()

        fun flushNumber() {
            if (numBuf.isNotEmpty()) {
                val parsed = numBuf.toString().toLongOrNull()
                if (parsed != null) {
                    sb.append(formatVnd(parsed).removeSuffix(" đ"))
                } else {
                    sb.append(numBuf)
                }
                numBuf.clear()
            }
        }

        for (index in expr.indices) {
            val ch = expr[index]
            val isUnaryMinus = ch == '-' && (index == 0 || isOperator(expr[index - 1]))
            if (ch.isDigit() || isUnaryMinus) {
                numBuf.append(ch)
            } else {
                flushNumber()
                val visualOp = when (ch) {
                    '*' -> " × "
                    '/' -> " ÷ "
                    '+' -> " + "
                    '-' -> " - "
                    else -> ch.toString()
                }
                sb.append(visualOp)
            }
        }
        flushNumber()
        return sb.toString().ifEmpty { "0" }
    }

    private fun tokenize(expr: String): List<Any>? {
        val result = mutableListOf<Any>()
        val buf = StringBuilder()

        for (i in expr.indices) {
            val ch = expr[i]
            val isUnaryMinus = ch == '-' && (i == 0 || isOperator(expr[i - 1]))
            if (ch.isDigit() || isUnaryMinus) {
                buf.append(ch)
            } else if (isOperator(ch)) {
                if (buf.isEmpty()) return null
                val num = buf.toString().toDoubleOrNull() ?: return null
                result.add(num)
                buf.clear()
                result.add(ch)
            } else {
                return null
            }
        }

        if (buf.isEmpty()) return null
        val lastNum = buf.toString().toDoubleOrNull() ?: return null
        result.add(lastNum)
        return result
    }
}
