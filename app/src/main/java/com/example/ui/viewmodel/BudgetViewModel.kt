package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.SavingsContributionEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.BudgetRepository
import com.example.ui.components.AppTab
import com.example.ui.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = BudgetRepository(database)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Active sub-screen (null = showing currentTab screen)
    private val _activeSubScreen = MutableStateFlow<String?>(null)
    val activeSubScreen: StateFlow<String?> = _activeSubScreen.asStateFlow()

    // Selected month for dashboard and budget tracking (defaults to current month)
    private val _selectedMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val selectedMonth: StateFlow<LocalDate> = _selectedMonth.asStateFlow()

    // Undo event channel
    private val _undoEvents = MutableSharedFlow<String>()
    val undoEvents: SharedFlow<String> = _undoEvents.asSharedFlow()

    // Tracking last deleted item for undo
    private var lastDeletedTransactionId: Long? = null

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Flows from repository
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<TransactionEntity>> = repository.getRecentTransactions(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingRecurring: StateFlow<List<RecurringTransactionEntity>> = repository.getUpcomingRecurring(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecurring: StateFlow<List<RecurringTransactionEntity>> = repository.allRecurring
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalEntries: StateFlow<List<JournalEntryEntity>> = repository.journalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.activeSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContributions: StateFlow<List<SavingsContributionEntity>> = repository.allContributions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<Map<String, String>> = repository.userSettings
        .combine(MutableStateFlow(Unit)) { settings, _ ->
            settings.associate { it.key to it.value }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            mapOf("currency" to "MYR", "theme" to "system", "start_of_week" to "sunday", "budget_cycle_start_day" to "1")
        )

    val currencyCode: StateFlow<String> = combine(userSettings) { settings ->
        settings[0]["currency"] ?: "MYR"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MYR")

    // Monthly summary calculation
    val monthlyTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        selectedMonth
    ) { txList, month ->
        val startEpoch = month.withDayOfMonth(1).toEpochDay()
        val endEpoch = month.plusMonths(1).minusDays(1).toEpochDay()
        txList.filter { it.dateEpochDay in startEpoch..endEpoch }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyBudgets: StateFlow<List<BudgetEntity>> = combine(
        selectedMonth,
        repository.allTransactions
    ) { month, _ ->
        val monthKey = DateUtils.toMonthYearKey(month)
        repository.getBudgetsForMonth(monthKey)
    }.combine(MutableStateFlow(Unit)) { flow, _ -> flow }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val monthlySpentCents: StateFlow<Long> = combine(monthlyTransactions) { txList ->
        txList.filter { it.type.equals("expense", ignoreCase = true) }
            .sumOf { it.amountCents }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthlyIncomeCents: StateFlow<Long> = combine(monthlyTransactions) { txList ->
        txList.filter { it.type.equals("income", ignoreCase = true) }
            .sumOf { it.amountCents }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthlyNetBalanceCents: StateFlow<Long> = combine(
        monthlyIncomeCents,
        monthlySpentCents
    ) { income, spent ->
        income - spent
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Transaction list filtering & search
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("all") // "all", "expense", "income"
    val filterCategory = MutableStateFlow<String?>(null)
    val filterDateRange = MutableStateFlow("all") // "all", "this_month", "last_month"
    val sortOrder = MutableStateFlow("date_desc") // "date_desc", "date_asc", "amount_desc", "amount_asc"

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        searchQuery,
        filterType,
        filterCategory,
        filterDateRange,
        sortOrder
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val txList = args[0] as List<TransactionEntity>
        val query = (args[1] as String).trim().lowercase()
        val type = args[2] as String
        val cat = args[3] as? String
        val range = args[4] as String
        val sort = args[5] as String

        val today = LocalDate.now()
        val thisMonthStart = today.withDayOfMonth(1).toEpochDay()
        val thisMonthEnd = today.plusMonths(1).withDayOfMonth(1).minusDays(1).toEpochDay()
        val lastMonthStart = today.minusMonths(1).withDayOfMonth(1).toEpochDay()
        val lastMonthEnd = today.withDayOfMonth(1).minusDays(1).toEpochDay()

        var result = txList.filter { tx ->
            val matchesType = when (type) {
                "expense" -> tx.type.equals("expense", ignoreCase = true)
                "income" -> tx.type.equals("income", ignoreCase = true)
                else -> true
            }
            val matchesCat = cat == null || tx.category.equals(cat, ignoreCase = true)
            val matchesRange = when (range) {
                "this_month" -> tx.dateEpochDay in thisMonthStart..thisMonthEnd
                "last_month" -> tx.dateEpochDay in lastMonthStart..lastMonthEnd
                else -> true
            }
            val matchesQuery = query.isEmpty() ||
                    tx.category.lowercase().contains(query) ||
                    (tx.note?.lowercase()?.contains(query) == true)

            matchesType && matchesCat && matchesRange && matchesQuery
        }

        result = when (sort) {
            "date_asc" -> result.sortedBy { it.dateEpochDay }
            "amount_desc" -> result.sortedByDescending { it.amountCents }
            "amount_asc" -> result.sortedBy { it.amountCents }
            else -> result.sortedByDescending { it.dateEpochDay }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialog / Sheet states
    var addTransactionType = MutableStateFlow("expense")
    var addTransactionDateEpochDay = MutableStateFlow(LocalDate.now().toEpochDay())
    var editTransactionTarget = MutableStateFlow<TransactionEntity?>(null)
    var isAddTransactionOpen = MutableStateFlow(false)

    var detailTransactionTarget = MutableStateFlow<TransactionEntity?>(null)
    var selectedCalendarDay = MutableStateFlow<LocalDate?>(null)

    // Navigation methods
    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
        _activeSubScreen.value = null
    }

    fun openSubScreen(screenName: String) {
        _activeSubScreen.value = screenName
    }

    fun closeSubScreen() {
        _activeSubScreen.value = null
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun prevMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun setMonth(month: LocalDate) {
        _selectedMonth.value = month.withDayOfMonth(1)
    }

    fun openAddTransaction(
        type: String = "expense",
        dateEpochDay: Long = LocalDate.now().toEpochDay(),
        editingTx: TransactionEntity? = null
    ) {
        addTransactionType.value = type
        addTransactionDateEpochDay.value = dateEpochDay
        editTransactionTarget.value = editingTx
        isAddTransactionOpen.value = true
    }

    fun closeAddTransaction() {
        isAddTransactionOpen.value = false
        editTransactionTarget.value = null
    }

    fun openTransactionDetail(tx: TransactionEntity) {
        detailTransactionTarget.value = tx
    }

    fun closeTransactionDetail() {
        detailTransactionTarget.value = null
    }

    fun openDayDetail(date: LocalDate) {
        selectedCalendarDay.value = date
    }

    fun closeDayDetail() {
        selectedCalendarDay.value = null
    }

    // Transaction actions
    fun saveTransaction(
        amountCents: Long,
        type: String,
        category: String,
        dateEpochDay: Long,
        note: String?,
        paymentMethod: String?,
        isRecurring: Boolean = false,
        editingId: Long? = null
    ) {
        viewModelScope.launch {
            if (editingId != null && editingId > 0) {
                val existing = database.transactionDao().getById(editingId)
                if (existing != null) {
                    val updated = existing.copy(
                        amountCents = amountCents,
                        type = type,
                        category = category,
                        dateEpochDay = dateEpochDay,
                        note = note,
                        paymentMethod = paymentMethod,
                        isRecurring = isRecurring,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateTransaction(updated)
                }
            } else {
                val newTx = TransactionEntity(
                    amountCents = amountCents,
                    type = type,
                    category = category,
                    dateEpochDay = dateEpochDay,
                    note = note,
                    paymentMethod = paymentMethod,
                    isRecurring = isRecurring
                )
                repository.insertTransaction(newTx)
            }
            closeAddTransaction()
            closeTransactionDetail()
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            lastDeletedTransactionId = tx.id
            repository.softDeleteTransaction(tx.id)
            closeTransactionDetail()
            _undoEvents.emit("Transaction deleted")
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            lastDeletedTransactionId?.let { id ->
                repository.restoreTransaction(id)
                lastDeletedTransactionId = null
                _undoEvents.emit("Transaction restored")
            }
        }
    }

    fun processRecurringBill(rule: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.processRecurringBillPayment(rule)
            _undoEvents.emit("Recorded payment for ${rule.name}")
        }
    }

    // Journal Entry actions
    fun saveJournalEntry(
        title: String?,
        body: String,
        dateEpochDay: Long,
        mood: String?,
        tags: String?,
        linkedTxId: Long? = null,
        editingId: Long? = null
    ) {
        viewModelScope.launch {
            if (editingId != null && editingId > 0) {
                val entry = JournalEntryEntity(
                    id = editingId,
                    title = title,
                    body = body,
                    dateEpochDay = dateEpochDay,
                    mood = mood,
                    tags = tags,
                    linkedTransactionId = linkedTxId,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateJournalEntry(entry)
            } else {
                val entry = JournalEntryEntity(
                    title = title,
                    body = body,
                    dateEpochDay = dateEpochDay,
                    mood = mood,
                    tags = tags,
                    linkedTransactionId = linkedTxId
                )
                repository.insertJournalEntry(entry)
            }
        }
    }

    fun deleteJournalEntry(id: Long) {
        viewModelScope.launch {
            repository.softDeleteJournalEntry(id)
        }
    }

    fun togglePinJournalEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            repository.togglePinJournalEntry(entry.id, !entry.isPinned)
        }
    }

    // Budget actions
    fun setCategoryBudget(category: String, limitCents: Long) {
        viewModelScope.launch {
            val monthKey = DateUtils.toMonthYearKey(_selectedMonth.value)
            repository.setBudget(category, monthKey, limitCents)
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
        }
    }

    // Savings actions
    fun saveSavingsGoal(name: String, targetAmountCents: Long, targetDateEpochDay: Long?) {
        viewModelScope.launch {
            repository.insertSavingsGoal(name, targetAmountCents, targetDateEpochDay)
        }
    }

    fun addSavingsContribution(goalId: Long, amountCents: Long, dateEpochDay: Long, note: String?) {
        viewModelScope.launch {
            repository.addSavingsContribution(goalId, amountCents, dateEpochDay, note)
        }
    }

    fun toggleArchiveSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.updateSavingsGoal(goal.copy(isArchived = !goal.isArchived))
        }
    }

    // Recurring actions
    fun saveRecurringRule(
        name: String,
        amountCents: Long,
        type: String,
        category: String,
        frequency: String,
        nextDueDateEpochDay: Long,
        dayOfMonth: Int,
        note: String? = null,
        editingId: Long? = null
    ) {
        viewModelScope.launch {
            val rule = RecurringTransactionEntity(
                id = editingId ?: 0L,
                name = name,
                amountCents = amountCents,
                type = type,
                category = category,
                frequency = frequency,
                nextDueDateEpochDay = nextDueDateEpochDay,
                dayOfMonth = dayOfMonth,
                note = note
            )
            if (editingId != null && editingId > 0) {
                repository.updateRecurringRule(rule)
            } else {
                repository.insertRecurringRule(rule)
            }
        }
    }

    fun deleteRecurringRule(id: Long) {
        viewModelScope.launch {
            repository.softDeleteRecurringRule(id)
        }
    }

    fun togglePauseRecurringRule(rule: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.updateRecurringRule(rule.copy(isPaused = !rule.isPaused))
        }
    }

    // Category actions
    fun addCustomCategory(name: String, type: String = "expense", iconName: String = "category", colorHex: String = "#10B981") {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    type = type,
                    iconName = iconName,
                    colorHex = colorHex,
                    isPreset = false
                )
            )
        }
    }

    // Settings actions
    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.setSetting(key, value)
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
            _undoEvents.emit("All data erased successfully")
        }
    }
}
