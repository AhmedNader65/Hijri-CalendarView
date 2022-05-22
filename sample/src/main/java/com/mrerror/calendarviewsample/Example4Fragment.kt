package com.mrerror.calendarviewsample

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.google.android.material.snackbar.Snackbar
import com.mrerror.calendarview.model.*
import com.mrerror.calendarview.ui.DayBinder
import com.mrerror.calendarview.ui.MonthHeaderFooterBinder
import com.mrerror.calendarview.ui.ViewContainer
import com.mrerror.calendarviewsample.databinding.Example4CalendarDayBinding
import com.mrerror.calendarviewsample.databinding.Example4CalendarHeaderBinding
import com.mrerror.calendarviewsample.databinding.Example4FragmentBinding
import java.time.format.TextStyle
import java.util.*

class Example4Fragment : BaseFragment(R.layout.example_4_fragment), HasToolbar, HasBackButton {

    override val toolbar: Toolbar?
        get() = binding.exFourToolbar
    override val titleRes: Int? = null
    private var startDate: MyLocaleDate? = null
    private var endDate: MyLocaleDate? = null

    private val startBackground: GradientDrawable by lazy {
        requireContext().getDrawableCompat(R.drawable.example_4_continuous_selected_bg_start) as GradientDrawable
    }

    private val endBackground: GradientDrawable by lazy {
        requireContext().getDrawableCompat(R.drawable.example_4_continuous_selected_bg_end) as GradientDrawable
    }

    private lateinit var binding: Example4FragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding = Example4FragmentBinding.bind(view)
        // We set the radius of the continuous selection background drawable dynamically
        // since the view size is `match parent` hence we cannot determine the appropriate
        // radius value which would equal half of the view's size beforehand.
        binding.exFourCalendar.post {
            val radius = ((binding.exFourCalendar.width / 7) / 2).toFloat()
            startBackground.setCornerRadius(topLeft = radius, bottomLeft = radius)
            endBackground.setCornerRadius(topRight = radius, bottomRight = radius)
        }

