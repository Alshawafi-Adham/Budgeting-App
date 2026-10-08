package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CategoryVisuals
import com.example.ui.components.DonutBreakdownChart
import com.example.ui.components.MonthlyTrendBarChart
import com.example.ui.components.MonthlyTrendItem
import com.example.ui.components.SliceData
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.theme.IncomeGreenDark
import com.example.ui.util.CurrencyUtils
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.BudgetViewModel
import java.time.LocalDate

enum class StatsTimeFilter {
    DAY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    val isSundayStart = (userSettings["start_of_week"] ?: "sunday").equals("sunday", ignoreCase = true)

    // Filter states
    var selectedTimeFilter by remember { mutableStateOf(StatsTimeFilter.MONTHLY) }
    var transactionTypeMode by remember { mutableStateOf("expense") } // "expense" or "income"

    // Anchor dates for navigations
    var selectedDayAnchor by remember { mutableStateOf(LocalDate.now()) }
    var selectedWeekAnchor by remember { mutableStateOf(LocalDate.now()) }
    var selectedMonthAnchor by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var selectedYearAnchor by remember { mutableIntStateOf(LocalDate.now().year) }

    // Custom date range
    var customStartDate by remember { mutableStateOf(LocalDate.now().minusDays(30)) }
    var customEndDate by remember { mutableStateOf(LocalDate.now()) }
    var showCustomDateDialog by remember { mutableStateOf(false) }
    var showDayPickerDialog by remember { mutableStateOf(false) }

    // Drill down target
    var selectedCategoryForDetail by remember { mutableStateOf<String?>(null) }
    var expandedCategoryCards by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Calculate effective start & end epoch days
    val (startEpochDay, endEpochDay, periodLabel) = remember(
        selectedTimeFilter,
        selectedDayAnchor,
        selectedWeekAnchor,
        selectedMonthAnchor,
        selectedYearAnchor,
        customStartDate,
        customEndDate,
        isSundayStart
    ) {
        when (selectedTimeFilter) {
            StatsTimeFilter.DAY -> {
                val epoch = selectedDayAnchor.toEpochDay()
                val label = if (selectedDayAnchor == LocalDate.now()) {
                    "Today (${DateUtils.formatShortDate(epoch)})"
                } else {
                    DateUtils.formatFullDate(epoch)
                }
                Triple(epoch, epoch, label)
            }
            StatsTimeFilter.WEEKLY -> {
                val (start, end) = DateUtils.getWeekRange(selectedWeekAnchor, isSundayStart)
                val label = DateUtils.formatDateRange(start, end)
                Triple(start.toEpochDay(), end.toEpochDay(), label)
            }
            StatsTimeFilter.MONTHLY -> {
                val (start, end) = DateUtils.getMonthRange(selectedMonthAnchor)
                val label = DateUtils.formatMonthYear(selectedMonthAnchor)
                Triple(start.toEpochDay(), end.toEpochDay(), label)
            }
            StatsTimeFilter.YEARLY -> {
                val yearDate = LocalDate.of(selectedYearAnchor, 1, 1)
                val (start, end) = DateUtils.getYearRange(yearDate)
                val label = "$selectedYearAnchor"
                Triple(start.toEpochDay(), end.toEpochDay(), label)
            }
            StatsTimeFilter.CUSTOM -> {
                val start = if (customStartDate.isAfter(customEndDate)) customEndDate else customStartDate
                val end = if (customStartDate.isAfter(customEndDate)) customStartDate else customEndDate
                val label = DateUtils.formatDateRange(start, end)
                Triple(start.toEpochDay(), end.toEpochDay(), label)
            }
        }
    }

    // Filter transactions in range
    val periodTransactions = remember(allTransactions, startEpochDay, endEpochDay) {
        allTransactions.filter { it.dateEpochDay in startEpochDay..endEpochDay }
    }

    val periodExpenses = remember(periodTransactions) {
        periodTransactions.filter { it.type.equals("expense", ignoreCase = true) }
    }
    val periodIncomes = remember(periodTransactions) {
        periodTransactions.filter { it.type.equals("income", ignoreCase = true) }
    }

    val totalSpentCents = remember(periodExpenses) { periodExpenses.sumOf { it.amountCents } }
    val totalIncomeCents = remember(periodIncomes) { periodIncomes.sumOf { it.amountCents } }
    val netCashFlowCents = totalIncomeCents - totalSpentCents

