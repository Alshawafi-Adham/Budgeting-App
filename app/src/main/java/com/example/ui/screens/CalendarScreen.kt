package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.theme.IncomeGreenDark
import com.example.ui.util.CurrencyUtils
import com.example.ui.util.DateUtils
import com.example.ui.viewmodel.BudgetViewModel
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allRecurring by viewModel.allRecurring.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val selectedDay by viewModel.selectedCalendarDay.collectAsState()

    val startOfWeekSetting = userSettings["start_of_week"] ?: "sunday"
    val isSundayStart = startOfWeekSetting.equals("sunday", ignoreCase = true)

    val today = remember { LocalDate.now() }

    // Month totals
    val startOfMonthEpoch = selectedMonth.withDayOfMonth(1).toEpochDay()
    val endOfMonthEpoch = selectedMonth.plusMonths(1).minusDays(1).toEpochDay()

    val monthTransactions = remember(allTransactions, selectedMonth) {
        allTransactions.filter { it.dateEpochDay in startOfMonthEpoch..endOfMonthEpoch }
    }

    val totalSpent = remember(monthTransactions) {
        monthTransactions.filter { it.type.equals("expense", ignoreCase = true) }.sumOf { it.amountCents }
    }
    val totalIncome = remember(monthTransactions) {
        monthTransactions.filter { it.type.equals("income", ignoreCase = true) }.sumOf { it.amountCents }
    }

    // Grid days calculation
    val daysInMonth = selectedMonth.lengthOfMonth()
    val firstDayOfMonth = selectedMonth.withDayOfMonth(1)
    val dayOfWeekVal = firstDayOfMonth.dayOfWeek.value // 1 (Mon) - 7 (Sun)
    val leadingEmptyDays = if (isSundayStart) {
        if (dayOfWeekVal == 7) 0 else dayOfWeekVal
    } else {
        dayOfWeekVal - 1
    }

    val dayHeaders = if (isSundayStart) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    } else {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
            .testTag("calendar_screen")
    ) {
        // Month Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.prevMonth() },
                modifier = Modifier.testTag("calendar_prev_month_btn")
            ) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = DateUtils.formatMonthYear(selectedMonth),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Spent: ${CurrencyUtils.formatCents(totalSpent, currencyCode)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ExpenseRedDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Earned: ${CurrencyUtils.formatCents(totalIncome, currencyCode)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = IncomeGreenDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(
                onClick = { viewModel.nextMonth() },
                modifier = Modifier.testTag("calendar_next_month_btn")
            ) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }

        // Days of week header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dayHeaders.forEach { dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Calendar Days Grid
        val totalCells = leadingEmptyDays + daysInMonth
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(totalCells) { index ->
                if (index < leadingEmptyDays) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val dayNum = index - leadingEmptyDays + 1
                    val date = selectedMonth.withDayOfMonth(dayNum)
                    val dateEpoch = date.toEpochDay()
                    val isToday = date == today

                    val dayTxs = allTransactions.filter { it.dateEpochDay == dateEpoch }
                    val hasExpenses = dayTxs.any { it.type.equals("expense", ignoreCase = true) }
                    val hasIncome = dayTxs.any { it.type.equals("income", ignoreCase = true) }

                    // Recurring bills due on this date (faint icon)
                    val hasRecurringDue = allRecurring.any {
                        !it.isPaused && it.nextDueDateEpochDay == dateEpoch
                    }

                    // Dot color coding: green (income only), red (expense only), gray (mixed)
                    val dotColor = when {
                        hasExpenses && hasIncome -> Color.Gray
                        hasExpenses -> ExpenseRedDark
                        hasIncome -> IncomeGreenDark
                        else -> null
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .then(
                                if (isToday) Modifier.border(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                ) else Modifier
                            )
                            .combinedClickable(
                                onClick = { viewModel.openDayDetail(date) },
                                onLongClick = {
                                    // Long-press a day → quick add expense for that date
                                    viewModel.openAddTransaction(
                                        type = "expense",
                                        dateEpochDay = dateEpoch
                                    )
                                }
                            )
                            .testTag("cal_day_$dayNum"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (dotColor != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(dotColor, CircleShape)
                                    )
                                }

                                if (hasRecurringDue) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = "Recurring Bill",
                                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Legend / Helper text
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(ExpenseRedDark, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expense", style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(IncomeGreenDark, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Income", style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color.Gray, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mixed", style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recurring", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    // Day detail sheet if day selected
    if (selectedDay != null) {
        DayDetailSheet(
            date = selectedDay!!,
            viewModel = viewModel
        )
    }
}
