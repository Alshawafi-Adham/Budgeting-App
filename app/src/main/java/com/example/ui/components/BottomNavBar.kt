package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

enum class AppTab(val label: String) {
    HOME("Home"),
    CALENDAR("Calendar"),
    ADD("Add"),
    NOTES("Notes"),
    BUDGET("Budget")
}

@Composable
fun BudgetFlowBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.height(72.dp)
            ) {
                // Tab 1: Home
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { onTabSelected(AppTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("bottom_nav_home")
                )

                // Tab 2: Calendar
                NavigationBarItem(
                    selected = currentTab == AppTab.CALENDAR,
                    onClick = { onTabSelected(AppTab.CALENDAR) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Calendar"
                        )
                    },
                    label = { Text("Calendar") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("bottom_nav_calendar")
                )

                // Placeholder middle slot for Add FAB
                NavigationBarItem(
                    selected = false,
                    onClick = { onAddClick() },
                    icon = { Box(modifier = Modifier.size(24.dp)) },
                    label = { Text("Add") },
                    enabled = true,
                    modifier = Modifier.testTag("bottom_nav_add_slot")
                )

                // Tab 4: Notes
                NavigationBarItem(
                    selected = currentTab == AppTab.NOTES,
                    onClick = { onTabSelected(AppTab.NOTES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.NOTES) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = "Notes"
                        )
                    },
                    label = { Text("Notes") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("bottom_nav_notes")
                )

                // Tab 5: Budget
                NavigationBarItem(
                    selected = currentTab == AppTab.BUDGET,
                    onClick = { onTabSelected(AppTab.BUDGET) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.BUDGET) Icons.Filled.PieChart else Icons.Outlined.PieChart,
                            contentDescription = "Budget"
                        )
                    },
                    label = { Text("Budget") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("bottom_nav_budget")
                )
            }

            // Center elevated Add button
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .offset(y = (-18).dp)
                    .size(54.dp)
                    .testTag("bottom_nav_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
