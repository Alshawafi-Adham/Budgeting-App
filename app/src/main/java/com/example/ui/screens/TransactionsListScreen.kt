package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TransactionItemRow
import com.example.ui.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()
    val filterDateRange by viewModel.filterDateRange.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("transactions_list_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_from_tx_list_btn")) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "All Transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier.testTag("sort_tx_btn")
                ) {
                    Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort")
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Date (Newest first)") },
                        onClick = {
                            viewModel.sortOrder.value = "date_desc"
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Date (Oldest first)") },
                        onClick = {
                            viewModel.sortOrder.value = "date_asc"
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Amount (Highest first)") },
                        onClick = {
                            viewModel.sortOrder.value = "amount_desc"
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Amount (Lowest first)") },
                        onClick = {
                            viewModel.sortOrder.value = "amount_asc"
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("search_transactions_input"),
            placeholder = { Text("Search category, merchant, note...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Type filters
            item {
                FilterChip(
                    selected = filterType == "all",
                    onClick = { viewModel.filterType.value = "all" },
                    label = { Text("All") },
                    modifier = Modifier.testTag("filter_all")
                )
            }
            item {
                FilterChip(
                    selected = filterType == "expense",
                    onClick = { viewModel.filterType.value = "expense" },
                    label = { Text("Expenses") },
                    modifier = Modifier.testTag("filter_expenses")
                )
            }
            item {
                FilterChip(
                    selected = filterType == "income",
                    onClick = { viewModel.filterType.value = "income" },
                    label = { Text("Income") },
                    modifier = Modifier.testTag("filter_income")
                )
            }

            // Date Range filters
            item {
                FilterChip(
                    selected = filterDateRange == "this_month",
                    onClick = {
                        viewModel.filterDateRange.value =
                            if (filterDateRange == "this_month") "all" else "this_month"
                    },
                    label = { Text("This Month") }
                )
            }
            item {
                FilterChip(
                    selected = filterDateRange == "last_month",
                    onClick = {
                        viewModel.filterDateRange.value =
                            if (filterDateRange == "last_month") "all" else "last_month"
                    },
                    label = { Text("Last Month") }
                )
            }

            // Category filter chips
            items(categories) { cat ->
                val isSelected = filterCategory == cat.name
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.filterCategory.value = if (isSelected) null else cat.name
                    },
                    label = { Text(cat.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Results count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${transactions.size} transactions found",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (filterCategory != null || filterType != "all" || filterDateRange != "all" || searchQuery.isNotEmpty()) {
                TextButton(
                    onClick = {
                        viewModel.filterCategory.value = null
                        viewModel.filterType.value = "all"
                        viewModel.filterDateRange.value = "all"
                        viewModel.searchQuery.value = ""
                    }
                ) {
                    Text("Clear filters", fontSize = 12.sp)
                }
            }
        }

        // Transactions list
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No matching transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try adjusting your search query or active filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        currencyCode = currencyCode,
                        onClick = { viewModel.openTransactionDetail(tx) }
                    )
                }
            }
        }
    }
}
