package com.example.sothuchi.data.repository

import com.example.sothuchi.data.local.AccountGroup
import com.example.sothuchi.data.local.BudgetEntity
import com.example.sothuchi.data.local.BudgetProgress
import com.example.sothuchi.data.local.CategoryEntity
import com.example.sothuchi.data.local.CategoryExpenseSlice
import com.example.sothuchi.data.local.FinancialRecordEntity
import com.example.sothuchi.data.local.MoneyDao
import com.example.sothuchi.data.local.RecordKind
import com.example.sothuchi.data.local.TransactionEntity
import com.example.sothuchi.data.local.TransactionType
import com.example.sothuchi.data.local.TransactionWithDetails
import com.example.sothuchi.data.local.WalletEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class MoneyRepository(private val dao: MoneyDao) {

    val wallets: Flow<List<WalletEntity>> = dao.observeWallets()
    val categories: Flow<List<CategoryEntity>> = dao.observeCategories()
    val transactions: Flow<List<TransactionWithDetails>> = dao.observeTransactionsWithDetails()
    val financialRecords: Flow<List<FinancialRecordEntity>> = dao.observeFinancialRecords()

    fun monthRange(year: Int, monthZeroBased: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return start to end
    }

    fun currentMonthRange(): Pair<Long, Long> {
        val now = Calendar.getInstance()
        return monthRange(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
    }

    fun observeMonthExpense(year: Int, monthZeroBased: Int): Flow<Long> {
        val (start, end) = monthRange(year, monthZeroBased)
        return dao.observeTotalByTypeInRange(TransactionType.EXPENSE, start, end)
    }

    fun observeMonthIncome(year: Int, monthZeroBased: Int): Flow<Long> {
        val (start, end) = monthRange(year, monthZeroBased)
        return dao.observeTotalByTypeInRange(TransactionType.INCOME, start, end)
    }

    fun observeByCategoryInMonth(type: TransactionType, year: Int, monthZeroBased: Int): Flow<List<CategoryExpenseSlice>> {
        val (start, end) = monthRange(year, monthZeroBased)
        return dao.observeByCategoryInRange(type, start, end)
    }

    fun observeBudgetProgressCurrentMonth(): Flow<List<BudgetProgress>> {
        val (start, end) = currentMonthRange()
        return dao.observeBudgetProgressInRange(start, end)
    }

    suspend fun seedInitialDataIfEmpty() {
        if (dao.countWallets() > 0) return

        dao.insertAllWallets(
            listOf(
                WalletEntity(name = "Ví chồng", groupType = AccountGroup.CASH, balance = 683_000L),
                WalletEntity(name = "Ví vợ", groupType = AccountGroup.CASH, balance = 30_000L),
                WalletEntity(name = "Ví chung", groupType = AccountGroup.CASH, balance = 4_000_000L),
                WalletEntity(name = "TK vợ VCB", groupType = AccountGroup.BANK, balance = 72_102_344L),
                WalletEntity(name = "TK vợ Vietin", groupType = AccountGroup.BANK, balance = 845_246L),
                WalletEntity(name = "Thẻ VCB", groupType = AccountGroup.CREDIT_CARD, balance = -1_964_363L),
                WalletEntity(name = "Thẻ MB", groupType = AccountGroup.CREDIT_CARD, balance = 0L),
                WalletEntity(name = "Mua vàng", groupType = AccountGroup.INVESTMENT, balance = 247_600_000L),
                WalletEntity(name = "Tiết kiệm kỳ hạn", groupType = AccountGroup.SAVING, balance = 843_889_716L),
                WalletEntity(name = "Đổi tiền", groupType = AccountGroup.OTHER, balance = 3_250_000L, includeInReport = false)
            )
        )

        dao.insertAllCategories(
            listOf(
                CategoryEntity(name = "Ăn uống", type = TransactionType.EXPENSE, colorHex = 0xFFE53935, iconKey = "food"),
                CategoryEntity(name = "Khác", type = TransactionType.EXPENSE, colorHex = 0xFF1E88E5, iconKey = "other"),
                CategoryEntity(name = "Con cái", type = TransactionType.EXPENSE, colorHex = 0xFF43A047, iconKey = "kids"),
                CategoryEntity(name = "Mua sắm", type = TransactionType.EXPENSE, colorHex = 0xFFFB8C00, iconKey = "shopping"),
                CategoryEntity(name = "Đi lại, xăng xe", type = TransactionType.EXPENSE, colorHex = 0xFF00ACC1, iconKey = "transport"),
                CategoryEntity(name = "Sinh hoạt", type = TransactionType.EXPENSE, colorHex = 0xFF7CB342, iconKey = "home"),
                CategoryEntity(name = "Hiếu hỉ", type = TransactionType.EXPENSE, colorHex = 0xFFEC407A, iconKey = "gift"),
                CategoryEntity(name = "Lương", type = TransactionType.INCOME, colorHex = 0xFF2E7D32, iconKey = "salary"),
                CategoryEntity(name = "Thưởng", type = TransactionType.INCOME, colorHex = 0xFF00897B, iconKey = "bonus"),
                CategoryEntity(name = "Lãi tiết kiệm", type = TransactionType.INCOME, colorHex = 0xFF1976D2, iconKey = "interest")
            )
        )

        dao.insertFinancialRecord(
            FinancialRecordEntity(kind = RecordKind.LEND, title = "Cho vay", amount = 80_000_000L, note = "Khoản cho vay ngắn hạn")
        )
        dao.insertFinancialRecord(
            FinancialRecordEntity(kind = RecordKind.BORROW, title = "Nợ phải trả", amount = 75_400_000L, note = "Khoản vay mua xe")
        )
    }

    suspend fun addWallet(name: String, groupType: AccountGroup, initialBalance: Long, includeInReport: Boolean) {
        if (name.isBlank()) return
        dao.insertWallet(
            WalletEntity(
                name = name.trim(),
                groupType = groupType,
                balance = initialBalance,
                includeInReport = includeInReport
            )
        )
    }

    suspend fun deleteWallet(walletId: Long) {
        dao.deleteWalletById(walletId)
    }

    suspend fun addCategory(name: String, type: TransactionType, colorHex: Long) {
        if (name.isBlank() || type == TransactionType.TRANSFER) return
        dao.insertCategory(
            CategoryEntity(name = name.trim(), type = type, colorHex = colorHex)
        )
    }

    suspend fun addTransaction(
        walletId: Long,
        toWalletId: Long?,
        categoryId: Long?,
        type: TransactionType,
        amount: Long,
        note: String,
        eventTrip: String = "",
        counterparty: String = "",
        location: String = "",
        isBorrowedToPay: Boolean = false,
        hasFee: Boolean = false,
        excludeFromReport: Boolean = false,
        timestamp: Long = System.currentTimeMillis()
    ) {
        if (amount <= 0L) return
        if (type == TransactionType.TRANSFER && (toWalletId == null || toWalletId == walletId)) return

        dao.addTransactionAndUpdateBalance(
            TransactionEntity(
                walletId = walletId,
                toWalletId = if (type == TransactionType.TRANSFER) toWalletId else null,
                categoryId = if (type == TransactionType.TRANSFER) null else categoryId,
                type = type,
                amount = amount,
                note = note.trim(),
                eventTrip = eventTrip.trim(),
                counterparty = counterparty.trim(),
                location = location.trim(),
                isBorrowedToPay = isBorrowedToPay,
                hasFee = hasFee,
                excludeFromReport = excludeFromReport,
                timestamp = timestamp
            )
        )
    }

    suspend fun deleteTransaction(transactionId: Long) {
        dao.deleteTransactionAndRevertBalance(transactionId)
    }

    suspend fun saveBudget(categoryId: Long, limitAmount: Long) {
        if (limitAmount <= 0L) return
        dao.upsertBudget(BudgetEntity(categoryId = categoryId, limitAmount = limitAmount))
    }

    suspend fun deleteBudget(budgetId: Long) {
        dao.deleteBudget(budgetId)
    }

    suspend fun addFinancialRecord(kind: RecordKind, title: String, amount: Long, note: String) {
        if (title.isBlank() || amount <= 0L) return
        dao.insertFinancialRecord(
            FinancialRecordEntity(
                kind = kind,
                title = title.trim(),
                amount = amount,
                note = note.trim()
            )
        )
    }

    suspend fun setRecordSettled(recordId: Long, settled: Boolean) {
        dao.updateRecordSettled(recordId, settled)
    }

    suspend fun deleteFinancialRecord(recordId: Long) {
        dao.deleteFinancialRecord(recordId)
    }
}