    // Active transactions based on selected type ("expense" vs "income")
    val activeTransactions = if (transactionTypeMode == "expense") periodExpenses else periodIncomes
    val activeTotalCents = if (transactionTypeMode == "expense") totalSpentCents else totalIncomeCents

    // Category breakdown with cost & percentage
    val categoryTotals = remember(activeTransactions) {
        activeTransactions.groupBy { it.category }
            .mapValues { entry ->
                val totalCost = entry.value.sumOf { it.amountCents }
                val txCount = entry.value.size
                val subcats = entry.value.groupBy { it.subcategory ?: "General" }
                    .mapValues { subEntry -> subEntry.value.sumOf { it.amountCents } }
                    .toList()
                    .sortedByDescending { it.second }
                Triple(totalCost, txCount, subcats)
            }
            .toList()
            .sortedByDescending { it.second.first }
    }

    val slices = remember(categoryTotals) {
        categoryTotals.map { (cat, info) ->
            SliceData(
                label = cat,
                valueCents = info.first,
                color = CategoryVisuals.getColorForCategory(cat)
            )
        }
    }

    // 6-Month Trend Data for trend card
    val sixMonthTrend = remember(allTransactions, selectedMonthAnchor) {
        val list = mutableListOf<MonthlyTrendItem>()
        for (i in 5 downTo 0) {
            val month = selectedMonthAnchor.minusMonths(i.toLong())
            val startEpoch = month.withDayOfMonth(1).toEpochDay()
            val endEpoch = month.plusMonths(1).minusDays(1).toEpochDay()
            val monthTxs = allTransactions.filter { it.dateEpochDay in startEpoch..endEpoch }
            val inc = monthTxs.filter { it.type.equals("income", ignoreCase = true) }.sumOf { it.amountCents }
            val exp = monthTxs.filter { it.type.equals("expense", ignoreCase = true) }.sumOf { it.amountCents }
            list.add(
                MonthlyTrendItem(
                    monthLabel = month.month.name.take(3),
                    incomeCents = inc,
                    expenseCents = exp
                )
            )
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("statistics_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_from_reports_btn")) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Spending Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Category cost breakdown & analytics",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Time Filters Row (Day, Weekly, Monthly, Yearly, Custom)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(StatsTimeFilter.values()) { filter ->
                val isSelected = selectedTimeFilter == filter
                val label = when (filter) {
                    StatsTimeFilter.DAY -> "Day"
                    StatsTimeFilter.WEEKLY -> "Weekly"
                    StatsTimeFilter.MONTHLY -> "Monthly"
                    StatsTimeFilter.YEARLY -> "Yearly"
                    StatsTimeFilter.CUSTOM -> "Custom"
                }
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedTimeFilter = filter
                        if (filter == StatsTimeFilter.CUSTOM) {
                            showCustomDateDialog = true
                        }
                    },
                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("filter_time_${filter.name.lowercase()}")
                )
            }
        }

        // Period Navigator Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        when (selectedTimeFilter) {
                            StatsTimeFilter.DAY -> selectedDayAnchor = selectedDayAnchor.minusDays(1)
                            StatsTimeFilter.WEEKLY -> selectedWeekAnchor = selectedWeekAnchor.minusWeeks(1)
                            StatsTimeFilter.MONTHLY -> selectedMonthAnchor = selectedMonthAnchor.minusMonths(1)
                            StatsTimeFilter.YEARLY -> selectedYearAnchor -= 1
                            StatsTimeFilter.CUSTOM -> showCustomDateDialog = true
                        }
                    },
                    modifier = Modifier.testTag("btn_prev_period")
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            if (selectedTimeFilter == StatsTimeFilter.DAY) showDayPickerDialog = true
                            if (selectedTimeFilter == StatsTimeFilter.CUSTOM) showCustomDateDialog = true
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (selectedTimeFilter == StatsTimeFilter.DAY) Icons.Default.CalendarMonth else Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        when (selectedTimeFilter) {
                            StatsTimeFilter.DAY -> selectedDayAnchor = selectedDayAnchor.plusDays(1)
                            StatsTimeFilter.WEEKLY -> selectedWeekAnchor = selectedWeekAnchor.plusWeeks(1)
                            StatsTimeFilter.MONTHLY -> selectedMonthAnchor = selectedMonthAnchor.plusMonths(1)
                            StatsTimeFilter.YEARLY -> selectedYearAnchor += 1
                            StatsTimeFilter.CUSTOM -> showCustomDateDialog = true
                        }
                    },
                    modifier = Modifier.testTag("btn_next_period")
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next")
                }
            }
        }

        // Mode Toggle: Expense (Spent) vs Income (Earned)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(3.dp)
        ) {
            val isExpense = transactionTypeMode == "expense"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isExpense) ExpenseRedDark else Color.Transparent)
                    .clickable { transactionTypeMode = "expense" }
                    .padding(vertical = 8.dp)
                    .testTag("mode_toggle_expenses"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Expenses (Spent)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isExpense) IncomeGreenDark else Color.Transparent)
                    .clickable { transactionTypeMode = "income" }
                    .padding(vertical = 8.dp)
                    .testTag("mode_toggle_income"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Income (Earned)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Period Summary KPI Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (transactionTypeMode == "expense") "Total Spent This Period" else "Total Earned This Period",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatCents(activeTotalCents, currencyCode),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (transactionTypeMode == "expense") ExpenseRedDark else IncomeGreenDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: ${CurrencyUtils.formatCents(totalSpentCents, currencyCode)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ExpenseRedDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Earned: ${CurrencyUtils.formatCents(totalIncomeCents, currencyCode)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IncomeGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Net: ${CurrencyUtils.formatCents(netCashFlowCents, currencyCode, includeSign = true)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (netCashFlowCents >= 0) IncomeGreenDark else ExpenseRedDark
                            )
                        }
                    }
                }
            }

            // Interactive Donut Breakdown Chart
            if (categoryTotals.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (transactionTypeMode == "expense") "Spending by Category" else "Income by Category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Text(
                                text = "Tap any slice to inspect category cost & transactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            DonutBreakdownChart(
                                slices = slices,
                                totalCents = activeTotalCents,
                                currencyCode = currencyCode,
                                onSliceSelected = { slice ->
                                    selectedCategoryForDetail = slice.label
                                }
                            )
                        }
                    }
                }
            }

            // Category Cost Breakdown Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (transactionTypeMode == "expense") "Category Cost Breakdown" else "Income Sources",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${categoryTotals.size} categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (categoryTotals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No ${if (transactionTypeMode == "expense") "expenses" else "income"} recorded in this period.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(categoryTotals, key = { it.first }) { (cat, info) ->
                    val (costCents, txCount, subcats) = info
                    val percentage = if (activeTotalCents > 0) ((costCents.toDouble() / activeTotalCents.toDouble()) * 100).toInt() else 0
                    val progress = if (activeTotalCents > 0) (costCents.toFloat() / activeTotalCents.toFloat()) else 0f
                    val isExpanded = expandedCategoryCards.contains(cat)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategoryForDetail = cat }
                            .testTag("stats_category_card_${cat.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryIconBadge(categoryName = cat, size = 40.dp, iconSize = 20.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "$txCount transaction${if (txCount > 1) "s" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyUtils.formatCents(costCents, currencyCode),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (transactionTypeMode == "expense") ExpenseRedDark else IncomeGreenDark
                                    )
                                    Text(
                                        text = "$percentage% of total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Colored progress bar proportional to total period spend
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CategoryVisuals.getColorForCategory(cat),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // Subcategories Breakdown Section (Feature: show subcategories cost breakdown)
                            if (subcats.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedCategoryCards = if (isExpanded) {
                                                expandedCategoryCards - cat
                                            } else {
                                                expandedCategoryCards + cat
                                            }
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Subcategories (${subcats.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Subcategories",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        subcats.forEach { (subcatName, subcatCents) ->
                                            val subcatPct = if (costCents > 0) ((subcatCents.toDouble() / costCents.toDouble()) * 100).toInt() else 0
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = subcatName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = CurrencyUtils.formatCents(subcatCents, currencyCode),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "($subcatPct%)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6-Month Trend Overview (if Monthly view)
            if (selectedTimeFilter == StatsTimeFilter.MONTHLY) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Income vs Expense Trend (Last 6 Months)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            MonthlyTrendBarChart(
                                trendData = sixMonthTrend,
                                currencyCode = currencyCode
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Detailed Category Drill-Down Sheet
    if (selectedCategoryForDetail != null) {
        val catName = selectedCategoryForDetail!!
        val catTxs = activeTransactions.filter { it.category.equals(catName, ignoreCase = true) }
        val catCost = catTxs.sumOf { it.amountCents }

        ModalBottomSheet(
            onDismissRequest = { selectedCategoryForDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryIconBadge(categoryName = catName, size = 44.dp, iconSize = 22.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = catName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${catTxs.size} transaction${if (catTxs.size > 1) "s" else ""} in $periodLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Text(
                        text = CurrencyUtils.formatCents(catCost, currencyCode),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (transactionTypeMode == "expense") ExpenseRedDark else IncomeGreenDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(catTxs, key = { it.id }) { tx ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategoryForDetail = null
                                    viewModel.openTransactionDetail(tx)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = DateUtils.formatShortDate(tx.dateEpochDay),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!tx.subcategory.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tx.subcategory,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                    if (!tx.note.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = tx.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Text(
                                    text = CurrencyUtils.formatCents(tx.amountCents, currencyCode),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.type.equals("expense", ignoreCase = true)) ExpenseRedDark else IncomeGreenDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Date Range Picker Dialog
    if (showCustomDateDialog) {
        CustomDateRangeDialog(
            initialStart = customStartDate,
            initialEnd = customEndDate,
            onDismiss = { showCustomDateDialog = false },
            onSave = { start, end ->
                customStartDate = start
                customEndDate = end
                selectedTimeFilter = StatsTimeFilter.CUSTOM
                showCustomDateDialog = false
            }
        )
    }

    // Day Picker Dialog
    if (showDayPickerDialog) {
        DayPickerDialog(
            currentDate = selectedDayAnchor,
            onDismiss = { showDayPickerDialog = false },
            onSelect = { picked ->
                selectedDayAnchor = picked
                showDayPickerDialog = false
            }
        )
    }
}

@Composable
fun CustomDateRangeDialog(
    initialStart: LocalDate,
    initialEnd: LocalDate,
    onDismiss: () -> Unit,
    onSave: (LocalDate, LocalDate) -> Unit
) {
    var startYear by remember { mutableIntStateOf(initialStart.year) }
    var startMonth by remember { mutableIntStateOf(initialStart.monthValue) }
    var startDay by remember { mutableIntStateOf(initialStart.dayOfMonth) }

    var endYear by remember { mutableIntStateOf(initialEnd.year) }
    var endMonth by remember { mutableIntStateOf(initialEnd.monthValue) }
    var endDay by remember { mutableIntStateOf(initialEnd.dayOfMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Date Range", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Start Date",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startYear.toString(),
                        onValueChange = { it.toIntOrNull()?.let { y -> startYear = y } },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = startMonth.toString(),
                        onValueChange = { it.toIntOrNull()?.let { m -> if (m in 1..12) startMonth = m } },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = startDay.toString(),
                        onValueChange = { it.toIntOrNull()?.let { d -> if (d in 1..31) startDay = d } },
                        label = { Text("Day") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "End Date",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = endYear.toString(),
                        onValueChange = { it.toIntOrNull()?.let { y -> endYear = y } },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endMonth.toString(),
                        onValueChange = { it.toIntOrNull()?.let { m -> if (m in 1..12) endMonth = m } },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDay.toString(),
                        onValueChange = { it.toIntOrNull()?.let { d -> if (d in 1..31) endDay = d } },
                        label = { Text("Day") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val validStart = LocalDate.of(startYear, startMonth, startDay.coerceAtMost(LocalDate.of(startYear, startMonth, 1).lengthOfMonth()))
                        val validEnd = LocalDate.of(endYear, endMonth, endDay.coerceAtMost(LocalDate.of(endYear, endMonth, 1).lengthOfMonth()))
                        onSave(validStart, validEnd)
                    } catch (e: Exception) {
                        onSave(initialStart, initialEnd)
                    }
                }
            ) {
                Text("Apply Range")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DayPickerDialog(
    currentDate: LocalDate,
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit
) {
    var year by remember { mutableIntStateOf(currentDate.year) }
    var month by remember { mutableIntStateOf(currentDate.monthValue) }
    var day by remember { mutableIntStateOf(currentDate.dayOfMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Day", fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = year.toString(),
                    onValueChange = { it.toIntOrNull()?.let { y -> year = y } },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = month.toString(),
                    onValueChange = { it.toIntOrNull()?.let { m -> if (m in 1..12) month = m } },
                    label = { Text("Month") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = day.toString(),
                    onValueChange = { it.toIntOrNull()?.let { d -> if (d in 1..31) day = d } },
                    label = { Text("Day") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val valid = LocalDate.of(year, month, day.coerceAtMost(LocalDate.of(year, month, 1).lengthOfMonth()))
                        onSelect(valid)
                    } catch (e: Exception) {
                        onSelect(currentDate)
                    }
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
