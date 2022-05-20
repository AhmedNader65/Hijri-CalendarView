package com.kizitonwose.calendarview.model

import java.io.Serializable
import java.util.*

data class CalendarDay internal constructor(val date: MyLocaleDate, val owner: DayOwner, val weekOfYear: Int = 0) :
    Comparable<CalendarDay>, Serializable {

    val day = date.dayOfMonth

    // Find the actual month on the calendar that owns this date.
    internal val positionYearMonth: Calendar
        get() = when (owner) {
            DayOwner.THIS_MONTH -> date.yearMonth
            DayOwner.PREVIOUS_MONTH -> date.getPrevMonthCalendar()
            DayOwner.NEXT_MONTH -> date.getNextMonthCalendar()
        }

    override fun toString(): String {
        return "CalendarDay { date =  $date, owner = $owner}"
    }

    fun getWeekOfTheYear(): Int {
        return weekOfYear
    }

    override fun compareTo(other: CalendarDay): Int {
        throw UnsupportedOperationException(
            "Compare using the `date` parameter instead. " +
                    "Out and In dates can have the same date values as CalendarDay in another month."
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CalendarDay
        return date == other.date && owner == other.owner
    }

    override fun hashCode(): Int {
        return 31 * (date.hashCode() + owner.hashCode())
    }
}
