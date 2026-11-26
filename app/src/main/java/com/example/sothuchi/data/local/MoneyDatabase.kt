package com.example.sothuchi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromAccountGroup(value: AccountGroup): String = value.name

    @TypeConverter
    fun toAccountGroup(value: String): AccountGroup = AccountGroup.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromRecordKind(value: RecordKind): String = value.name

    @TypeConverter
    fun toRecordKind(value: String): RecordKind = RecordKind.valueOf(value)
}

@Database(
    entities = [
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        FinancialRecordEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MoneyDatabase : RoomDatabase() {
    abstract fun moneyDao(): MoneyDao

    companion object {
        @Volatile
        private var INSTANCE: MoneyDatabase? = null

        fun getInstance(context: Context): MoneyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoneyDatabase::class.java,
                    "so_thu_chi_misa_v3_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
