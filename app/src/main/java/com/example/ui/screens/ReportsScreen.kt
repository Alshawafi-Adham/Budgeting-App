package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ReportsScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthlyTxs by viewModel.monthlyTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()

    var selectedSliceForDrillDown by remember { mutableStateOf<String?>(null) }

    val expenses = remember(monthlyTxs) {
        monthlyTxs.filter { it.type.equals("expense", ignoreCase = true) }
    }
    val incomes = remember(monthlyTxs) {
        monthlyTxs.filter { it.type.equals("income", ignoreCase = true) }
    }

    val totalSpentCents = remember(expenses) { expenses.sumOf { it.amountCents } }
    val totalIncomeCents = remember(incomes) { incomes.sumOf { it.amountCents } }
    val netCashFlowCents = totalIncomeCents - totalSpentCents

    // Category breakdown
    val categoryTotals = remember(expenses) {
        expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amountCents } }
            .toList()
            .sortedByDescending { it.second }
    }

    val slices = remember(categoryTotals) {
        categoryTotals.map { (cat, cents) ->
            SliceData(
                label = cat,
                valueCents = cents,
                color = CategoryVisuals.getColorForCategory(cat)
            )
        }
    }

    // 6-Month Trend Data
    val sixMonthTrend = remember(allTransactions, selectedMonth) {
        val list = mutableListOf<MonthlyTrendItem>()
        for (i in 5 downTo 0) {
            val month = selectedMonth.minusMonths(i.toLong())
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
            .testTag("reports_screen")
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
            Text(
                text = "Reports & Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        // Month selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.prevMonth() }) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }
            Text(
                text = DateUtils.formatMonthYear(selectedMonth),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cash Flow Summary Card
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
                            text = "Monthly Cash Flow",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatCents(netCashFlowCents, currencyCode, includeSign = true),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (netCashFlowCents >= 0) IncomeGreenDark else ExpenseRedDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Income: ${CurrencyUtils.formatCents(totalIncomeCents, currencyCode)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IncomeGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Spent: ${CurrencyUtils.formatCents(totalSpentCents, currencyCode)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ExpenseRedDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Monthly Category Breakdown (Donut Chart)
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
                            text = "Spending by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Text(
                            text = "Tap any slice or item to inspect transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DonutBreakdownChart(
                            slices = slices,
                            totalCents = totalSpentCents,
                            currencyCode = currencyCode,
                            onSliceSelected = { slice ->
                                selectedSliceForDrillDown = slice.label
                            }
                        )
                    }
                }
            }

            // Income vs Expense Trend (Last 6 Months)
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
                            text = "Income vs Expense Trend (6 Months)",
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

            // Top Spending Categories Ranked List
            item {
                Text(
                    text = "Top Spending Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (categoryTotals.isEmpty()) {
                item {
                    Text(
                        text = "No expenses recorded this month.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(categoryTotals) { (cat, cents) ->
                    val percentage = if (totalSpentCents > 0) ((cents.toDouble() / totalSpentCents.toDouble()) * 100).toInt() else 0
                    val progress = if (totalSpentCents > 0) (cents.toFloat() / totalSpentCents.toFloat()) else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSliceForDrillDown = cat },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryIconBadge(categoryName = cat, size = 36.dp, iconSize = 18.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyUtils.formatCents(cents, currencyCode),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$percentage%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CategoryVisuals.getColorForCategory(cat),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Drill down sheet if category selected
    if (selectedSliceForDrillDown != null) {
        val catName = selectedSliceForDrillDown!!
        val catTransactions = monthlyTxs.filter { it.category.equals(catName, ignoreCase = true) }
        CategoryTransactionsSheet(
            categoryName = catName,
            transactions = catTransactions,
            currencyCode = currencyCode,
            onDismiss = { selectedSliceForDrillDown = null },
            onTransactionClick = { tx ->
                selectedSliceForDrillDown = null
                viewModel.openTransactionDetail(tx)
            }
        )
    }
}
