package com.example

import com.example.ui.util.CurrencyUtils
import com.example.ui.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {
    @Test
    fun testCurrencyFormatting() {
        val formattedMYR = CurrencyUtils.formatCents(1250, "MYR")
        assertEquals("RM 12.50", formattedMYR)

        val formattedUSD = CurrencyUtils.formatCents(50000, "USD")
        assertEquals("$ 500.00", formattedUSD)
    }

    @Test
    fun testAmountParsing() {
        val res1 = CurrencyUtils.parseAmountToCents("12.50")
        assertTrue(res1 is CurrencyUtils.ParseResult.Success)
        assertEquals(1250L, (res1 as CurrencyUtils.ParseResult.Success).cents)

        val resZero = CurrencyUtils.parseAmountToCents("0")
        assertTrue(resZero is CurrencyUtils.ParseResult.Error)

        val resNegative = CurrencyUtils.parseAmountToCents("-5.00")
        assertTrue(resNegative is CurrencyUtils.ParseResult.Error)
    }

    @Test
    fun testMonthEndClampingEdgeCase() {
        // Critical edge case from specification:
        // Monthly recurring on Jan 31 -> next due Feb 28/29 (month-end clamp)
        val jan31 = LocalDate.of(2026, 1, 31)
        val nextDueFeb = DateUtils.calculateNextDueDate(
            currentDate = jan31,
            frequency = "monthly",
            anchorDayOfMonth = 31
        )
        // 2026 is non-leap year, Feb has 28 days
        assertEquals(LocalDate.of(2026, 2, 28), nextDueFeb)

        // Then from Feb 28, advancing with anchor 31 should return March 31
        val nextDueMar = DateUtils.calculateNextDueDate(
            currentDate = nextDueFeb,
            frequency = "monthly",
            anchorDayOfMonth = 31
        )
        assertEquals(LocalDate.of(2026, 3, 31), nextDueMar)
    }
}
