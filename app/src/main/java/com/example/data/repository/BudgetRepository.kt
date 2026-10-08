package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.SavingsContributionEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserSettingEntity
import com.example.ui.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class BudgetRepository(private val database: AppDatabase) {
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()
    private val journalDao = database.journalDao()
    private val savingsDao = database.savingsDao()
    private val recurringDao = database.recurringDao()
    private val settingsDao = database.settingsDao()

    // Transaction streams
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllActiveFlow()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllFlow()
    val journalEntries: Flow<List<JournalEntryEntity>> = journalDao.getAllActiveFlow()
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = savingsDao.getAllGoalsFlow(includeArchived = true)
    val activeSavingsGoals: Flow<List<SavingsGoalEntity>> = savingsDao.getAllGoalsFlow(includeArchived = false)
    val allContributions: Flow<List<SavingsContributionEntity>> = savingsDao.getAllContributionsFlow()
    val allRecurring: Flow<List<RecurringTransactionEntity>> = recurringDao.getAllActiveFlow()
    val userSettings: Flow<List<UserSettingEntity>> = settingsDao.getAllSettingsFlow()

    fun getTransactionsForMonth(yearMonthKey: String): Flow<List<TransactionEntity>> {
        // yearMonthKey is "YYYY-MM"
        val parts = yearMonthKey.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val start = LocalDate.of(year, month, 1).toEpochDay()
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1).toEpochDay()
        return transactionDao.getByDateRangeFlow(start, end)
    }

    fun getBudgetsForMonth(yearMonthKey: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsForMonthFlow(yearMonthKey)
    }

    fun getContributionsForGoal(goalId: Long): Flow<List<SavingsContributionEntity>> {
        return savingsDao.getContributionsForGoalFlow(goalId)
    }

    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentActiveFlow(limit)
    }

    fun getUpcomingRecurring(limit: Int = 5): Flow<List<RecurringTransactionEntity>> {
        return recurringDao.getUpcomingActiveFlow(limit)
    }

    suspend fun insertTransaction(tx: TransactionEntity): Long {
        return transactionDao.insert(tx)
    }

    suspend fun updateTransaction(tx: TransactionEntity) {
        transactionDao.update(tx)
    }

    suspend fun softDeleteTransaction(id: Long) {
        transactionDao.softDelete(id)
    }

    suspend fun restoreTransaction(id: Long) {
        transactionDao.restore(id)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insert(category)
    }

    suspend fun setBudget(category: String, monthYear: String, limitCents: Long) {
        budgetDao.setBudget(
            BudgetEntity(
                category = category,
                monthYear = monthYear,
                limitCents = limitCents
            )
        )
    }

    suspend fun deleteBudget(id: Long) {
        budgetDao.deleteBudget(id)
    }

    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long {
        return journalDao.insert(entry)
    }

    suspend fun updateJournalEntry(entry: JournalEntryEntity) {
        journalDao.update(entry)
    }

    suspend fun softDeleteJournalEntry(id: Long) {
        journalDao.softDelete(id)
    }

    suspend fun restoreJournalEntry(id: Long) {
        journalDao.restore(id)
    }

    suspend fun togglePinJournalEntry(id: Long, isPinned: Boolean) {
        journalDao.setPinned(id, isPinned)
    }

    suspend fun insertSavingsGoal(name: String, targetAmountCents: Long, targetDateEpochDay: Long?): Long {
        return savingsDao.insertGoal(
            SavingsGoalEntity(
                name = name,
                targetAmountCents = targetAmountCents,
                targetDateEpochDay = targetDateEpochDay
            )
        )
    }

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) {
        savingsDao.updateGoal(goal)
    }

    suspend fun addSavingsContribution(goalId: Long, amountCents: Long, dateEpochDay: Long, note: String?): Long {
        return savingsDao.insertContribution(
            SavingsContributionEntity(
                goalId = goalId,
                amountCents = amountCents,
                dateEpochDay = dateEpochDay,
                note = note
            )
        )
    }

    suspend fun deleteSavingsContribution(id: Long) {
        savingsDao.deleteContribution(id)
    }

    suspend fun insertRecurringRule(rule: RecurringTransactionEntity): Long {
        return recurringDao.insert(rule)
    }

    suspend fun updateRecurringRule(rule: RecurringTransactionEntity) {
        recurringDao.update(rule)
    }

    suspend fun softDeleteRecurringRule(id: Long) {
        recurringDao.softDelete(id)
    }

    suspend fun restoreRecurringRule(id: Long) {
        recurringDao.restore(id)
    }

    /**
     * When user records / pays an upcoming recurring bill:
     * 1. Creates actual transaction
     * 2. Advances rule's nextDueDate using month-end clamping logic
     */
    suspend fun processRecurringBillPayment(rule: RecurringTransactionEntity) {
        val currentDueDate = DateUtils.epochDayToLocalDate(rule.nextDueDateEpochDay)
        // Insert transaction for the bill
        transactionDao.insert(
            TransactionEntity(
                amountCents = rule.amountCents,
                type = rule.type,
                category = rule.category,
                dateEpochDay = rule.nextDueDateEpochDay,
                note = "Recurring: ${rule.name}",
                isRecurring = true,
                recurringRuleId = rule.id
            )
        )

        // Advance next due date with clamp
        val nextDate = DateUtils.calculateNextDueDate(
            currentDate = currentDueDate,
            frequency = rule.frequency,
            anchorDayOfMonth = rule.dayOfMonth
        )

        recurringDao.update(
            rule.copy(nextDueDateEpochDay = nextDate.toEpochDay())
        )
    }

    suspend fun setSetting(key: String, value: String) {
        settingsDao.setSetting(UserSettingEntity(key, value))
    }

    suspend fun getSetting(key: String): String? {
        return settingsDao.getSetting(key)?.value
    }

    suspend fun deleteAllData() {
        transactionDao.deleteAll()
        journalDao.deleteAll()
        savingsDao.deleteAllGoals()
        savingsDao.deleteAllContributions()
        recurringDao.deleteAll()
        budgetDao.deleteAll()
        categoryDao.deleteCustomCategories()
    }

    /**
     * Seeds initial sample entries if empty so the user can experience the dashboard immediately.
     */
    suspend fun seedInitialDataIfEmpty() {
        val existing = transactionDao.getAllActiveFlow().first()
        if (existing.isNotEmpty()) return

        val today = LocalDate.now()
        val currentMonthKey = DateUtils.toMonthYearKey(today)

        // Seed default budget limits
        budgetDao.setBudget(BudgetEntity(category = "OVERALL", monthYear = currentMonthKey, limitCents = 250000)) // 2,500.00
        budgetDao.setBudget(BudgetEntity(category = "Food", monthYear = currentMonthKey, limitCents = 60000)) // 600.00
        budgetDao.setBudget(BudgetEntity(category = "Transport", monthYear = currentMonthKey, limitCents = 30000)) // 300.00
        budgetDao.setBudget(BudgetEntity(category = "Entertainment", monthYear = currentMonthKey, limitCents = 20000)) // 200.00
        budgetDao.setBudget(BudgetEntity(category = "Housing", monthYear = currentMonthKey, limitCents = 80000)) // 800.00

        // Seed transactions
        transactionDao.insert(
            TransactionEntity(
                amountCents = 350000,
                type = "income",
                category = "Salary",
                dateEpochDay = today.withDayOfMonth(1).toEpochDay(),
                note = "Monthly primary salary",
                paymentMethod = "transfer"
            )
        )
        transactionDao.insert(
            TransactionEntity(
                amountCents = 80000,
                type = "expense",
                category = "Housing",
                dateEpochDay = today.withDayOfMonth(2).toEpochDay(),
                note = "Monthly apartment rent",
                paymentMethod = "transfer"
            )
        )
        transactionDao.insert(
            TransactionEntity(
                amountCents = 1250,
                type = "expense",
                category = "Food",
                dateEpochDay = today.toEpochDay(),
                note = "Morning breakfast & coffee",
                paymentMethod = "card"
            )
        )
        transactionDao.insert(
            TransactionEntity(
                amountCents = 4500,
                type = "expense",
                category = "Food",
                dateEpochDay = today.minusDays(1).toEpochDay(),
                note = "Weekly groceries market",
                paymentMethod = "card"
            )
        )
        transactionDao.insert(
            TransactionEntity(
                amountCents = 1800,
                type = "expense",
                category = "Transport",
                dateEpochDay = today.minusDays(2).toEpochDay(),
                note = "Train commute card top-up",
                paymentMethod = "card"
            )
        )

        // Seed recurring bills
        val anchorDay = 28
        val nextBillDate = if (today.dayOfMonth <= 28) today.withDayOfMonth(28) else today.plusMonths(1).withDayOfMonth(28)
        recurringDao.insert(
            RecurringTransactionEntity(
                name = "High-speed Internet",
                amountCents = 12000,
                type = "expense",
                category = "Utilities",
                frequency = "monthly",
                nextDueDateEpochDay = nextBillDate.toEpochDay(),
                dayOfMonth = 28,
                note = "Fiber broadband subscription"
            )
        )
        val rentDue = if (today.dayOfMonth == 1) today.plusMonths(1).withDayOfMonth(1) else today.withDayOfMonth(1).plusMonths(1)
        recurringDao.insert(
            RecurringTransactionEntity(
                name = "Apartment Rent",
                amountCents = 80000,
                type = "expense",
                category = "Housing",
                frequency = "monthly",
                nextDueDateEpochDay = rentDue.toEpochDay(),
                dayOfMonth = 1,
                note = "Next month rental"
            )
        )

        // Seed savings goal
        val goalId = savingsDao.insertGoal(
            SavingsGoalEntity(
                name = "Emergency Fund",
                targetAmountCents = 500000, // 5,000.00
                targetDateEpochDay = today.plusMonths(6).toEpochDay()
            )
        )
        savingsDao.insertContribution(
            SavingsContributionEntity(
                goalId = goalId,
                amountCents = 150000,
                dateEpochDay = today.withDayOfMonth(1).toEpochDay(),
                note = "Initial allocation from salary"
            )
        )

        // Seed journal note
        journalDao.insert(
            JournalEntryEntity(
                title = "Budgeting with intentionality",
                body = "Started the month tracking every purchase with BudgetFlow. Feeling in control of expenses and steadily building the emergency cushion.",
                dateEpochDay = today.toEpochDay(),
                mood = "proud",
                tags = "milestone,goals",
                isPinned = true
            )
        )
    }

    /**
     * Exports all local data to a JSON string.
     */
    suspend fun exportToJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "BudgetFlow")
        root.put("exportedAt", System.currentTimeMillis())

        val txList = transactionDao.getAllForBackup()
        val txArr = JSONArray()
        for (tx in txList) {
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("amountCents", tx.amountCents)
            obj.put("type", tx.type)
            obj.put("category", tx.category)
            obj.put("dateEpochDay", tx.dateEpochDay)
            obj.put("note", tx.note ?: "")
            obj.put("paymentMethod", tx.paymentMethod ?: "")
            obj.put("isRecurring", tx.isRecurring)
            txArr.put(obj)
        }
        root.put("transactions", txArr)

        val journals = journalDao.getAllForBackup()
        val journalArr = JSONArray()
        for (j in journals) {
            val obj = JSONObject()
            obj.put("title", j.title ?: "")
            obj.put("body", j.body)
            obj.put("dateEpochDay", j.dateEpochDay)
            obj.put("mood", j.mood ?: "")
            obj.put("tags", j.tags ?: "")
            obj.put("isPinned", j.isPinned)
            journalArr.put(obj)
        }
        root.put("journalEntries", journalArr)

        val budgets = budgetDao.getAllForBackup()
        val budgetArr = JSONArray()
        for (b in budgets) {
            val obj = JSONObject()
            obj.put("category", b.category)
            obj.put("monthYear", b.monthYear)
            obj.put("limitCents", b.limitCents)
            budgetArr.put(obj)
        }
        root.put("budgets", budgetArr)

        val goals = savingsDao.getAllGoalsForBackup()
        val goalsArr = JSONArray()
        for (g in goals) {
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("name", g.name)
            obj.put("targetAmountCents", g.targetAmountCents)
            obj.put("targetDateEpochDay", g.targetDateEpochDay ?: -1)
            obj.put("isArchived", g.isArchived)
            goalsArr.put(obj)
        }
        root.put("savingsGoals", goalsArr)

        val contribs = savingsDao.getAllContributionsForBackup()
        val contribArr = JSONArray()
        for (c in contribs) {
            val obj = JSONObject()
            obj.put("goalId", c.goalId)
            obj.put("amountCents", c.amountCents)
            obj.put("dateEpochDay", c.dateEpochDay)
            obj.put("note", c.note ?: "")
            contribArr.put(obj)
        }
        root.put("savingsContributions", contribArr)

        val recurrings = recurringDao.getAllForBackup()
        val recArr = JSONArray()
        for (r in recurrings) {
            val obj = JSONObject()
            obj.put("name", r.name)
            obj.put("amountCents", r.amountCents)
            obj.put("type", r.type)
            obj.put("category", r.category)
            obj.put("frequency", r.frequency)
            obj.put("nextDueDateEpochDay", r.nextDueDateEpochDay)
            obj.put("dayOfMonth", r.dayOfMonth)
            obj.put("isPaused", r.isPaused)
            recArr.put(obj)
        }
        root.put("recurringTransactions", recArr)

        return root.toString(2)
    }

    /**
     * Exports transactions to CSV formatted text.
     */
    suspend fun exportToCsv(): String {
        val txList = transactionDao.getAllForBackup()
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Category,AmountCents,AmountFormatted,PaymentMethod,Note\n")
        for (tx in txList) {
            val date = DateUtils.formatFullDate(tx.dateEpochDay)
            val noteClean = (tx.note ?: "").replace("\"", "\"\"")
            val decimal = tx.amountCents / 100.0
            sb.append("${tx.id},\"$date\",${tx.type},\"${tx.category}\",${tx.amountCents},${String.format("%.2f", decimal)},\"${tx.paymentMethod ?: ""}\",\"$noteClean\"\n")
        }
        return sb.toString()
    }

    /**
     * Restores data from JSON string backup.
     */
    suspend fun importFromJson(jsonString: String): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            var count = 0

            if (root.has("transactions")) {
                val arr = root.getJSONArray("transactions")
                val list = mutableListOf<TransactionEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        TransactionEntity(
                            amountCents = obj.getLong("amountCents"),
                            type = obj.getString("type"),
                            category = obj.getString("category"),
                            dateEpochDay = obj.getLong("dateEpochDay"),
                            note = obj.optString("note").ifEmpty { null },
                            paymentMethod = obj.optString("paymentMethod").ifEmpty { null },
                            isRecurring = obj.optBoolean("isRecurring", false)
                        )
                    )
                }
                transactionDao.insertAll(list)
                count += list.size
            }

            if (root.has("journalEntries")) {
                val arr = root.getJSONArray("journalEntries")
                val list = mutableListOf<JournalEntryEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        JournalEntryEntity(
                            title = obj.optString("title").ifEmpty { null },
                            body = obj.getString("body"),
                            dateEpochDay = obj.getLong("dateEpochDay"),
                            mood = obj.optString("mood").ifEmpty { null },
                            tags = obj.optString("tags").ifEmpty { null },
                            isPinned = obj.optBoolean("isPinned", false)
                        )
                    )
                }
                journalDao.insertAll(list)
                count += list.size
            }

            if (root.has("budgets")) {
                val arr = root.getJSONArray("budgets")
                val list = mutableListOf<BudgetEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        BudgetEntity(
                            category = obj.getString("category"),
                            monthYear = obj.getString("monthYear"),
                            limitCents = obj.getLong("limitCents")
                        )
                    )
                }
                budgetDao.insertAll(list)
                count += list.size
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
