package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JournalEntryEntity
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.BudgetViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val journalEntries by viewModel.journalEntries.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedMoodFilter by remember { mutableStateOf<String?>(null) }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }

    var editingEntry by remember { mutableStateOf<JournalEntryEntity?>(null) }
    var isEditorOpen by remember { mutableStateOf(false) }
    var viewingEntry by remember { mutableStateOf<JournalEntryEntity?>(null) }

    // Collect all unique tags
    val allTags = remember(journalEntries) {
        journalEntries.flatMap { entry ->
            entry.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        }.distinct()
    }

    // Filter journal entries
    val filteredEntries = remember(journalEntries, searchQuery, selectedMoodFilter, selectedTagFilter) {
        val query = searchQuery.trim().lowercase()
        journalEntries.filter { entry ->
            val matchesQuery = query.isEmpty() ||
                    (entry.title?.lowercase()?.contains(query) == true) ||
                    entry.body.lowercase().contains(query)
            val matchesMood = selectedMoodFilter == null || entry.mood.equals(selectedMoodFilter, ignoreCase = true)
            val matchesTag = selectedTagFilter == null || entry.tags?.split(",")?.any { it.trim().equals(selectedTagFilter, ignoreCase = true) } == true
            matchesQuery && matchesMood && matchesTag
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("notes_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Financial Notes & Journal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Reflect on spending patterns and intentions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("search_journal_input"),
                placeholder = { Text("Search title, reflections, tags...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mood & Tag Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedMoodFilter == null && selectedTagFilter == null,
                        onClick = {
                            selectedMoodFilter = null
                            selectedTagFilter = null
                        },
                        label = { Text("All") }
                    )
                }

                listOf("proud", "good", "neutral", "stressed").forEach { mood ->
                    item {
                        FilterChip(
                            selected = selectedMoodFilter == mood,
                            onClick = {
                                selectedMoodFilter = if (selectedMoodFilter == mood) null else mood
                            },
                            label = { Text(mood.replaceFirstChar { it.uppercase() }) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getMoodIcon(mood),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = getMoodColor(mood)
                                )
                            }
                        )
                    }
                }

                allTags.forEach { tag ->
                    item {
                        FilterChip(
                            selected = selectedTagFilter == tag,
                            onClick = {
                                selectedTagFilter = if (selectedTagFilter == tag) null else tag
                            },
                            label = { Text("#$tag") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No journal entries found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button below to write a financial reflection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredEntries, key = { it.id }) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onClick = { viewingEntry = entry },
                            onLongClick = { viewModel.togglePinJournalEntry(entry) },
                            onPinToggle = { viewModel.togglePinJournalEntry(entry) }
                        )
                    }
                }
            }
        }

        // New Journal Entry Floating Action Button
        FloatingActionButton(
            onClick = {
                editingEntry = null
                isEditorOpen = true
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp)
                .testTag("fab_new_journal_entry")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Write Note")
        }
    }

    // Editor Dialog
    if (isEditorOpen) {
        JournalEditorDialog(
            entry = editingEntry,
            onDismiss = { isEditorOpen = false },
            onSave = { title, body, mood, tags ->
                viewModel.saveJournalEntry(
                    title = title,
                    body = body,
                    dateEpochDay = editingEntry?.dateEpochDay ?: System.currentTimeMillis() / 86400000,
                    mood = mood,
                    tags = tags,
                    editingId = editingEntry?.id
                )
                isEditorOpen = false
            }
        )
    }

    // View Entry Dialog
    if (viewingEntry != null) {
        val entry = viewingEntry!!
        AlertDialog(
            onDismissRequest = { viewingEntry = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.title ?: "Financial Reflection",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (entry.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = DateUtils.formatFullDate(entry.dateEpochDay),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (entry.mood != null) {
                            Text(
                                text = "Mood: ${entry.mood.replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = getMoodColor(entry.mood),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = entry.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!entry.tags.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            entry.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "#$tag", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editingEntry = entry
                        viewingEntry = null
                        isEditorOpen = true
                    }
                ) {
                    Text("Edit")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.deleteJournalEntry(entry.id)
                            viewingEntry = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { viewingEntry = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JournalEntryCard(
    entry: JournalEntryEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPinToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("journal_card_${entry.id}"),
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
                    if (entry.mood != null) {
                        Icon(
                            imageVector = getMoodIcon(entry.mood),
                            contentDescription = entry.mood,
                            tint = getMoodColor(entry.mood),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = DateUtils.formatShortDate(entry.dateEpochDay),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = onPinToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (entry.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin entry",
                        tint = if (entry.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!entry.title.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (!entry.tags.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "#$tag", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JournalEditorDialog(
    entry: JournalEntryEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String?, body: String, mood: String?, tags: String?) -> Unit
) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var body by remember { mutableStateOf(entry?.body ?: "") }
    var selectedMood by remember { mutableStateOf(entry?.mood) }
    var tags by remember { mutableStateOf(entry?.tags ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (entry != null) "Edit Financial Note" else "New Financial Note")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("journal_input_title"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Reflection", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "${body.length}/2000",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (body.length > 2000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = {
                        if (it.length <= 2000) body = it
                        error = null
                    },
                    placeholder = { Text("What made you spend or save? How are you feeling about your money today?") },
                    modifier = Modifier.fillMaxWidth().height(140.dp).testTag("journal_input_body"),
                    shape = RoundedCornerShape(12.dp),
                    isError = error != null,
                    supportingText = {
                        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Mood", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("proud", "good", "neutral", "stressed").forEach { mood ->
                        val isSelected = selectedMood == mood
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMood = if (isSelected) null else mood },
                            label = { Text(mood.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated, e.g. groceries, mindful)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("journal_input_tags"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (body.trim().isEmpty()) {
                        error = "Note body is required"
                    } else {
                        onSave(title.trim().ifEmpty { null }, body.trim(), selectedMood, tags.trim().ifEmpty { null })
                    }
                },
                modifier = Modifier.testTag("save_journal_entry_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getMoodIcon(mood: String): ImageVector {
    return when (mood.lowercase().trim()) {
        "good" -> Icons.Default.SentimentSatisfied
        "proud" -> Icons.Default.SentimentVerySatisfied
        "stressed" -> Icons.Default.MoodBad
        else -> Icons.Default.Mood
    }
}

fun getMoodColor(mood: String): Color {
    return when (mood.lowercase().trim()) {
        "good" -> Color(0xFF10B981)
        "proud" -> Color(0xFF3B82F6)
        "stressed" -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }
}
