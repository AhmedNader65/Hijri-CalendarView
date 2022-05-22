package com.mrerror.calendarview.model

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

data class MyLocaleDate(val dayOfMonth: Int, var yearMonth: Calendar) {

    fun getNextMonthCalendar(): Calendar {
        var cal: Calendar

        if (yearMonth is UmmalquraCalendar) {

            cal = UmmalquraCalendar()
            cal.set(UmmalquraCalendar.DAY_OF_MONTH, yearMonth.get(UmmalquraCalendar.DAY_OF_MONTH))
            cal.set(UmmalquraCalendar.YEAR, yearMonth.get(UmmalquraCalendar.YEAR))
            cal.set(UmmalquraCalendar.MONTH, yearMonth.get(UmmalquraCalendar.MONTH))
            if (cal.get(UmmalquraCalendar.MONTH) + 1 > 11) {
                cal.set(UmmalquraCalendar.DAY_OF_MONTH, 1)
                cal.set(UmmalquraCalendar.YEAR, cal.get(UmmalquraCalendar.YEAR) + 1)
                val offset = (cal.get(UmmalquraCalendar.MONTH) + 1) % 11
                cal.set(UmmalquraCalendar.MONTH, offset)
            } else
                cal.set(UmmalquraCalendar.MONTH, cal.get(UmmalquraCalendar.MONTH) + 1)
        } else {
            cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, 1)
        }
        return cal
    }

    fun getPrevMonthCalendar(): Calendar {
        var cal: Calendar
        if (yearMonth is UmmalquraCalendar) {
            cal = UmmalquraCalendar()
            cal.set(UmmalquraCalendar.DAY_OF_MONTH, yearMonth.get(UmmalquraCalendar.DAY_OF_MONTH))
            cal.set(UmmalquraCalendar.YEAR, yearMonth.get(UmmalquraCalendar.YEAR))
            cal.set(UmmalquraCalendar.MONTH, yearMonth.get(UmmalquraCalendar.MONTH))
            if (cal.get(UmmalquraCalendar.MONTH) - 1 < 0) {
                cal.set(UmmalquraCalendar.DAY_OF_MONTH, 1)
                cal.set(UmmalquraCalendar.YEAR, cal.get(UmmalquraCalendar.YEAR) - 1)
                cal.set(UmmalquraCalendar.MONTH, 11)
            } else
                cal.set(UmmalquraCalendar.MONTH, cal.get(UmmalquraCalendar.MONTH) - 1)
        } else {
            cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -1)
        }
        return cal
    }
}
