package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecurringTransactionEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.TransactionItemRow
import com.example.ui.theme.ExpenseContainerDark
import com.example.ui.theme.ExpenseContainerLight
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.theme.IncomeContainerDark
import com.example.ui.theme.IncomeContainerLight
import com.example.ui.theme.IncomeGreenDark
import com.example.ui.util.CurrencyUtils
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun HomeScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val currencyCode by viewModel.currencyCode.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val spentCents by viewModel.monthlySpentCents.collectAsState()
    val incomeCents by viewModel.monthlyIncomeCents.collectAsState()
    val netBalanceCents by viewModel.monthlyNetBalanceCents.collectAsState()
    val recentTxs by viewModel.recentTransactions.collectAsState()
    val upcomingBills by viewModel.upcomingRecurring.collectAsState()
    val budgets by viewModel.monthlyBudgets.collectAsState()

    val overallBudget = budgets.find { it.category == "OVERALL" }
    val remainingBudgetCents = overallBudget?.let { it.limitCents - spentCents }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BudgetFlow",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = DateUtils.formatMonthYear(selectedMonth),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.openSubScreen("savings_goals") },
                        modifier = Modifier.testTag("home_savings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Savings Goals",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { viewModel.openSubScreen("statistics") },
                        modifier = Modifier.testTag("home_statistics_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Statistics & Reports",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { viewModel.openSubScreen("settings") },
                        modifier = Modifier.testTag("home_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Feature 1: Monthly Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openSubScreen("statistics") }
                    .testTag("home_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyUtils.formatCents(netBalanceCents, currencyCode, includeSign = false),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (netBalanceCents >= 0) MaterialTheme.colorScheme.onSurface else ExpenseRedDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Total Income
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(IncomeGreenDark.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = IncomeGreenDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Income",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyUtils.formatCents(incomeCents, currencyCode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreenDark
                            )
                        }

                        // Total Spent
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(ExpenseRedDark.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = ExpenseRedDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Spent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyUtils.formatCents(spentCents, currencyCode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRedDark
                            )
                        }
                    }

                    // Remaining Budget progress bar if set
                    if (overallBudget != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        val progress = (spentCents.toFloat() / overallBudget.limitCents.toFloat()).coerceIn(0f, 1f)
                        val isOver = spentCents > overallBudget.limitCents
                        val barColor = when {
                            isOver -> ExpenseRedDark
                            progress >= 0.8f -> Color(0xFFF59E0B)
                            else -> IncomeGreenDark
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Budget Limit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isOver) {
                                    "Over by ${CurrencyUtils.formatCents(spentCents - overallBudget.limitCents, currencyCode)}"
                                } else {
                                    "${CurrencyUtils.formatCents(remainingBudgetCents ?: 0L, currencyCode)} left"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = barColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = barColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Quick-action chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { viewModel.openAddTransaction(type = "expense") },
                        label = { Text("Add Expense") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = ExpenseRedDark,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("chip_add_expense"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    AssistChip(
                        onClick = { viewModel.openAddTransaction(type = "income") },
                        label = { Text("Add Income") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = IncomeGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("chip_add_income"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    AssistChip(
                        onClick = { viewModel.openSubScreen("statistics") },
                        label = { Text("Statistics") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("chip_view_statistics"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    AssistChip(
                        onClick = { viewModel.openSubScreen("journal_editor") },
                        label = { Text("Write Note") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("chip_write_note"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Upcoming Bills Section (Next 5 recurring transactions)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Bills",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { viewModel.openSubScreen("recurring_bills") },
                    modifier = Modifier.testTag("view_all_bills_btn")
                ) {
                    Text("Manage")
                }
            }

            if (upcomingBills.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No upcoming bills scheduled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    upcomingBills.forEach { bill ->
                        UpcomingBillCard(
                            bill = bill,
                            currencyCode = currencyCode,
                            onPayClick = { viewModel.processRecurringBill(bill) }
                        )
                    }
                }
            }
        }

        // Recent Transactions Section (Last 10 entries)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { viewModel.openSubScreen("transactions_list") },
                    modifier = Modifier.testTag("view_all_transactions_btn")
                ) {
                    Text("See All")
                }
            }
        }

        if (recentTxs.isEmpty()) {
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
                            text = "No transactions yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button to record your first expense or income.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentTxs, key = { it.id }) { tx ->
                TransactionItemRow(
                    transaction = tx,
                    currencyCode = currencyCode,
                    onClick = { viewModel.openTransactionDetail(tx) }
                )
            }
        }
    }
}

@Composable
fun UpcomingBillCard(
    bill: RecurringTransactionEntity,
    currencyCode: String,
    onPayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bill_item_${bill.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(
                    categoryName = bill.category,
                    size = 40.dp,
                    iconSize = 20.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = bill.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Due ${DateUtils.formatShortDate(bill.nextDueDateEpochDay)} • ${bill.frequency.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyUtils.formatCents(bill.amountCents, currencyCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onPayClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("pay_bill_btn_${bill.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark Paid",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paid", fontSize = 12.sp)
                }
            }
        }
    }
}
