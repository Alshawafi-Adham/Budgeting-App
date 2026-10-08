package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.SavingsContributionEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY dateEpochDay DESC, id DESC")
    fun getAllActiveFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND dateEpochDay >= :startEpoch AND dateEpochDay <= :endEpoch ORDER BY dateEpochDay DESC, id DESC")
    fun getByDateRangeFlow(startEpoch: Long, endEpoch: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY dateEpochDay DESC, id DESC LIMIT :limit")
    fun getRecentActiveFlow(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TransactionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity): Long

    @Update
    suspend fun update(tx: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET isDeleted = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun restore(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("SELECT * FROM transactions WHERE isDeleted = 0")
    suspend fun getAllForBackup(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(txs: List<TransactionEntity>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY isPreset DESC, name ASC")
    fun getAllFlow(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE isPreset = 0")
    suspend fun deleteCustomCategories()

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonthFlow(monthYear: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudget(id: Long)

    @Query("SELECT * FROM budgets")
    suspend fun getAllForBackup(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries WHERE isDeleted = 0 ORDER BY isPinned DESC, dateEpochDay DESC, id DESC")
    fun getAllActiveFlow(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<JournalEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntryEntity): Long

    @Update
    suspend fun update(entry: JournalEntryEntity)

    @Query("UPDATE journal_entries SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE journal_entries SET isDeleted = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun restore(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE journal_entries SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("SELECT * FROM journal_entries WHERE isDeleted = 0")
    suspend fun getAllForBackup(): List<JournalEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<JournalEntryEntity>)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAll()
}

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings_goals WHERE (:includeArchived = 1 OR isArchived = 0) ORDER BY isArchived ASC, id DESC")
    fun getAllGoalsFlow(includeArchived: Boolean): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    @Query("SELECT * FROM savings_contributions WHERE goalId = :goalId ORDER BY dateEpochDay DESC, id DESC")
    fun getContributionsForGoalFlow(goalId: Long): Flow<List<SavingsContributionEntity>>

    @Query("SELECT * FROM savings_contributions ORDER BY dateEpochDay DESC, id DESC")
    fun getAllContributionsFlow(): Flow<List<SavingsContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contrib: SavingsContributionEntity): Long

    @Query("DELETE FROM savings_contributions WHERE id = :id")
    suspend fun deleteContribution(id: Long)

    @Query("SELECT * FROM savings_goals")
    suspend fun getAllGoalsForBackup(): List<SavingsGoalEntity>

    @Query("SELECT * FROM savings_contributions")
    suspend fun getAllContributionsForBackup(): List<SavingsContributionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<SavingsGoalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContributions(contribs: List<SavingsContributionEntity>)

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAllGoals()

    @Query("DELETE FROM savings_contributions")
    suspend fun deleteAllContributions()
}

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_transactions WHERE isDeleted = 0 ORDER BY nextDueDateEpochDay ASC, id ASC")
    fun getAllActiveFlow(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE isDeleted = 0 AND isPaused = 0 ORDER BY nextDueDateEpochDay ASC LIMIT :limit")
    fun getUpcomingActiveFlow(limit: Int): Flow<List<RecurringTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RecurringTransactionEntity): Long

    @Update
    suspend fun update(rule: RecurringTransactionEntity)

    @Query("UPDATE recurring_transactions SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("UPDATE recurring_transactions SET isDeleted = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("SELECT * FROM recurring_transactions WHERE isDeleted = 0")
    suspend fun getAllForBackup(): List<RecurringTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<RecurringTransactionEntity>)

    @Query("DELETE FROM recurring_transactions")
    suspend fun deleteAll()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM user_settings")
    fun getAllSettingsFlow(): Flow<List<UserSettingEntity>>

    @Query("SELECT * FROM user_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): UserSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: UserSettingEntity)

    @Query("SELECT * FROM user_settings")
    suspend fun getAllForBackup(): List<UserSettingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<UserSettingEntity>)

    @Query("DELETE FROM user_settings")
    suspend fun deleteAll()
}
