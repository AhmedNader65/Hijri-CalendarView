package com.kizitonwose.calendarview.model

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

data class MyLocaleDate(val dayOfMonth: Int, val yearMonth: Calendar) {

    fun getNextMonthCalendar(): Calendar {
        var cal: Calendar
        if (yearMonth is UmmalquraCalendar) {
            cal = yearMonth.clone() as UmmalquraCalendar
            if (cal.get(UmmalquraCalendar.MONTH) + 1 > 11) {
                cal.set(UmmalquraCalendar.YEAR, cal.get(UmmalquraCalendar.YEAR) + 1)
                val offset = (cal.get(UmmalquraCalendar.MONTH) + 1) % 11
                cal.set(UmmalquraCalendar.MONTH, offset)
            } else
                cal.set(UmmalquraCalendar.MONTH, cal.get(UmmalquraCalendar.MONTH) + 1)
        } else {
            cal = yearMonth.clone() as UmmalquraCalendar
            cal.add(Calendar.MONTH, 1)
        }
        return cal
    }

    fun plusDay(day:Int):MyLocaleDate {
        yearMonth.add(Calendar.DAY_OF_MONTH,day)
        return MyLocaleDate(dayOfMonth,yearMonth)
    }

    fun getPrevMonthCalendar(): Calendar {
        var cal: Calendar
        if (yearMonth is UmmalquraCalendar) {
            cal = yearMonth.clone() as UmmalquraCalendar
            if (cal.get(UmmalquraCalendar.MONTH) - 1 < 0) {
                cal.set(UmmalquraCalendar.YEAR, cal.get(UmmalquraCalendar.YEAR) - 1)
                cal.set(UmmalquraCalendar.MONTH, 11)
            } else
                cal.set(UmmalquraCalendar.MONTH, cal.get(UmmalquraCalendar.MONTH) - 1)
        } else {
            cal = yearMonth.clone() as UmmalquraCalendar
            cal.add(Calendar.MONTH, -1)
        }
        return cal
    }
}