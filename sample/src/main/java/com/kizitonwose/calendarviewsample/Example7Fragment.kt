package com.kizitonwose.calendarviewsample

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.MyLocaleDate
import com.kizitonwose.calendarview.model.TYPE
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.Size
import com.kizitonwose.calendarview.utils.yearMonth
import com.kizitonwose.calendarviewsample.databinding.Example7CalendarDayBinding
import com.kizitonwose.calendarviewsample.databinding.Example7FragmentBinding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class Example7Fragment : BaseFragment(R.layout.example_7_fragment), HasToolbar, HasBackButton {

    override val titleRes: Int = R.string.example_7_title

    override val toolbar: Toolbar?
        get() = binding.exSevenToolbar

    private var selectedDate : MyLocaleDate? =null

    private val dateFormatter = DateTimeFormatter.ofPattern("dd")
    private val dayFormatter = DateTimeFormatter.ofPattern("EEE")
    private val monthFormatter = DateTimeFormatter.ofPattern("MMM")

    private lateinit var binding: Example7FragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = Example7FragmentBinding.bind(view)

        val dm = DisplayMetrics()
        val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(dm)
        binding.exSevenCalendar.apply {
            val dayWidth = dm.widthPixels / 5
            val dayHeight = (dayWidth * 1.25).toInt()
            daySize = Size(dayWidth, dayHeight)
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = Example7CalendarDayBinding.bind(view)
            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    val firstDay = binding.exSevenCalendar.findFirstVisibleDay()
                    val lastDay = binding.exSevenCalendar.findLastVisibleDay()
                    if (firstDay == day) {
                        // If the first date on screen was clicked, we scroll to the date to ensure
                        // it is fully visible if it was partially off the screen when clicked.
                        binding.exSevenCalendar.smoothScrollToDate(day.date)
                    } else if (lastDay == day) {
                        // If the last date was clicked, we scroll to 4 days ago, this forces the
                        // clicked date to be fully visible if it was partially off the screen.
                        // We scroll to 4 days ago because we show max of five days on the screen
                        // so scrolling to 4 days ago brings the clicked date into full visibility
                        // at the end of the calendar view.
                        val cal = day.date.yearMonth
                        cal.add(Calendar.DAY_OF_MONTH,4)
                        day.date.yearMonth = cal
                        binding.exSevenCalendar.smoothScrollToDate(day.date)
                    }

                    // Example: If you want the clicked date to always be centered on the screen,
                    // you would use: exSevenCalendar.smoothScrollToDate(day.date.minusDays(2))

                    if (selectedDate != day.date) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        binding.exSevenCalendar.notifyDateChanged(day.date)
                        oldDate?.let { binding.exSevenCalendar.notifyDateChanged(it) }
                    }
                }
            }

            fun bind(day: CalendarDay) {
                val ar = Locale("ar")
                this.day = day
                bind.exSevenDateText.text = "${day.date.yearMonth.get(Calendar.DAY_OF_MONTH)}"
                bind.exSevenDayText.text = "${day.date.yearMonth.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, ar)}"
                bind.exSevenMonthText.text = "${day.date.yearMonth.getDisplayName(Calendar.MONTH, Calendar.LONG, ar)}"

                bind.exSevenDateText.setTextColor(view.context.getColorCompat(if (day.date.yearMonth.time == selectedDate?.yearMonth?.time) R.color.example_7_yellow else R.color.example_7_white))
                bind.exSevenSelectedView.isVisible = day.date == selectedDate
            }
        }

        binding.exSevenCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) = container.bind(day)
        }

        val currentMonth = YearMonth.now()
        // Value for firstDayOfWeek does not matter since inDates and outDates are not generated.
        binding.exSevenCalendar.setup(0, 3, DayOfWeek.values().random(),TYPE.HIJRI)
        val cal = UmmalquraCalendar()
        binding.exSevenCalendar.scrollToDate(MyLocaleDate(cal.get(UmmalquraCalendar.DAY_OF_MONTH),cal) )
    }
}
