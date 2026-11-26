package com.example.sothuchi.ui.components

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.sothuchi.data.local.TransactionWithDetails
import java.io.File

fun exportTransactionsToCsvAndShare(
    context: Context,
    transactions: List<TransactionWithDetails>
): Result<String> {
    return runCatching {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "so_thu_chi_misa_${System.currentTimeMillis()}.csv")

        fun escapeCsv(value: String): String {
            val cleaned = value.replace(""", """")
            return ""$cleaned""
        }

        val header = "Thời gian,Loại,Hạng mục,Ví nguồn,Ví nhận,Số tiền (VND),Diễn giải,Sự kiện,Đối tượng,Địa điểm
"
        val body = buildString {
            for (tx in transactions) {
                append(escapeCsv(formatDateTime(tx.timestamp))).append(",")
                append(escapeCsv(tx.type.label)).append(",")
                append(escapeCsv(tx.categoryName)).append(",")
                append(escapeCsv(tx.walletName)).append(",")
                append(escapeCsv(tx.toWalletName ?: "")).append(",")
                append(tx.amount).append(",")
                append(escapeCsv(tx.note)).append(",")
                append(escapeCsv(tx.eventTrip)).append(",")
                append(escapeCsv(tx.counterparty)).append(",")
                append(escapeCsv(tx.location)).append("
")
            }
        }

        file.writeText("﻿" + header + body, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Xuất báo cáo sổ thu chi"))
        "Đã tạo file CSV (${transactions.size} giao dịch)"
    }
}
