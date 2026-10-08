package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecurringTransactionEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.util.CurrencyUtils
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.BudgetViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringBillsScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allRecurring by viewModel.allRecurring.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var isNewRuleOpen by remember { mutableStateOf(false) }
    var editingRuleTarget by remember { mutableStateOf<RecurringTransactionEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("recurring_bills_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
        ) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_from_recurring_btn")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Recurring Bills & Income",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            if (allRecurring.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recurring transactions yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add subscriptions, rent, insurance, or regular paycheck rules.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allRecurring, key = { it.id }) { rule ->
                        RecurringRuleCard(
                            rule = rule,
                            currencyCode = currencyCode,
                            onPay = { viewModel.processRecurringBill(rule) },
                            onTogglePause = { viewModel.togglePauseRecurringRule(rule) },
                            onDelete = { viewModel.deleteRecurringRule(rule.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingRuleTarget = null
                isNewRuleOpen = true
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .testTag("fab_add_recurring_rule")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Rule")
        }
    }

    if (isNewRuleOpen) {
        NewRecurringRuleDialog(
            currencyCode = currencyCode,
            categories = categories.map { it.name },
            onDismiss = { isNewRuleOpen = false },
            onSave = { name, amountCents, type, category, frequency, dayOfMonth, nextDateEpoch ->
                viewModel.saveRecurringRule(
                    name = name,
                    amountCents = amountCents,
                    type = type,
                    category = category,
                    frequency = frequency,
                    nextDueDateEpochDay = nextDateEpoch,
                    dayOfMonth = dayOfMonth
                )
                isNewRuleOpen = false
            }
        )
    }
}

@Composable
fun RecurringRuleCard(
    rule: RecurringTransactionEntity,
    currencyCode: String,
    onPay: () -> Unit,
    onTogglePause: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recurring_card_${rule.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isPaused) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
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
                    CategoryIconBadge(categoryName = rule.category, size = 42.dp, iconSize = 22.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rule.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (rule.isPaused) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Paused",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Text(
                            text = "${rule.frequency.replaceFirstChar { it.uppercase() }} • Next due: ${DateUtils.formatFullDate(rule.nextDueDateEpochDay)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Text(
                    text = CurrencyUtils.formatCents(rule.amountCents, currencyCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = onTogglePause, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (rule.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (rule.isPaused) "Resume" else "Pause",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (!rule.isPaused) {
                    FilledTonalButton(
                        onClick = onPay,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("mark_recurring_paid_${rule.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Bill")
                    }
                }
            }
        }
    }
}

@Composable
fun NewRecurringRuleDialog(
    currencyCode: String,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (name: String, amountCents: Long, type: String, category: String, frequency: String, dayOfMonth: Int, nextDateEpoch: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var category by remember { mutableStateOf(if (categories.isNotEmpty()) categories.first() else "Utilities") }
    var frequency by remember { mutableStateOf("monthly") }
    var dayOfMonth by remember { mutableStateOf(LocalDate.now().dayOfMonth.coerceIn(1, 31)) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Recurring Transaction") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bill / Income Name (e.g. Netflix, Rent)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_recurring_name"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        error = null
                    },
                    label = { Text("Amount") },
                    prefix = { Text("${CurrencyUtils.getSymbol(currencyCode)} ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = error != null,
                    supportingText = {
                        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_recurring_amount"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Frequency", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("weekly", "monthly", "yearly").forEach { freq ->
                        FilterChip(
                            selected = frequency == freq,
                            onClick = { frequency = freq },
                            label = { Text(freq.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Anchor day: Day $dayOfMonth (clamps automatically for shorter months like Feb 28/29)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        error = "Name is required"
                        return@Button
                    }
                    val parsed = CurrencyUtils.parseAmountToCents(amountText)
                    when (parsed) {
                        is CurrencyUtils.ParseResult.Error -> error = parsed.message
                        is CurrencyUtils.ParseResult.Success -> {
                            val today = LocalDate.now()
                            val nextDate = DateUtils.calculateNextDueDate(
                                currentDate = today,
                                frequency = frequency,
                                anchorDayOfMonth = dayOfMonth
                            )
                            onSave(name.trim(), parsed.cents, type, category, frequency, dayOfMonth, nextDate.toEpochDay())
                        }
                    }
                },
                modifier = Modifier.testTag("save_recurring_rule_btn")
            ) {
                Text("Create Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
