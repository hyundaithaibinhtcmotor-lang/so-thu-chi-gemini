package com.example.sothuchi.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AccountGroup(val label: String, val isLiability: Boolean = false) {
    CASH("Tiền mặt"),
    BANK("Tài khoản ngân hàng"),
    CREDIT_CARD("Thẻ tín dụng", isLiability = true),
    INVESTMENT("Tài khoản đầu tư"),
    SAVING("Tài khoản tiết kiệm"),
    OTHER("Khác")
}

enum class TransactionType(val label: String) {
    EXPENSE("Chi tiền"),
    INCOME("Thu tiền"),
    TRANSFER("Chuyển khoản")
}

enum class RecordKind(val label: String) {
    LEND("Cho vay"),
    BORROW("Nợ phải trả"),
    SAVING("Sổ tiết kiệm")
}

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val groupType: AccountGroup,
    val balance: Long,
    val includeInReport: Boolean = true,
    val isInUse: Boolean = true
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val colorHex: Long,
    val iconKey: String = "default",
    val isFrequent: Boolean = true
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["toWalletId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("walletId"), Index("toWalletId"), Index("categoryId"), Index("timestamp")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletId: Long,
    val toWalletId: Long? = null,
    val categoryId: Long? = null,
    val type: TransactionType,
    val amount: Long,
    val note: String,
    val eventTrip: String = "",
    val counterparty: String = "",
    val location: String = "",
    val isBorrowedToPay: Boolean = false,
    val hasFee: Boolean = false,
    val excludeFromReport: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val limitAmount: Long
)

@Entity(tableName = "financial_records")
data class FinancialRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: RecordKind,
    val title: String,
    val amount: Long,
    val note: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class TransactionWithDetails(
    val id: Long,
    val walletId: Long,
    val walletName: String,
    val toWalletId: Long?,
    val toWalletName: String?,
    val categoryId: Long?,
    val categoryName: String,
    val categoryColor: Long,
    val categoryIconKey: String,
    val type: TransactionType,
    val amount: Long,
    val note: String,
    val eventTrip: String,
    val counterparty: String,
    val location: String,
    val excludeFromReport: Boolean,
    val timestamp: Long
)

data class BudgetProgress(
    val budgetId: Long,
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: Long,
    val limitAmount: Long,
    val spentAmount: Long
) {
    val ratio: Float
        get() = if (limitAmount <= 0L) 0f else (spentAmount.toFloat() / limitAmount.toFloat()).coerceAtLeast(0f)
}

data class CategoryExpenseSlice(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: Long,
    val totalAmount: Long
)
