package com.example.sothuchi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MisaColors {
    val HeaderBlueTop = Color(0xFF193EB8)
    val HeaderBlueBottom = Color(0xFF2860EA)
    val BrandBlue = Color(0xFF235CE8)
    val BrandBlueSoft = Color(0xFFE7EFFD)
    val ScreenBackground = Color(0xFFF1F5FB)
    val CardSurface = Color(0xFFFFFFFF)
    val DividerSubtle = Color(0xFFE6ECF5)

    val TextPrimary = Color(0xFF182234)
    val TextSecondary = Color(0xFF6B788E)

    val IncomeGreen = Color(0xFF1B9E4B)
    val ExpenseRed = Color(0xFFE53935)
    val ChartCyan = Color(0xFF00AEEF)
}

private val MisaLightScheme = lightColorScheme(
    primary = MisaColors.BrandBlue,
    onPrimary = Color.White,
    primaryContainer = MisaColors.BrandBlueSoft,
    onPrimaryContainer = Color(0xFF102A6B),
    secondary = MisaColors.ChartCyan,
    onSecondary = Color.White,
    background = MisaColors.ScreenBackground,
    onBackground = MisaColors.TextPrimary,
    surface = MisaColors.CardSurface,
    onSurface = MisaColors.TextPrimary,
    surfaceVariant = MisaColors.ScreenBackground,
    onSurfaceVariant = MisaColors.TextSecondary,
    outlineVariant = MisaColors.DividerSubtle
)

@Composable
fun MisaMoneyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MisaLightScheme,
        content = content
    )
}
