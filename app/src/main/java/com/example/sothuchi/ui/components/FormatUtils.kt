package com.example.sothuchi.ui.components

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val vnLocale = Locale("vi", "VN")

fun formatVnd(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(vnLocale)
    return "${formatter.format(amount)} đ"
}

fun formatCompactK(amount: Long): String {
    if (amount == 0L) return ""
    val k = amount / 1000.0
    return if (amount % 1000L == 0L) {
        "${amount / 1000}K"
    } else {
        String.format(vnLocale, "%.1fK", k)
    }
}

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", vnLocale)
    return sdf.format(Date(timestamp))
}

fun formatDateOnly(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", vnLocale)
    return sdf.format(Date(timestamp))
}

fun formatTimeOnly(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", vnLocale)
    return sdf.format(Date(timestamp))
}
