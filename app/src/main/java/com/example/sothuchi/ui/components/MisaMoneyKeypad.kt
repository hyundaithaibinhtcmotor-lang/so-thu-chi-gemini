package com.example.sothuchi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sothuchi.ui.theme.MisaColors

@Composable
fun MisaMoneyKeypad(
    expression: String,
    onExpressionChange: (String) -> Unit,
    onConfirm: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasOperator = MoneyExpressionEvaluator.hasPendingOperator(expression)
    val evaluated = MoneyExpressionEvaluator.evaluate(expression)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MisaColors.CardSurface)
    ) {
        HorizontalDivider(color = MisaColors.DividerSubtle)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MisaColors.ScreenBackground)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val ops = listOf("+" to '+', "-" to '-', "×" to '*', "÷" to '/')
            ops.forEach { (label, symbol) ->
                OperatorPill(
                    label = label,
                    onClick = {
                        onExpressionChange(MoneyExpressionEvaluator.appendOperator(expression, symbol))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            OperatorPill(
                label = "=",
                onClick = {
                    val result = MoneyExpressionEvaluator.evaluate(expression)
                    if (result != null) {
                        onExpressionChange(result.coerceAtLeast(0L).toString())
                    }
                },
                modifier = Modifier.weight(1f),
                highlight = hasOperator
            )
        }

        HorizontalDivider(color = MisaColors.DividerSubtle)

        val rows = listOf(
            listOf(KeySpec.Digit('1'), KeySpec.Digit('2'), KeySpec.Digit('3'), KeySpec.Backspace),
            listOf(KeySpec.Digit('4'), KeySpec.Digit('5'), KeySpec.Digit('6'), KeySpec.TripleZero),
            listOf(KeySpec.Digit('7'), KeySpec.Digit('8'), KeySpec.Digit('9'), KeySpec.Confirm)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { spec ->
                        KeypadCell(
                            spec = spec,
                            canConfirm = evaluated != null && evaluated > 0L,
                            onClick = {
                                when (spec) {
                                    is KeySpec.Digit -> onExpressionChange(
                                        MoneyExpressionEvaluator.appendDigit(expression, spec.char)
                                    )
                                    KeySpec.TripleZero -> onExpressionChange(
                                        MoneyExpressionEvaluator.appendTripleZero(expression)
                                    )
                                    KeySpec.Backspace -> onExpressionChange(
                                        MoneyExpressionEvaluator.backspace(expression)
                                    )
                                    KeySpec.Confirm -> {
                                        val finalAmount = MoneyExpressionEvaluator.evaluate(expression)
                                        if (finalAmount != null && finalAmount > 0L) {
                                            onConfirm(finalAmount)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(2f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onExpressionChange(MoneyExpressionEvaluator.appendDigit(expression, '0'))
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = MisaColors.ScreenBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MisaColors.TextPrimary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onExpressionChange("0") },
                    shape = RoundedCornerShape(8.dp),
                    color = MisaColors.ScreenBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "C",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MisaColors.ExpenseRed
                        )
                    }
                }
            }
        }
    }
}

private sealed interface KeySpec {
    data class Digit(val char: Char) : KeySpec
    data object TripleZero : KeySpec
    data object Backspace : KeySpec
    data object Confirm : KeySpec
}

@Composable
private fun OperatorPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (highlight) MisaColors.BrandBlue else MisaColors.CardSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Color.White else MisaColors.BrandBlue
            )
        }
    }
}

@Composable
private fun KeypadCell(
    spec: KeySpec,
    canConfirm: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when (spec) {
        KeySpec.Confirm -> if (canConfirm) MisaColors.BrandBlue else MisaColors.DividerSubtle
        KeySpec.TripleZero -> MisaColors.BrandBlueSoft
        else -> MisaColors.ScreenBackground
    }
    val contentColor = when (spec) {
        KeySpec.Confirm -> if (canConfirm) Color.White else MisaColors.TextSecondary
        KeySpec.TripleZero -> MisaColors.BrandBlue
        else -> MisaColors.TextPrimary
    }

    Surface(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (spec) {
                is KeySpec.Digit -> Text(
                    text = spec.char.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                KeySpec.TripleZero -> Text(
                    text = "000",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                KeySpec.Backspace -> Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Xóa lùi",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                KeySpec.Confirm -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Xác nhận",
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
