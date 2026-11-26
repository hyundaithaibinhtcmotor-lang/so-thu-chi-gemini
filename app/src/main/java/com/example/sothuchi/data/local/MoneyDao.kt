package com.example.sothuchi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MoneyDao {
    @Query("SELECT * FROM wallets ORDER BY id ASC")
    fun observeWallets(): Flow<List<WalletEntity>>

    @Query("UPDATE wallets SET balance = balance + :delta WHERE id = :walletId")
    suspend fun adjustWalletBalance(walletId: Long, delta: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWallets(wallets: List<WalletEntity>)

    @Query("DELETE FROM wallets WHERE id = :walletId")
    suspend fun deleteWalletById(walletId: Long)

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun countWallets(): Int

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<CategoryEntity>)

    @Insert
    suspend fun insertTransactionOnly(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Long): TransactionEntity?

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)

    @Transaction
    suspend fun addTransactionAndUpdateBalance(transaction: TransactionEntity) {
        insertTransactionOnly(transaction)
        when (transaction.type) {
            TransactionType.EXPENSE -> adjustWalletBalance(transaction.walletId, -transaction.amount)
            TransactionType.INCOME -> adjustWalletBalance(transaction.walletId, transaction.amount)
            TransactionType.TRANSFER -> {
                val targetId = transaction.toWalletId ?: return
                adjustWalletBalance(transaction.walletId, -transaction.amount)
                adjustWalletBalance(targetId, transaction.amount)
            }
        }
    }

    @Transaction
    suspend fun deleteTransactionAndRevertBalance(transactionId: Long) {
        val tx = getTransactionById(transactionId) ?: return
        when (tx.type) {
            TransactionType.EXPENSE -> adjustWalletBalance(tx.walletId, tx.amount)
            TransactionType.INCOME -> adjustWalletBalance(tx.walletId, -tx.amount)
            TransactionType.TRANSFER -> {
                val targetId = tx.toWalletId
                adjustWalletBalance(tx.walletId, tx.amount)
                if (targetId != null) {
                    adjustWalletBalance(targetId, -tx.amount)
                }
            }
        }
        deleteTransactionById(transactionId)
    }

    @Transaction
    @Query(
        """
        SELECT 
            t.id,
            t.walletId,
            w.name AS walletName,
            t.toWalletId,
            tw.name AS toWalletName,
            t.categoryId,
            COALESCE(c.name, 'Chuyển khoản') AS categoryName,
            COALESCE(c.colorHex, 4279937509) AS categoryColor,
            COALESCE(c.iconKey, 'transfer') AS categoryIconKey,
            t.type,
            t.amount,
            t.note,
            t.eventTrip,
            t.counterparty,
            t.location,
            t.excludeFromReport,
            t.timestamp
        FROM transactions t
        INNER JOIN wallets w ON t.walletId = w.id
        LEFT JOIN wallets tw ON t.toWalletId = tw.id
        LEFT JOIN categories c ON t.categoryId = c.id
        ORDER BY t.timestamp DESC
        """
    )
    fun observeTransactionsWithDetails(): Flow<List<TransactionWithDetails>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND excludeFromReport = 0 AND timestamp BETWEEN :startMs AND :endMs")
    fun observeTotalByTypeInRange(type: TransactionType, startMs: Long, endMs: Long): Flow<Long>

    @Query(
        """
        SELECT 
            c.id AS categoryId,
            c.name AS categoryName,
            c.colorHex AS colorHex,
            COALESCE(SUM(t.amount), 0) AS totalAmount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = :type AND t.excludeFromReport = 0 AND t.timestamp BETWEEN :startMs AND :endMs
        GROUP BY c.id, c.name, c.colorHex
        HAVING totalAmount > 0
        ORDER BY totalAmount DESC
        """
    )
    fun observeByCategoryInRange(type: TransactionType, startMs: Long, endMs: Long): Flow<List<CategoryExpenseSlice>>

    @Query(
        """
        SELECT 
            b.id AS budgetId,
            c.id AS categoryId,
            c.name AS categoryName,
            c.colorHex AS categoryColor,
            b.limitAmount AS limitAmount,
            COALESCE((
                SELECT SUM(t.amount)
                FROM transactions t
                WHERE t.categoryId = c.id
                  AND t.type = 'EXPENSE'
                  AND t.excludeFromReport = 0
                  AND t.timestamp BETWEEN :startMs AND :endMs
            ), 0) AS spentAmount
        FROM budgets b
        INNER JOIN categories c ON b.categoryId = c.id
        ORDER BY b.id ASC
        """
    )
    fun observeBudgetProgressInRange(startMs: Long, endMs: Long): Flow<List<BudgetProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: BudgetEntity): Long

    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudget(budgetId: Long)

    @Query("SELECT * FROM financial_records ORDER BY isSettled ASC, createdAt DESC")
    fun observeFinancialRecords(): Flow<List<FinancialRecordEntity>>

    @Insert
    suspend fun insertFinancialRecord(record: FinancialRecordEntity): Long

    @Query("UPDATE financial_records SET isSettled = :settled WHERE id = :recordId")
    suspend fun updateRecordSettled(recordId: Long, settled: Boolean)

    @Query("DELETE FROM financial_records WHERE id = :recordId")
    suspend fun deleteFinancialRecord(recordId: Long)
}
