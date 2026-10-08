package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Transaction table: all amounts stored strictly as integer cents.
 * Soft delete supported via [isDeleted].
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["dateEpochDay"]),
        Index(value = ["category"]),
        Index(value = ["type"]),
        Index(value = ["isDeleted"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountCents: Long, // Positive integer cents, e.g. 1500 = $15.00 / RM 15.00
    val type: String, // "expense" or "income"
    val category: String,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val note: String? = null, // Max 500 characters
    val paymentMethod: String? = null, // "cash", "card", "transfer", or null
    val isRecurring: Boolean = false,
    val recurringRuleId: Long? = null,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Categories table: presets + user custom categories.
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String = "both", // "expense", "income", or "both"
    val iconName: String = "category",
    val colorHex: String = "#10B981",
    val isPreset: Boolean = false
)

/**
 * Monthly budget limits per category or overall ("OVERALL").
 * Month format: "YYYY-MM" (e.g. "2026-10")
 */
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["category", "monthYear"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // Category name or "OVERALL"
    val monthYear: String, // "YYYY-MM"
    val limitCents: Long
)

/**
 * Standalone financial journal entries.
 */
@Entity(
    tableName = "journal_entries",
    indices = [
        Index(value = ["dateEpochDay"]),
        Index(value = ["isPinned"]),
        Index(value = ["isDeleted"])
    ]
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String? = null,
    val body: String, // Max 2000 characters
    val dateEpochDay: Long,
    val linkedTransactionId: Long? = null,
    val mood: String? = null, // "good", "neutral", "stressed", "proud"
    val tags: String? = null, // Comma-separated tags
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Savings goal table.
 */
@Entity(
    tableName = "savings_goals",
    indices = [Index(value = ["isArchived"])]
)
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmountCents: Long,
    val targetDateEpochDay: Long? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Contributions towards savings goals.
 */
@Entity(
    tableName = "savings_contributions",
    indices = [Index(value = ["goalId"])]
)
data class SavingsContributionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val amountCents: Long,
    val dateEpochDay: Long,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Recurring transaction rules with month-end clamp logic.
 */
@Entity(
    tableName = "recurring_transactions",
    indices = [
        Index(value = ["nextDueDateEpochDay"]),
        Index(value = ["isPaused"]),
        Index(value = ["isDeleted"])
    ]
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amountCents: Long,
    val type: String, // "expense" or "income"
    val category: String,
    val frequency: String, // "weekly", "monthly", "yearly"
    val nextDueDateEpochDay: Long,
    val dayOfMonth: Int = 1, // Anchor day for month-end clamping (e.g. 31)
    val endDateEpochDay: Long? = null,
    val isPaused: Boolean = false,
    val isDeleted: Boolean = false,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Key-value settings entity.
 */
@Entity(tableName = "user_settings")
data class UserSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
