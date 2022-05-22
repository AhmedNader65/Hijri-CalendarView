package com.kizitonwose.calendarviewsample

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import com.kizitonwose.calendarview.model.*
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarviewsample.databinding.Example1CalendarDayBinding
import com.kizitonwose.calendarviewsample.databinding.Example1FragmentBinding
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class Example1Fragment : BaseFragment(R.layout.example_1_fragment), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_1_title
0
    private lateinit var binding: Example1FragmentBinding

    private val selectedDates = mutableSetOf<MyLocaleDate>()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = Example1FragmentBinding.bind(view)
        val daysOfWeek = daysOfWeekFromLocale()
        binding.legendLayout.root.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ENGLISH)
                setTextColorRes(R.color.example_1_white_light)
            }
        }

        val currentMonth = Calendar.getInstance()
        val endMonth = Calendar.getInstance()
        endMonth.add(Calendar.MONTH, 5)
        binding.exOneCalendar.setup(0, 5, daysOfWeek.first(), TYPE.GREGORIAN)
        binding.exOneCalendar.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = Example1CalendarDayBinding.bind(view).exOneDayText

            init {
                view.setOnClickListener {
                    Log.e("clicked day","${day.date.yearMonth.get(Calendar.YEAR)} - ${day.date.yearMonth.get(Calendar.MONTH)} - ${day.date.yearMonth.get(Calendar.DAY_OF_MONTH)} ")
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDates.contains(day.date)) {
                            selectedDates.remove(day.date)
                        } else {
                            selectedDates.add(day.date)
                        }
                        binding.exOneCalendar.notifyDayChanged(day)
                    }
                }
            }
        }

        binding.exOneCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    when {
                        selectedDates.contains(day.date) -> {
                            textView.setTextColorRes(R.color.example_1_bg)
                            textView.setBackgroundResource(R.drawable.example_1_selected_bg)
                        }
                        DateUtils.isToday(day.date.yearMonth.timeInMillis) -> {
                            textView.setTextColorRes(R.color.example_1_white)
                            textView.setBackgroundResource(R.drawable.example_1_today_bg)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.example_1_white)
                            textView.background = null
                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.example_1_white_light)
                    textView.background = null
                }
            }
        }

        binding.exOneCalendar.monthScrollListener = {
            if (binding.exOneCalendar.maxRowCount == 6) {
                binding.exOneYearText.text = it.calendar.get(Calendar.YEAR).toString()
                binding.exOneMonthText.text = (it.calendar.getDisplayName(
                    Calendar.MONTH,
                    Calendar.SHORT,
                    Locale("ar")
                ))
            } else {
                // In week mode, we show the header a bit differently.
                // We show indices with dates from different months since
                // dates overflow and cells in one index can belong to different
                // months/years.
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    binding.exOneYearText.text = firstDate.yearMonth.get(Calendar.YEAR).toString()
                    binding.exOneMonthText.text = firstDate.yearMonth.getDisplayName(Calendar.MONTH, Calendar.LONG,Locale("ar"))
                } else {
                    val nextMonth = Calendar.getInstance()
                    nextMonth.set(Calendar.MONTH, it.month)
                    binding.exOneMonthText.text = "${
                        (nextMonth.getDisplayName(
                            Calendar.MONTH,
                            Calendar.SHORT,
                            Locale("ar")
                        ))
                    } - ${
                        (it.calendar.getDisplayName(
                            Calendar.MONTH,
                            Calendar.SHORT,
                            Locale("ar")
                        ))
                    }"
                    if (firstDate.yearMonth.get(Calendar.YEAR).toString() == lastDate.yearMonth.get(Calendar.YEAR).toString()) {
                        binding.exOneYearText.text = firstDate.yearMonth.get(Calendar.YEAR).toString()
                    } else {
                        binding.exOneYearText.text = "${firstDate.yearMonth.get(Calendar.YEAR)} - ${lastDate.yearMonth.get(Calendar.YEAR)}"
                    }
                }
            }
        }

        binding.weekModeCheckBox.setOnCheckedChangeListener { _, monthToWeek ->
            val firstDate = binding.exOneCalendar.findFirstVisibleDay()?.date ?: return@setOnCheckedChangeListener
            val lastDate = binding.exOneCalendar.findLastVisibleDay()?.date ?: return@setOnCheckedChangeListener

            val oneWeekHeight = binding.exOneCalendar.daySize.height
            val oneMonthHeight = oneWeekHeight * 6

            val oldHeight = if (monthToWeek) oneMonthHeight else oneWeekHeight
            val newHeight = if (monthToWeek) oneWeekHeight else oneMonthHeight

            // Animate calendar height changes.
            val animator = ValueAnimator.ofInt(oldHeight, newHeight)
            animator.addUpdateListener { animator ->
                binding.exOneCalendar.updateLayoutParams {
                    height = animator.animatedValue as Int
                }
            }

            // When changing from month to week mode, we change the calendar's
            // config at the end of the animation(doOnEnd) but when changing
            // from week to month mode, we change the calendar's config at
            // the start of the animation(doOnStart). This is so that the change
            // in height is visible. You can do this whichever way you prefer.

            animator.doOnStart {
                if (!monthToWeek) {
                    binding.exOneCalendar.updateMonthConfiguration(
                        inDateStyle = InDateStyle.ALL_MONTHS,
                        maxRowCount = 6,
                        hasBoundaries = true
                    )
                }
            }
            animator.doOnEnd {
                if (monthToWeek) {
                    binding.exOneCalendar.updateMonthConfiguration(
                        inDateStyle = InDateStyle.FIRST_MONTH,
                        maxRowCount = 1,
                        hasBoundaries = false
                    )
                }

                if (monthToWeek) {
                    // We want the first visible day to remain
                    // visible when we change to week mode.
                    binding.exOneCalendar.scrollToDate(firstDate)
                } else {
                    // When changing to month mode, we choose current
                    // month if it is the only one in the current frame.
                    // if we have multiple months in one frame, we prefer
                    // the second one unless it's an outDate in the last index.
                    if (firstDate.yearMonth == lastDate.yearMonth) {
                        binding.exOneCalendar.scrollToMonth(firstDate.yearMonth)
                    } else {
                        // We compare the next with the last month on the calendar so we don't go over.

                        // We compare the next with the last month on the calendar so we don't go over.
                        if (firstDate.yearMonth.time < endMonth.time)
                            binding.exOneCalendar.scrollToMonth(firstDate.yearMonth)
                        else
                            binding.exOneCalendar.scrollToMonth(endMonth)
                    }
                }
            }
            animator.duration = 250
            animator.start()
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.example_1_bg_light)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.statusBarColor = requireContext().getColorCompat(R.color.colorPrimaryDark)
    }
}
