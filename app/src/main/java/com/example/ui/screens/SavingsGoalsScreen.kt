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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Unarchive
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavingsGoalEntity
import com.example.ui.theme.IncomeGreenDark
import com.example.ui.util.CurrencyUtils
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.BudgetViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allGoals by viewModel.allSavingsGoals.collectAsState()
    val allContribs by viewModel.allContributions.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()

    var showArchived by remember { mutableStateOf(false) }
    var isNewGoalOpen by remember { mutableStateOf(false) }
    var contributingGoalTarget by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    val displayedGoals = remember(allGoals, showArchived) {
        if (showArchived) allGoals else allGoals.filter { !it.isArchived }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("savings_goals_screen")
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_from_savings_btn")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Savings Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = showArchived,
                    onClick = { showArchived = !showArchived },
                    label = { Text("Archived") }
                )
            }

            if (displayedGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (showArchived) "No archived goals" else "No active savings goals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set a target for an emergency fund, travel, or big purchase.",
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
                    items(displayedGoals, key = { it.id }) { goal ->
                        val goalContribs = allContribs.filter { it.goalId == goal.id }
                        val currentCents = goalContribs.sumOf { it.amountCents }

                        SavingsGoalCard(
                            goal = goal,
                            currentCents = currentCents,
                            currencyCode = currencyCode,
                            onAddContribution = { contributingGoalTarget = goal },
                            onToggleArchive = { viewModel.toggleArchiveSavingsGoal(goal) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { isNewGoalOpen = true },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .testTag("fab_add_savings_goal")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal")
        }
    }

    // New Goal Dialog
    if (isNewGoalOpen) {
        NewSavingsGoalDialog(
            currencyCode = currencyCode,
            onDismiss = { isNewGoalOpen = false },
            onSave = { name, targetCents, targetDateEpoch ->
                viewModel.saveSavingsGoal(name, targetCents, targetDateEpoch)
                isNewGoalOpen = false
            }
        )
    }

    // Add Contribution Dialog
    if (contributingGoalTarget != null) {
        val target = contributingGoalTarget!!
        AddContributionDialog(
            goalName = target.name,
            currencyCode = currencyCode,
            onDismiss = { contributingGoalTarget = null },
            onSave = { amountCents, note ->
                viewModel.addSavingsContribution(
                    goalId = target.id,
                    amountCents = amountCents,
                    dateEpochDay = LocalDate.now().toEpochDay(),
                    note = note
                )
                contributingGoalTarget = null
            }
        )
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoalEntity,
    currentCents: Long,
    currencyCode: String,
    onAddContribution: () -> Unit,
    onToggleArchive: () -> Unit
) {
    val progress = (currentCents.toFloat() / goal.targetAmountCents.toFloat()).coerceIn(0f, 1f)
    val percentage = (currentCents.toDouble() / goal.targetAmountCents.toDouble() * 100).toInt()
    val isCompleted = currentCents >= goal.targetAmountCents

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("savings_goal_card_${goal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Savings,
                        contentDescription = null,
                        tint = if (isCompleted) IncomeGreenDark else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (goal.targetDateEpochDay != null) {
                            Text(
                                text = "Target: ${DateUtils.formatFullDate(goal.targetDateEpochDay)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                IconButton(onClick = onToggleArchive, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (goal.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = "Archive Goal",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${CurrencyUtils.formatCents(currentCents, currencyCode)} of ${CurrencyUtils.formatCents(goal.targetAmountCents, currencyCode)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) IncomeGreenDark else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isCompleted) IncomeGreenDark else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onAddContribution,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_contribution_btn_${goal.id}")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Contribution")
                }
            }
        }
    }
}

@Composable
fun NewSavingsGoalDialog(
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (name: String, targetCents: Long, targetDateEpoch: Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Savings Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name (e.g. Travel, Laptop)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_goal_name"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = {
                        targetAmount = it
                        error = null
                    },
                    label = { Text("Target Amount") },
                    prefix = { Text("${CurrencyUtils.getSymbol(currencyCode)} ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = error != null,
                    supportingText = {
                        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_goal_target_amount"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        error = "Goal name is required"
                        return@Button
                    }
                    val parsed = CurrencyUtils.parseAmountToCents(targetAmount)
                    when (parsed) {
                        is CurrencyUtils.ParseResult.Error -> error = parsed.message
                        is CurrencyUtils.ParseResult.Success -> {
                            onSave(name.trim(), parsed.cents, LocalDate.now().plusMonths(6).toEpochDay())
                        }
                    }
                },
                modifier = Modifier.testTag("save_goal_btn")
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddContributionDialog(
    goalName: String,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (amountCents: Long, note: String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contribute to $goalName") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        error = null
                    },
                    label = { Text("Contribution Amount") },
                    prefix = { Text("${CurrencyUtils.getSymbol(currencyCode)} ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = error != null,
                    supportingText = {
                        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_contrib_amount"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_contrib_note"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = CurrencyUtils.parseAmountToCents(amount)
                    when (parsed) {
                        is CurrencyUtils.ParseResult.Error -> error = parsed.message
                        is CurrencyUtils.ParseResult.Success -> {
                            onSave(parsed.cents, note.trim().ifEmpty { null })
                        }
                    }
                },
                modifier = Modifier.testTag("save_contrib_btn")
            ) {
                Text("Add Contribution")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
