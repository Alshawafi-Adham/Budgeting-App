package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.SubcategoryRegistry
import com.example.ui.components.CategoryIconBadge
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.viewmodel.BudgetViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryManagerScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Expense, 1 = Income

    val filteredCategories = categories.filter {
        if (selectedTab == 0) it.type == "expense" || it.type == "both"
        else it.type == "income" || it.type == "both"
    }

    var editingCategoryTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var isNewCategoryOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("category_manager_screen")
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_from_cat_manager_btn")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Categories & Subcategories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            // Expense vs Income Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Expense Categories", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Income Categories", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCategories, key = { it.id }) { cat ->
                    val subcatsList = remember(cat) {
                        if (!cat.subcategories.isNullOrBlank()) {
                            cat.subcategories.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        } else {
                            SubcategoryRegistry.getSubcategories(cat.name)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingCategoryTarget = cat }
                            .testTag("manage_cat_card_${cat.name.lowercase()}"),
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
                                    CategoryIconBadge(categoryName = cat.name, size = 40.dp, iconSize = 20.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${subcatsList.size} subcategories",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { editingCategoryTarget = cat },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Category",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (subcatsList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    subcatsList.forEach { sub ->
                                        Box(
                                            modifier = Modifier
                                                .androidx.compose.foundation.background(
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = sub,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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

        // Add Category FAB
        FloatingActionButton(
            onClick = { isNewCategoryOpen = true },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .testTag("fab_add_new_category")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category")
        }
    }

    // Edit Category Dialog
    if (editingCategoryTarget != null) {
        val cat = editingCategoryTarget!!
        EditCategoryDialog(
            category = cat,
            onDismiss = { editingCategoryTarget = null },
            onSave = { updatedName, updatedSubcats ->
                val updatedCat = cat.copy(
                    name = updatedName,
                    subcategories = updatedSubcats.joinToString(",")
                )
                viewModel.updateCategory(updatedCat, oldName = cat.name)
                editingCategoryTarget = null
            },
            onDelete = if (!cat.isPreset) {
                {
                    viewModel.deleteCategory(cat)
                    editingCategoryTarget = null
                }
            } else null
        )
    }

    // Create New Category Dialog
    if (isNewCategoryOpen) {
        NewCategoryDialog(
            initialType = if (selectedTab == 0) "expense" else "income",
            onDismiss = { isNewCategoryOpen = false },
            onSave = { name, type, subcats ->
                viewModel.addCustomCategory(
                    name = name,
                    type = type,
                    subcategories = subcats.joinToString(",")
                )
                isNewCategoryOpen = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditCategoryDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, subcategories: List<String>) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember { mutableStateOf(category.name) }
    var currentSubcats by remember {
        mutableStateOf(
            if (!category.subcategories.isNullOrBlank()) {
                category.subcategories.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            } else {
                SubcategoryRegistry.getSubcategories(category.name).toMutableList()
            }
        )
    }
    var newSubcatInput by remember { mutableStateOf("") }
    var editingSubcatIndex by remember { mutableStateOf<Int?>(null) }
    var editingSubcatText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit: ${category.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Subcategories (Tap chip to edit)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Input for adding subcategory
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubcatInput,
                        onValueChange = { newSubcatInput = it },
                        placeholder = { Text("Add subcategory...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (newSubcatInput.isNotBlank()) {
                                currentSubcats.add(newSubcatInput.trim())
                                newSubcatInput = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentSubcats.forEachIndexed { index, sub ->
                        InputChip(
                            selected = false,
                            onClick = {
                                editingSubcatIndex = index
                                editingSubcatText = sub
                            },
                            label = { Text(sub, fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { currentSubcats.removeAt(index) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), currentSubcats)
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = ExpenseRedDark)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )

    if (editingSubcatIndex != null) {
        val targetIdx = editingSubcatIndex!!
        AlertDialog(
            onDismissRequest = { editingSubcatIndex = null },
            title = { Text("Edit Subcategory", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingSubcatText,
                    onValueChange = { editingSubcatText = it },
                    label = { Text("Subcategory Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = editingSubcatText.trim()
                        if (trimmed.isNotBlank() && targetIdx in currentSubcats.indices) {
                            currentSubcats[targetIdx] = trimmed
                        }
                        editingSubcatIndex = null
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSubcatIndex = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewCategoryDialog(
    initialType: String,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, subcategories: List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var currentSubcats by remember { mutableStateOf(mutableListOf<String>()) }
    var newSubcatInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New ${initialType.replaceFirstChar { it.uppercase() }} Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Add Subcategories",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubcatInput,
                        onValueChange = { newSubcatInput = it },
                        placeholder = { Text("e.g. Coffee, Fuel...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (newSubcatInput.isNotBlank()) {
                                currentSubcats.add(newSubcatInput.trim())
                                newSubcatInput = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentSubcats.forEachIndexed { index, sub ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(sub, fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { currentSubcats.removeAt(index) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), initialType, currentSubcats)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
