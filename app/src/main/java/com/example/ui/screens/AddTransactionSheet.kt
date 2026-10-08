package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.theme.IncomeGreenDark
import com.example.ui.util.CurrencyUtils
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.BudgetViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editingTx by viewModel.editTransactionTarget.collectAsState()
    val prefillType by viewModel.addTransactionType.collectAsState()
    val prefillDateEpochDay by viewModel.addTransactionDateEpochDay.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()

    // Form state
    var selectedType by remember(editingTx, prefillType) {
        mutableStateOf(editingTx?.type ?: prefillType)
    }

    val initialAmount = remember(editingTx) {
        editingTx?.let { String.format("%.2f", it.amountCents / 100.0) } ?: ""
    }
    var amountText by remember(editingTx) { mutableStateOf(initialAmount) }
    var amountError by remember { mutableStateOf<String?>(null) }

    var selectedCategory by remember(editingTx, selectedType, categories) {
        mutableStateOf(
            editingTx?.category ?: if (selectedType == "income") "Salary" else "Food"
        )
    }

    var selectedDateEpochDay by remember(editingTx, prefillDateEpochDay) {
        mutableStateOf(editingTx?.dateEpochDay ?: prefillDateEpochDay)
    }

    var noteText by remember(editingTx) {
        mutableStateOf(editingTx?.note ?: "")
    }

    var selectedPaymentMethod by remember(editingTx) {
        mutableStateOf(editingTx?.paymentMethod)
    }

    var isRecurring by remember(editingTx) {
        mutableStateOf(editingTx?.isRecurring ?: false)
    }

    // Custom category dialog state
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    // Date picker dialog state
    var showDatePickerDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeAddTransaction() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("add_transaction_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingTx != null) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = { viewModel.closeAddTransaction() },
                    modifier = Modifier.testTag("close_add_tx_btn")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Type Toggle: Expense vs Income
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                val isExpense = selectedType == "expense"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isExpense) ExpenseRedDark else Color.Transparent)
                        .clickable {
                            selectedType = "expense"
                            if (selectedCategory == "Salary") selectedCategory = "Food"
                        }
                        .padding(vertical = 10.dp)
                        .testTag("type_expense_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense",
                        fontWeight = FontWeight.Bold,
                        color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isExpense) IncomeGreenDark else Color.Transparent)
                        .clickable {
                            selectedType = "income"
                            if (selectedCategory == "Food") selectedCategory = "Salary"
                        }
                        .padding(vertical = 10.dp)
                        .testTag("type_income_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income",
                        fontWeight = FontWeight.Bold,
                        color = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            Text(
                text = "Amount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    amountError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_tx_amount"),
                placeholder = { Text("0.00") },
                prefix = {
                    Text(
                        text = "${CurrencyUtils.getSymbol(currencyCode)} ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedType == "expense") ExpenseRedDark else IncomeGreenDark
                    )
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = amountError != null,
                supportingText = {
                    if (amountError != null) {
                        Text(
                            text = amountError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { showAddCategoryDialog = true },
                    modifier = Modifier.testTag("add_custom_category_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Custom", fontSize = 13.sp)
                }
            }

            val matchingCategories = categories.filter {
                it.type == "both" || it.type.equals(selectedType, ignoreCase = true)
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                matchingCategories.forEach { cat ->
                    val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat.name },
                        label = { Text(cat.name) },
                        leadingIcon = {
                            CategoryIconBadge(
                                categoryName = cat.name,
                                size = 24.dp,
                                iconSize = 14.dp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("cat_chip_${cat.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Selection
            Text(
                text = "Date",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { showDatePickerDialog = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("tx_date_picker_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = DateUtils.formatFullDate(selectedDateEpochDay),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Chips (Optional)
            Text(
                text = "Payment Method (Optional)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("cash", "card", "transfer").forEach { method ->
                    val isSelected = selectedPaymentMethod == method
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedPaymentMethod = if (isSelected) null else method
                        },
                        label = { Text(method.replaceFirstChar { it.uppercase() }) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (method) {
                                    "cash" -> Icons.Default.Money
                                    "card" -> Icons.Default.CreditCard
                                    else -> Icons.Default.SyncAlt
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("payment_chip_$method")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note (max 500 characters)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Note (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${noteText.length}/500",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (noteText.length > 500) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = { if (it.length <= 500) noteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_tx_note"),
                placeholder = { Text("Add context, merchant, or notes...") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Recurring rule toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recurring Transaction",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Mark as recurring bill or regular income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    modifier = Modifier.testTag("switch_is_recurring")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save / Submit Button
            Button(
                onClick = {
                    val parseResult = CurrencyUtils.parseAmountToCents(amountText)
                    when (parseResult) {
                        is CurrencyUtils.ParseResult.Error -> {
                            amountError = parseResult.message
                        }
                        is CurrencyUtils.ParseResult.Success -> {
                            viewModel.saveTransaction(
                                amountCents = parseResult.cents,
                                type = selectedType,
                                category = selectedCategory,
                                dateEpochDay = selectedDateEpochDay,
                                note = noteText.trim().ifEmpty { null },
                                paymentMethod = selectedPaymentMethod,
                                isRecurring = isRecurring,
                                editingId = editingTx?.id
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == "expense") ExpenseRedDark else IncomeGreenDark
                )
            ) {
                Text(
                    text = if (editingTx != null) "Update Transaction" else "Save Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Add Custom Category Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Custom Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_cat_name")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCustomCategory(
                                name = newCategoryName.trim(),
                                type = selectedType
                            )
                            selectedCategory = newCategoryName.trim()
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_custom_cat_btn")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Date Picker Selector Dialog
    if (showDatePickerDialog) {
        val today = LocalDate.now()
        val dates = listOf(
            today to "Today",
            today.minusDays(1) to "Yesterday",
            today.minusDays(2) to "${today.minusDays(2).dayOfWeek.name.take(3)}, ${DateUtils.formatShortDate(today.minusDays(2).toEpochDay())}",
            today.minusDays(3) to "${today.minusDays(3).dayOfWeek.name.take(3)}, ${DateUtils.formatShortDate(today.minusDays(3).toEpochDay())}",
            today.minusDays(4) to "${today.minusDays(4).dayOfWeek.name.take(3)}, ${DateUtils.formatShortDate(today.minusDays(4).toEpochDay())}"
        )

        AlertDialog(
            onDismissRequest = { showDatePickerDialog = false },
            title = { Text("Select Date") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dates.forEach { (date, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedDateEpochDay = date.toEpochDay()
                                    showDatePickerDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedDateEpochDay == date.toEpochDay()) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedDateEpochDay == date.toEpochDay()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