        // Set the First day of week depending on Locale
        val daysOfWeek = daysOfWeekFromLocale()
        binding.legendLayout.root.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColorRes(R.color.example_4_grey)
            }
        }

        val currentMonth = UmmalquraCalendar()
        binding.exFourCalendar.setup(10, 10, daysOfWeek.first(), TYPE.HIJRI)
        binding.exFourCalendar.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = Example4CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH && (DateUtils.isToday(day.date.yearMonth.timeInMillis) || day.date.yearMonth.timeInMillis > System.currentTimeMillis())) {
                        val date = day.date
                        if (startDate != null) {
                            if (date.yearMonth.timeInMillis < startDate!!.yearMonth.timeInMillis || endDate != null) {
                                startDate = date
                                endDate = null
                            } else if (date != startDate) {
                                endDate = date
                            }
                        } else {
                            startDate = date
                        }
                        this@Example4Fragment.binding.exFourCalendar.notifyCalendarChanged()
                        bindSummaryViews()
                    }
                }
            }
        }

        binding.exFourCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.exFourDayText
                val roundBgView = container.binding.exFourRoundBgView

                textView.text = null
                textView.background = null
                roundBgView.makeInVisible()

                val startDate = startDate
                val endDate = endDate

                when (day.owner) {
                    DayOwner.THIS_MONTH -> {
                        textView.text = day.day.toString()
                        if (day.date.yearMonth.timeInMillis < System.currentTimeMillis()) {
                            textView.setTextColorRes(R.color.example_4_grey_past)
                        } else {
                            when {
                                startDate == day.date && endDate == null -> {
                                    textView.setTextColorRes(R.color.white)
                                    roundBgView.makeVisible()
                                    roundBgView.setBackgroundResource(R.drawable.example_4_single_selected_bg)
                                }
                                day.date == startDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    textView.background = startBackground
                                }
                                startDate != null && endDate != null && (day.date.yearMonth.timeInMillis > startDate.yearMonth.timeInMillis && day.date.yearMonth.timeInMillis < endDate.yearMonth.timeInMillis) -> {
                                    textView.setTextColorRes(R.color.white)
                                    textView.setBackgroundResource(R.drawable.example_4_continuous_selected_bg_middle)
                                }
                                day.date == endDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    textView.background = endBackground
                                }
                                DateUtils.isToday(day.date.yearMonth.timeInMillis) -> {
                                    textView.setTextColorRes(R.color.example_4_grey)
                                    roundBgView.makeVisible()
                                    roundBgView.setBackgroundResource(R.drawable.example_4_today_bg)
                                }
                                else -> textView.setTextColorRes(R.color.example_4_grey)
                            }
                        }
                    }
                    // Make the coloured selection background continuous on the invisible in and out dates across various months.
                    DayOwner.PREVIOUS_MONTH ->
                        if (startDate != null && endDate != null && isInDateBetween(day.date, startDate, endDate)) {
                            textView.setBackgroundResource(R.drawable.example_4_continuous_selected_bg_middle)
                        }
                    DayOwner.NEXT_MONTH ->
                        if (startDate != null && endDate != null && isOutDateBetween(day.date, startDate, endDate)) {
                            textView.setBackgroundResource(R.drawable.example_4_continuous_selected_bg_middle)
                        }
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = Example4CalendarHeaderBinding.bind(view).exFourHeaderText
        }
        binding.exFourCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                val ar = Locale("ar")
                val title = month.calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, ar)
                val monthTitle = "$title ${month.year}"
                container.textView.text = monthTitle
            }
        }

        binding.exFourSaveButton.setOnClickListener click@{
            val startDate = startDate
            val endDate = endDate
            if (startDate != null && endDate != null) {
                val ar = Locale("ar")
                val text = "Selected: ${startDate.yearMonth.get(Calendar.YEAR)} ${
                    startDate.yearMonth.getDisplayName(
                        Calendar.MONTH,
                        Calendar.LONG,
                        ar
                    )
                } ${startDate.yearMonth.get(Calendar.DAY_OF_MONTH)} " +
                        "- ${endDate.yearMonth.get(Calendar.YEAR)} ${
                            endDate.yearMonth.getDisplayName(
                                Calendar.MONTH,
                                Calendar.LONG,
                                ar
                            )
                        } ${endDate.yearMonth.get(Calendar.DAY_OF_MONTH)} "

                Snackbar.make(requireView(), text, Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(requireView(), "No selection. Searching all Airbnb listings.", Snackbar.LENGTH_LONG)
                    .show()
            }
            fragmentManager?.popBackStack()
        }

        bindSummaryViews()
    }

    private fun isInDateBetween(inDate: MyLocaleDate, startDate: MyLocaleDate, endDate: MyLocaleDate): Boolean {
        if (startDate.yearMonth.timeInMillis == endDate.yearMonth.timeInMillis) return false
        if (inDate.yearMonth.timeInMillis == startDate.yearMonth.timeInMillis) return true
        val firstDateInThisMonth = inDate.getNextMonthCalendar()
        firstDateInThisMonth.set(Calendar.DAY_OF_MONTH, 1)
        return firstDateInThisMonth.timeInMillis >= startDate.yearMonth.timeInMillis && firstDateInThisMonth.timeInMillis <= endDate.yearMonth.timeInMillis && startDate.yearMonth.timeInMillis != firstDateInThisMonth.timeInMillis
    }

    private fun isOutDateBetween(outDate: MyLocaleDate, startDate: MyLocaleDate, endDate: MyLocaleDate): Boolean {
        if (startDate.yearMonth.timeInMillis == endDate.yearMonth.timeInMillis) return false
        if (outDate.yearMonth.timeInMillis == endDate.yearMonth.timeInMillis) return true
        val lastDateInThisMonth = outDate.getPrevMonthCalendar()
        lastDateInThisMonth.set(Calendar.DAY_OF_MONTH, 1)
        return lastDateInThisMonth.timeInMillis >= startDate.yearMonth.timeInMillis && lastDateInThisMonth.timeInMillis <= endDate.yearMonth.timeInMillis && endDate.yearMonth.timeInMillis != lastDateInThisMonth.timeInMillis
    }

    private fun bindSummaryViews() {
        binding.exFourStartDateText.apply {
            if (startDate != null) {
                val ar = Locale("ar")
                text = "${startDate!!.yearMonth.get(Calendar.YEAR)} ${
                    startDate!!.yearMonth.getDisplayName(
                        Calendar.MONTH,
                        Calendar.LONG,
                        ar
                    )
                } ${startDate!!.yearMonth.get(Calendar.DAY_OF_MONTH)} "
                setTextColorRes(R.color.example_4_grey)
            } else {
                text = getString(R.string.start_date)
                setTextColor(Color.GRAY)
            }
        }

        binding.exFourEndDateText.apply {
            if (endDate != null) {
                var ar = Locale("ar")
                text = "${endDate!!.yearMonth.get(Calendar.YEAR)} ${
                    endDate!!.yearMonth.getDisplayName(
                        Calendar.MONTH,
                        Calendar.LONG,
                        ar
                    )
                } ${endDate!!.yearMonth.get(Calendar.DAY_OF_MONTH)} "
                setTextColorRes(R.color.example_4_grey)
            } else {
                text = getString(R.string.end_date)
                setTextColor(Color.GRAY)
            }
        }

        // Enable save button if a range is selected or no date is selected at all, Airbnb style.
        binding.exFourSaveButton.isEnabled = endDate != null || (startDate == null && endDate == null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.example_4_menu, menu)
        binding.exFourToolbar.post {
            // Configure menu text to match what is in the Airbnb app.
            binding.exFourToolbar.findViewById<TextView>(R.id.menuItemClear).apply {
                setTextColor(requireContext().getColorCompat(R.color.example_4_grey))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                isAllCaps = false
            }
        }
        menu.findItem(R.id.menuItemClear).setOnMenuItemClickListener {
            startDate = null
            endDate = null
            binding.exFourCalendar.notifyCalendarChanged()
            bindSummaryViews()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        val closeIndicator = requireContext().getDrawableCompat(R.drawable.ic_close)?.apply {
            setColorFilter(requireContext().getColorCompat(R.color.example_4_grey), PorterDuff.Mode.SRC_ATOP)
        }
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(closeIndicator)
        requireActivity().window.apply {
            // Update statusbar color to match toolbar color.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                statusBarColor = requireContext().getColorCompat(R.color.white)
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                statusBarColor = Color.GRAY
            }
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.apply {
            // Reset statusbar color.
            statusBarColor = requireContext().getColorCompat(R.color.colorPrimaryDark)
            decorView.systemUiVisibility = 0
        }
    }
}
