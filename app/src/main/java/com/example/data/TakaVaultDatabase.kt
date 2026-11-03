package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface TakaVaultDao {
    @Query("SELECT * FROM wallet_config WHERE id = 1")
    fun observeWalletConfig(): Flow<WalletConfigEntity?>

    @Query("SELECT * FROM wallet_config WHERE id = 1")
    suspend fun getWalletConfig(): WalletConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWalletConfig(config: WalletConfigEntity)

    @Query("SELECT * FROM note_counts")
    fun observeNoteCounts(): Flow<List<NoteCountEntity>>

    @Query("SELECT * FROM note_counts")
    suspend fun getNoteCounts(): List<NoteCountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNoteCount(noteCount: NoteCountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNoteCounts(noteCounts: List<NoteCountEntity>)

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    suspend fun getCategories(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getTransactions(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(txs: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("SELECT * FROM debts ORDER BY timestamp DESC")
    fun observeDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts ORDER BY timestamp DESC")
    suspend fun getDebts(): List<DebtEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<DebtEntity>)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: String)

    @Query("DELETE FROM debts")
    suspend fun clearDebts()

    @Query("SELECT * FROM temp_money ORDER BY timestamp DESC")
    fun observeTempMoney(): Flow<List<TempMoneyEntity>>

    @Query("SELECT * FROM temp_money ORDER BY timestamp DESC")
    suspend fun getTempMoney(): List<TempMoneyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTempMoney(item: TempMoneyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTempMoneyList(items: List<TempMoneyEntity>)

    @Query("DELETE FROM temp_money WHERE id = :id")
    suspend fun deleteTempMoneyById(id: String)

    @Query("DELETE FROM temp_money")
    suspend fun clearTempMoney()
}

@Database(
    entities = [
        WalletConfigEntity::class,
        NoteCountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        DebtEntity::class,
        TempMoneyEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TakaVaultDatabase : RoomDatabase() {
    abstract fun dao(): TakaVaultDao

    companion object {
        @Volatile
        private var INSTANCE: TakaVaultDatabase? = null

        fun getInstance(context: Context): TakaVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TakaVaultDatabase::class.java,
                    "takavault_room_v4.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also { INSTANCE = it }
            }
        }
    }
}
