package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryIconBadge
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.util.CurrencyUtils
import com.example.ui.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userSettings by viewModel.userSettings.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val currentCurrency = userSettings["currency"] ?: "MYR"
    val currentTheme = userSettings["theme"] ?: "system"
    val currentStartOfWeek = userSettings["start_of_week"] ?: "sunday"
    val currentCycleDay = userSettings["budget_cycle_start_day"] ?: "1"

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showStartOfWeekDialog by remember { mutableStateOf(false) }
    var showCycleDayDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm1 by remember { mutableStateOf(false) }
    var showDeleteConfirm2 by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_from_settings_btn")) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preferences section
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.MonetizationOn,
                            title = "Currency",
                            subtitle = "$currentCurrency (${CurrencyUtils.getSymbol(currentCurrency)})",
                            onClick = { showCurrencyDialog = true },
                            tag = "setting_currency"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        SettingsRow(
                            icon = Icons.Default.DarkMode,
                            title = "Theme",
                            subtitle = currentTheme.replaceFirstChar { it.uppercase() },
                            onClick = { showThemeDialog = true },
                            tag = "setting_theme"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        SettingsRow(
                            icon = Icons.Default.DateRange,
                            title = "Start of Week",
                            subtitle = currentStartOfWeek.replaceFirstChar { it.uppercase() },
                            onClick = { showStartOfWeekDialog = true },
                            tag = "setting_start_of_week"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        SettingsRow(
                            icon = Icons.Default.DateRange,
                            title = "Budget Cycle Start Day",
                            subtitle = "Day $currentCycleDay of month",
                            onClick = { showCycleDayDialog = true },
                            tag = "setting_cycle_day"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        SettingsRow(
                            icon = Icons.Default.Category,
                            title = "Categories & Subcategories",
                            subtitle = "Customize expense & income subcategories",
                            onClick = { viewModel.openSubScreen("category_manager") },
                            tag = "setting_categories_manager"
                        )
                    }
                }
            }

            // Categories & Subcategories section
            item {
                Text(
                    text = "Categories & Subcategories",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openSubScreen("category_manager") }
                        .testTag("setting_categories_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Manage Categories & Subcategories",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${categories.size} categories • Tap to edit anytime",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category preview badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.take(5).forEach { cat ->
                                CategoryIconBadge(
                                    categoryName = cat.name,
                                    size = 32.dp,
                                    iconSize = 16.dp
                                )
                            }
                            if (categories.size > 5) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .androidx.compose.foundation.background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            androidx.compose.foundation.shape.CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${categories.size - 5}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.openSubScreen("category_manager") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_edit_categories_settings"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Edit Categories & Subcategories")
                        }
                    }
                }
            }

            // Data Management section
            item {
                Text(
                    text = "Data & Backup (Local-First)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Upload,
                            title = "Export Backup (JSON)",
                            subtitle = "Copy complete database backup to clipboard",
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.repository.exportToJson()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("BudgetFlow Backup", json))
                                    Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            tag = "btn_export_json"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        SettingsRow(
                            icon = Icons.Default.Upload,
                            title = "Export Transactions (CSV)",
                            subtitle = "Copy spreadsheet CSV to clipboard",
                            onClick = {
                                coroutineScope.launch {
                                    val csv = viewModel.repository.exportToCsv()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("BudgetFlow CSV", csv))
                                    Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            tag = "btn_export_csv"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        SettingsRow(
                            icon = Icons.Default.Download,
                            title = "Import Backup (JSON)",
                            subtitle = "Restore data from JSON backup",
                            onClick = { showImportDialog = true },
                            tag = "btn_import_json"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        SettingsRow(
                            icon = Icons.Default.DeleteForever,
                            title = "Delete All Data",
                            subtitle = "Permanently wipe all records from this device",
                            onClick = { showDeleteConfirm1 = true },
                            titleColor = ExpenseRedDark,
                            tag = "btn_delete_all_data"
                        )
                    }
                }
            }

            // About section
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "BudgetFlow v1.0",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "\"Your money, your story.\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Manual-first personal budgeting app built for reflection and financial mindfulness. All data is kept 100% locally on your device in SQLite. No account, no ads, and no external tracking required.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Currency selector dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(CurrencyUtils.SUPPORTED_CURRENCIES) { curr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSetting("currency", curr.code)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentCurrency.equals(curr.code, ignoreCase = true),
                                onClick = {
                                    viewModel.updateSetting("currency", curr.code)
                                    showCurrencyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${curr.name} (${curr.symbol})", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Close") }
            }
        )
    }

    // Theme selector dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("App Theme") },
            text = {
                Column {
                    listOf("system" to "System Default", "light" to "Light Mode", "dark" to "Dark Mode").forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSetting("theme", key)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme.equals(key, ignoreCase = true),
                                onClick = {
                                    viewModel.updateSetting("theme", key)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    // Start of week dialog
    if (showStartOfWeekDialog) {
        AlertDialog(
            onDismissRequest = { showStartOfWeekDialog = false },
            title = { Text("Start of Week") },
            text = {
                Column {
                    listOf("sunday" to "Sunday", "monday" to "Monday").forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSetting("start_of_week", key)
                                    showStartOfWeekDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentStartOfWeek.equals(key, ignoreCase = true),
                                onClick = {
                                    viewModel.updateSetting("start_of_week", key)
                                    showStartOfWeekDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStartOfWeekDialog = false }) { Text("Close") }
            }
        )
    }

    // Budget Cycle Day dialog
    if (showCycleDayDialog) {
        var cycleInput by remember { mutableStateOf(currentCycleDay) }
        AlertDialog(
            onDismissRequest = { showCycleDayDialog = false },
            title = { Text("Budget Cycle Start Day") },
            text = {
                Column {
                    Text(
                        text = "Choose day of the month your budget resets (e.g. 1st or your payday).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cycleInput,
                        onValueChange = {
                            val num = it.toIntOrNull()
                            if (num != null && num in 1..28) {
                                cycleInput = it
                            } else if (it.isEmpty()) {
                                cycleInput = it
                            }
                        },
                        label = { Text("Day of month (1 - 28)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = cycleInput.toIntOrNull() ?: 1
                        viewModel.updateSetting("budget_cycle_start_day", num.toString())
                        showCycleDayDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCycleDayDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Import Backup dialog
    if (showImportDialog) {
        var jsonText by remember { mutableStateOf("") }
        var importError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restore From Backup") },
            text = {
                Column {
                    Text(
                        text = "Paste the JSON backup content below to restore records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonText,
                        onValueChange = {
                            jsonText = it
                            importError = null
                        },
                        placeholder = { Text("Paste JSON here...") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        isError = importError != null,
                        supportingText = {
                            if (importError != null) Text(importError!!, color = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jsonText.trim().isEmpty()) {
                            importError = "Please paste JSON backup data"
                            return@Button
                        }
                        coroutineScope.launch {
                            val result = viewModel.repository.importFromJson(jsonText.trim())
                            result.onSuccess { count ->
                                Toast.makeText(context, "Restored $count records!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            }.onFailure {
                                importError = "Invalid JSON format: ${it.localizedMessage}"
                            }
                        }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete All Confirmation 1
    if (showDeleteConfirm1) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm1 = false },
            title = { Text("Delete All Data?", color = ExpenseRedDark) },
            text = {
                Text("This will permanently remove all transactions, budgets, journal notes, and savings goals from your device. Are you sure you want to proceed?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm1 = false
                        showDeleteConfirm2 = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRedDark)
                ) {
                    Text("Yes, Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm1 = false }) { Text("Cancel") }
            }
        )
    }

    // Delete All Confirmation 2 (Double confirmation required by spec)
    if (showDeleteConfirm2) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm2 = false },
            title = { Text("Final Warning", color = ExpenseRedDark, fontWeight = FontWeight.Bold) },
            text = {
                Text("This action CANNOT be undone. All your financial logs will be wiped permanently.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm2 = false
                        viewModel.deleteAllData()
                        Toast.makeText(context, "All data has been deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRedDark)
                ) {
                    Text("Permanently Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm2 = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == ExpenseRedDark) ExpenseRedDark else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
    }
}
