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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.TransactionItemRow
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.theme.IncomeGreenDark
import com.example.ui.theme.WarningAmber
import com.example.ui.util.CurrencyUtils
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val monthlyTxs by viewModel.monthlyTransactions.collectAsState()
    val budgets by viewModel.monthlyBudgets.collectAsState()

    var editTargetCategory by remember { mutableStateOf<String?>(null) }
    var currentLimitToEdit by remember { mutableStateOf<Long?>(null) }
    var viewingCategoryTxs by remember { mutableStateOf<String?>(null) }

    val overallBudget = budgets.find { it.category == "OVERALL" }
    val totalExpenseCents = monthlyTxs.filter { it.type.equals("expense", ignoreCase = true) }.sumOf { it.amountCents }

    // Map each category to its spent amount and budget limit
    val expenseCategories = categories.filter { it.type != "income" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 76.dp)
            .testTag("budget_screen")
    ) {
        // Month Selector Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.prevMonth() }) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Monthly Budgets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateUtils.formatMonthYear(selectedMonth),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Overall Budget Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overall_budget_card"),
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Overall Monthly Budget",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (overallBudget != null) {
                                        "Limit: ${CurrencyUtils.formatCents(overallBudget.limitCents, currencyCode)}"
                                    } else {
                                        "No overall limit set"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    editTargetCategory = "OVERALL"
                                    currentLimitToEdit = overallBudget?.limitCents
                                },
                                modifier = Modifier.testTag("edit_overall_budget_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Overall Budget")
                            }
                        }

                        if (overallBudget != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val progress = (totalExpenseCents.toFloat() / overallBudget.limitCents.toFloat()).coerceIn(0f, 1f)
                            val isOver = totalExpenseCents > overallBudget.limitCents
                            val percentage = (totalExpenseCents.toDouble() / overallBudget.limitCents.toDouble() * 100).toInt()
                            val barColor = when {
                                isOver -> ExpenseRedDark
                                percentage >= 80 -> WarningAmber
                                else -> IncomeGreenDark
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${CurrencyUtils.formatCents(totalExpenseCents, currencyCode)} spent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isOver) "Over budget by ${CurrencyUtils.formatCents(totalExpenseCents - overallBudget.limitCents, currencyCode)}"
                                    else "${CurrencyUtils.formatCents(overallBudget.limitCents - totalExpenseCents, currencyCode)} remaining ($percentage%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = barColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Category Limits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Categories Budgets List
            items(expenseCategories, key = { it.name }) { cat ->
                val catBudget = budgets.find { it.category.equals(cat.name, ignoreCase = true) }
                val catSpent = monthlyTxs
                    .filter { it.type.equals("expense", ignoreCase = true) && it.category.equals(cat.name, ignoreCase = true) }
                    .sumOf { it.amountCents }

                CategoryBudgetCard(
                    categoryName = cat.name,
                    spentCents = catSpent,
                    budgetLimitCents = catBudget?.limitCents,
                    currencyCode = currencyCode,
                    onEditBudget = {
                        editTargetCategory = cat.name
                        currentLimitToEdit = catBudget?.limitCents
                    },
                    onCategoryClick = {
                        viewingCategoryTxs = cat.name
                    }
                )
            }
        }
    }

    // Edit Budget Limit Dialog
    if (editTargetCategory != null) {
        val catName = editTargetCategory!!
        var limitInput by remember {
            mutableStateOf(currentLimitToEdit?.let { String.format("%.2f", it / 100.0) } ?: "")
        }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { editTargetCategory = null },
            title = {
                Text(text = if (catName == "OVERALL") "Set Overall Monthly Budget" else "Set Budget: $catName")
            },
            text = {
                Column {
                    Text(
                        text = "Enter monthly spending target for this category.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = {
                            limitInput = it
                            error = null
                        },
                        label = { Text("Budget Limit") },
                        prefix = { Text("${CurrencyUtils.getSymbol(currencyCode)} ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = error != null,
                        supportingText = {
                            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_budget_limit"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = CurrencyUtils.parseAmountToCents(limitInput)
                        when (parsed) {
                            is CurrencyUtils.ParseResult.Error -> error = parsed.message
                            is CurrencyUtils.ParseResult.Success -> {
                                viewModel.setCategoryBudget(catName, parsed.cents)
                                editTargetCategory = null
                            }
                        }
                    },
                    modifier = Modifier.testTag("save_budget_btn")
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                Row {
                    val existingBudget = budgets.find { it.category.equals(catName, ignoreCase = true) }
                    if (existingBudget != null) {
                        TextButton(
                            onClick = {
                                viewModel.deleteBudget(existingBudget.id)
                                editTargetCategory = null
                            }
                        ) {
                            Text("Remove", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { editTargetCategory = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Category Transactions Viewer Sheet
    if (viewingCategoryTxs != null) {
        val catName = viewingCategoryTxs!!
        val catTransactions = monthlyTxs.filter { it.category.equals(catName, ignoreCase = true) }

        CategoryTransactionsSheet(
            categoryName = catName,
            transactions = catTransactions,
            currencyCode = currencyCode,
            onDismiss = { viewingCategoryTxs = null },
            onTransactionClick = { tx ->
                viewingCategoryTxs = null
                viewModel.openTransactionDetail(tx)
            }
        )
    }
}

@Composable
fun CategoryBudgetCard(
    categoryName: String,
    spentCents: Long,
    budgetLimitCents: Long?,
    currencyCode: String,
    onEditBudget: () -> Unit,
    onCategoryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCategoryClick() }
            .testTag("cat_budget_card_${categoryName.lowercase()}"),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(categoryName = categoryName, size = 42.dp, iconSize = 22.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (budgetLimitCents != null) {
                                "Limit: ${CurrencyUtils.formatCents(budgetLimitCents, currencyCode)}"
                            } else {
                                "Tap to set limit"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onEditBudget, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit limit",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (budgetLimitCents != null && budgetLimitCents > 0) {
                val progress = (spentCents.toFloat() / budgetLimitCents.toFloat()).coerceIn(0f, 1f)
                val percentage = (spentCents.toDouble() / budgetLimitCents.toDouble() * 100).toInt()
                val isOver = spentCents > budgetLimitCents
                val barColor = when {
                    isOver -> ExpenseRedDark
                    percentage >= 80 -> WarningAmber
                    else -> IncomeGreenDark
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${CurrencyUtils.formatCents(spentCents, currencyCode)} spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isOver) {
                            "Over by ${CurrencyUtils.formatCents(spentCents - budgetLimitCents, currencyCode)}"
                        } else {
                            "${CurrencyUtils.formatCents(budgetLimitCents - spentCents, currencyCode)} left ($percentage%)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
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
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Spent this month: ${CurrencyUtils.formatCents(spentCents, currencyCode)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Set limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CategoryTransactionsSheet(
    categoryName: String,
    transactions: List<TransactionEntity>,
    currencyCode: String,
    onDismiss: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(categoryName = categoryName, size = 36.dp, iconSize = 18.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$categoryName Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions in $categoryName this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItemRow(
                            transaction = tx,
                            currencyCode = currencyCode,
                            onClick = { onTransactionClick(tx) }
                        )
                    }
                }
            }
        }
    }
}
