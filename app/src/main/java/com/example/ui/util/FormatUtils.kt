package com.example.ui.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object CurrencyUtils {
    val SUPPORTED_CURRENCIES = listOf(
        CurrencyInfo("MYR", "RM", "Malaysian Ringgit"),
        CurrencyInfo("USD", "$", "US Dollar"),
        CurrencyInfo("EUR", "€", "Euro"),
        CurrencyInfo("GBP", "£", "British Pound"),
        CurrencyInfo("SGD", "S$", "Singapore Dollar"),
        CurrencyInfo("AUD", "A$", "Australian Dollar"),
        CurrencyInfo("CAD", "C$", "Canadian Dollar"),
        CurrencyInfo("JPY", "¥", "Japanese Yen"),
        CurrencyInfo("INR", "₹", "Indian Rupee")
    )

    fun getSymbol(code: String): String {
        return SUPPORTED_CURRENCIES.find { it.code.equals(code, ignoreCase = true) }?.symbol ?: code
    }

    /**
     * Formats positive or negative integer cents into currency display string.
     */
    fun formatCents(cents: Long, currencyCode: String = "MYR", includeSign: Boolean = false): String {
        val symbol = getSymbol(currencyCode)
        val isNegative = cents < 0
        val absCents = kotlin.math.abs(cents)
        val dollars = absCents / 100
        val remainingCents = absCents % 100

        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
        numberFormat.applyPattern("#,##0")
        val formattedDollars = numberFormat.format(dollars)
        val formattedAmount = "$symbol $formattedDollars.${String.format(Locale.US, "%02d", remainingCents)}"

        return when {
            isNegative -> "-$formattedAmount"
            includeSign && cents > 0 -> "+$formattedAmount"
            else -> formattedAmount
        }
    }

    /**
     * Parses user string input into positive integer cents.
     * Rejects <= 0, empty, or invalid formatting.
     */
    fun parseAmountToCents(input: String): ParseResult {
        val trimmed = input.trim().replace(",", "")
        if (trimmed.isEmpty()) {
            return ParseResult.Error("Amount is required")
        }
        val doubleVal = trimmed.toDoubleOrNull()
            ?: return ParseResult.Error("Please enter a valid number")

        if (doubleVal <= 0.0) {
            return ParseResult.Error("Amount must be greater than zero")
        }

        // Convert to cents with precision rounding
        val cents = kotlin.math.round(doubleVal * 100).toLong()
        if (cents <= 0L) {
            return ParseResult.Error("Amount must be at least 0.01")
        }
        return ParseResult.Success(cents)
    }

    sealed class ParseResult {
        data class Success(val cents: Long) : ParseResult()
        data class Error(val message: String) : ParseResult()
    }
}

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String
)

object DateUtils {
    private val MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private val SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    private val DAY_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

    fun today(): LocalDate = LocalDate.now()

    fun epochDayToLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun localDateToEpochDay(date: LocalDate): Long = date.toEpochDay()

    fun formatMonthYear(date: LocalDate): String = date.format(MONTH_YEAR_FORMATTER)

    fun toMonthYearKey(date: LocalDate): String = String.format(Locale.US, "%04d-%02d", date.year, date.monthValue)

    fun formatShortDate(epochDay: Long): String {
        val date = epochDayToLocalDate(epochDay)
        val today = today()
        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            date == today.plusDays(1) -> "Tomorrow"
            date.year == today.year -> date.format(DAY_MONTH_FORMATTER)
            else -> date.format(SHORT_DATE_FORMATTER)
        }
    }

    fun formatFullDate(epochDay: Long): String {
        return epochDayToLocalDate(epochDay).format(SHORT_DATE_FORMATTER)
    }

    fun getWeekRange(anchorDate: LocalDate, isSundayStart: Boolean = true): Pair<LocalDate, LocalDate> {
        val dayOfWeek = anchorDate.dayOfWeek.value // 1 (Mon) - 7 (Sun)
        val daysBeforeStart = if (isSundayStart) {
            if (dayOfWeek == 7) 0 else dayOfWeek
        } else {
            dayOfWeek - 1
        }
        val start = anchorDate.minusDays(daysBeforeStart.toLong())
        val end = start.plusDays(6)
        return Pair(start, end)
    }

    fun getMonthRange(anchorDate: LocalDate): Pair<LocalDate, LocalDate> {
        val start = anchorDate.withDayOfMonth(1)
        val end = anchorDate.withDayOfMonth(anchorDate.lengthOfMonth())
        return Pair(start, end)
    }

    fun getYearRange(anchorDate: LocalDate): Pair<LocalDate, LocalDate> {
        val start = LocalDate.of(anchorDate.year, 1, 1)
        val end = LocalDate.of(anchorDate.year, 12, 31)
        return Pair(start, end)
    }

    fun formatDateRange(start: LocalDate, end: LocalDate): String {
        return if (start == end) {
            start.format(SHORT_DATE_FORMATTER)
        } else if (start.year == end.year) {
            "${start.format(DAY_MONTH_FORMATTER)} - ${end.format(SHORT_DATE_FORMATTER)}"
        } else {
            "${start.format(SHORT_DATE_FORMATTER)} - ${end.format(SHORT_DATE_FORMATTER)}"
        }
    }

    /**
     * Handles month-end clamp logic for recurring transactions.
     * E.g. Jan 31 + 1 month with anchor day 31 -> Feb 28 (or 29 in leap year).
     */
    fun calculateNextDueDate(
        currentDate: LocalDate,
        frequency: String,
        anchorDayOfMonth: Int
    ): LocalDate {
        return when (frequency.lowercase()) {
            "weekly" -> currentDate.plusWeeks(1)
            "yearly" -> {
                val nextYear = currentDate.plusYears(1)
                val clampedDay = minOf(anchorDayOfMonth, nextYear.lengthOfMonth())
                nextYear.withDayOfMonth(clampedDay)
            }
            "monthly" -> {
                val nextMonth = currentDate.plusMonths(1)
                val clampedDay = minOf(anchorDayOfMonth, nextMonth.lengthOfMonth())
                nextMonth.withDayOfMonth(clampedDay)
            }
            else -> currentDate.plusMonths(1)
        }
    }
}
