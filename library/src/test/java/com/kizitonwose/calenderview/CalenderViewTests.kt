package com.kizitonwose.calenderview

import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.model.MonthConfig
import com.kizitonwose.calendarview.model.OutDateStyle
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.util.*

/**
 * These are core functionality tests.
 * The UI behaviour tests are in the sample project.
 */
class CalenderViewTests {

    // You can see what May and November 2019 with Monday as the first day of
    // week look like in the included May2019.png and November2019.png files.

    private lateinit var nov2019: Calendar
    private val firstDayOfWeek = DayOfWeek.MONDAY

    @Test
    fun `test all month in date generation works as expected`() {
        val may2019 = Calendar.getInstance()
        may2019.set(Calendar.MONTH, 4)
        may2019.set(Calendar.YEAR, 2019)
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_ROW)

        val validInDateIndices = (0..1)
        val inDatesInMonth = weekDays.flatten().filterIndexed { index, _ -> validInDateIndices.contains(index) }

        // inDates are in appropriate indices and have accurate count.
        assertTrue(inDatesInMonth.all { it.owner == DayOwner.PREVIOUS_MONTH })
    }

    @Test
    fun `test no in date generation works as expected`() {
        val may2019 = Calendar.getInstance()
        may2019.set(Calendar.MONTH, 4)
        may2019.set(Calendar.YEAR, 2019)
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, false, OutDateStyle.END_OF_ROW)
        assertTrue(weekDays.flatten().none { it.owner == DayOwner.PREVIOUS_MONTH })
    }

    @Test
    fun `test first month in date generation works as expected`() {
        val may2019 = Calendar.getInstance()
        may2019.set(Calendar.MONTH, 4)
        may2019.set(Calendar.YEAR, 2019)
        nov2019 = Calendar.getInstance()
        nov2019.set(Calendar.MONTH, 10)
        nov2019.set(Calendar.YEAR, 2019)
        val months = MonthConfig.generateBoundedMonths(
            may2019, nov2019, firstDayOfWeek, 6, InDateStyle.FIRST_MONTH, OutDateStyle.NONE
        )

        // inDates are in the first month.
        assertTrue(months.first().weekDays.flatten().any { it.owner == DayOwner.PREVIOUS_MONTH })

        // No inDates in other months.
        assertTrue(months.takeLast(months.size - 2).all { calendarMonth ->
            calendarMonth.weekDays.flatten().none { it.owner == DayOwner.PREVIOUS_MONTH }
        })
    }

    @Test
    fun `test end of row out date generation works as expected`() {
        val may2019 = Calendar.getInstance()
        may2019.set(Calendar.MONTH, 4)
        may2019.set(Calendar.YEAR, 2019)
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_ROW)

        val validOutDateIndices = weekDays.flatten().indices.toList().takeLast(1)
        val outDatesInMonth = weekDays.flatten().filterIndexed { index, _ -> validOutDateIndices.contains(index) }

        // outDates are in appropriate indices and have accurate count.
        assertTrue(outDatesInMonth.all { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test end of grid out date generation works as expected`() {
        val may2019 = Calendar.getInstance()
        may2019.set(Calendar.MONTH, 4)
        may2019.set(Calendar.YEAR, 2019)
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_GRID)

        val validOutDateIndices = weekDays.flatten().indices.toList().takeLast(8)
        val outDatesInMonth = weekDays.flatten().filterIndexed { index, _ -> validOutDateIndices.contains(index) }

        // outDates are in appropriate indices and have accurate count.
        assertTrue(outDatesInMonth.all { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test no out date generation works as expected`() {
        val may2019 = Calendar.getInstance()
        may2019.set(Calendar.MONTH, 4)
        may2019.set(Calendar.YEAR, 2019)
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.NONE)
        assertTrue(weekDays.flatten().none { it.owner == DayOwner.NEXT_MONTH } )
    }

    @Test
    fun `test max row count works with boundaries`() {
        val maxRowCount = 3
        val may2019 = Calendar.getInstance()
        may2019.set(Calendar.MONTH, 4)
        may2019.set(Calendar.YEAR, 2019)

        val end = Calendar.getInstance()
        end.set(Calendar.MONTH, 4)
        end.set(Calendar.YEAR, 2019)
        end.add(Calendar.MONTH, 20)
        val months = MonthConfig.generateBoundedMonths(
            may2019, end,
            firstDayOfWeek, maxRowCount, InDateStyle.ALL_MONTHS, OutDateStyle.END_OF_ROW
        )

        assertTrue(months.all { it.weekDays.count() <= maxRowCount })

        // With a bounded config, OutDateStyle of endOfRow and maxRowCount of 3,
        // there should be two CalendarMonth instances for may2019, the first
        // should have 3 weeks and the second should have 2 weeks.
        val mayCalendarMonths = months.filter { it.calendar.get(Calendar.MONTH)== may2019.get(Calendar.MONTH) && it.calendar.get(Calendar.YEAR)== may2019.get(Calendar.YEAR) }
        assertTrue(mayCalendarMonths.count() == 2)

        assertTrue(mayCalendarMonths.first().weekDays.count() == 3)
        assertTrue(mayCalendarMonths.last().weekDays.count() == 2)

        assertTrue(mayCalendarMonths.first().indexInSameMonth == 0)
        assertTrue(mayCalendarMonths.last().indexInSameMonth == 1)

        assertTrue(mayCalendarMonths.first().numberOfSameMonth == 2)
        assertTrue(mayCalendarMonths.last().numberOfSameMonth == 2)
    }

    @Test
    fun `test max row count works without boundaries`() {
        val maxRowCount = 3
        val start = Calendar.getInstance()
        start.set(Calendar.MONTH, 4)
        start.set(Calendar.YEAR, 2019)
        start.add(Calendar.MONTH, -40)
        val end = Calendar.getInstance()
        end.set(Calendar.MONTH, 4)
        end.set(Calendar.YEAR, 2019)
        end.add(Calendar.MONTH, 50)
        val months = MonthConfig.generateUnboundedMonths(
            start, end,
            firstDayOfWeek, maxRowCount, InDateStyle.ALL_MONTHS, OutDateStyle.END_OF_GRID
        )

        // The number of weeks in all CalendarMonth instances except the last one must match
        // maxRowCount if the calendar has no boundaries. The number of weeks in the last
        // month must also match maxRowCount if OutDateStyle is endOfGrid, otherwise, it will
        // be the length(1 - maxRowCount) of whatever number of weeks remaining after grouping
        // all weeks by maxRowCount value.
        assertTrue(months.all { it.weekDays.count() == maxRowCount })
    }

    @Test
    fun `test unbounded month generation does not exceed number of days in each month`() {
        val start = Calendar.getInstance()
        start.set(Calendar.MONTH, 2)
        start.set(Calendar.YEAR, 2019)
        val end = Calendar.getInstance()
        end.set(Calendar.MONTH, 2)
        end.set(Calendar.YEAR, 2021)
        val maxRowCount = 6
        MonthConfig.generateUnboundedMonths(
            start, end,
            DayOfWeek.SUNDAY, maxRowCount, InDateStyle.ALL_MONTHS, OutDateStyle.END_OF_GRID
        )
        // No assertion necessary, as this particular range would throw an exception previously
        // when trying to build a day that is out of bounds (eg: December 32).
    }
}