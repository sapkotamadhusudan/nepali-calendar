package com.maddy.calendar.sample

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import com.maddy.calendar.core.Formatter
import com.maddy.calendar.core.ILocalDate
import com.maddy.calendar.sample.databinding.Example1CalendarDayBinding
import com.maddy.calendar.sample.databinding.Example1FragmentBinding
import com.maddy.calendar.ui.model.CalendarDay
import com.maddy.calendar.ui.model.DayOwner
import com.maddy.calendar.ui.model.InDateStyle
import com.maddy.calendar.ui.model.compareToYearMonth
import com.maddy.calendar.ui.view.DayBinder
import com.maddy.calendar.ui.view.ViewContainer
import com.maddy.calendar.ui.utils.next
import com.maddy.calendar.ui.utils.yearMonth
import java.util.*

class Example1Fragment : BaseFragment(R.layout.example_1_fragment), HasToolbar {

    override val toolbar: Toolbar?
        get() = null

    override val titleRes: Int = R.string.example_1_title

    private lateinit var binding: Example1FragmentBinding

    private val selectedDates = mutableSetOf<ILocalDate>()
    private val today = ILocalDate.nowBS()
    private val monthTitleFormatter = "MMMM"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = Example1FragmentBinding.bind(view)
        val daysOfWeek = daysOfWeekFromLocale()
        binding.legendLayout.root.children.forEachIndexed { index, itemView ->
            (itemView as TextView).apply {
                text = daysOfWeek[index].name(today.type, true)
                setTextColorRes(R.color.example_1_white_light)
            }
        }

        val currentMonth = ILocalDate.now(today.type)
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)
        binding.exOneCalendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.exOneCalendar.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = Example1CalendarDayBinding.bind(view).exOneDayText

            init {
                view.setOnClickListener {
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
                textView.text = Formatter.format(day.date, "dd")
                if (day.owner == DayOwner.THIS_MONTH) {
                    when {
                        selectedDates.contains(day.date) -> {
                            textView.setTextColorRes(R.color.example_1_bg)
                            textView.setBackgroundResource(R.drawable.example_1_selected_bg)
                        }
                        today == day.date -> {
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
                binding.exOneYearText.text = Formatter.format(it.yearMonth, "yyyy")
                binding.exOneMonthText.text = Formatter.format(it.yearMonth, monthTitleFormatter)
            } else {
                // In week mode, we show the header a bit differently.
                // We show indices with dates from different months since
                // dates overflow and cells in one index can belong to different
                // months/years.
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    binding.exOneYearText.text = Formatter.format(firstDate.yearMonth, "yyyy")
                    binding.exOneMonthText.text = Formatter.format(firstDate, monthTitleFormatter)
                } else {
                    binding.exOneMonthText.text =
                        "${Formatter.format(firstDate, monthTitleFormatter)} - ${Formatter.format(lastDate, monthTitleFormatter)}"
                    if (firstDate.year == lastDate.year) {
                        binding.exOneYearText.text = Formatter.format(firstDate.yearMonth, "yyyy")
                    } else {
                        binding.exOneYearText.text = "${Formatter.format(firstDate.yearMonth, "yyyy")} - ${Formatter.format(lastDate.yearMonth, "yyyy")}"
                    }
                }
            }
        }

        binding.weekModeCheckBox.setOnCheckedChangeListener { _, monthToWeek ->
            val firstDate = binding.exOneCalendar.findFirstVisibleDay()?.date
                ?: return@setOnCheckedChangeListener
            val lastDate = binding.exOneCalendar.findLastVisibleDay()?.date
                ?: return@setOnCheckedChangeListener

            val oneWeekHeight = binding.exOneCalendar.daySize.height
            val oneMonthHeight = oneWeekHeight * 6

            val oldHeight = if (monthToWeek) oneMonthHeight else oneWeekHeight
            val newHeight = if (monthToWeek) oneWeekHeight else oneMonthHeight

            // Animate calendar height changes.
            val animator = ValueAnimator.ofInt(oldHeight, newHeight)
            animator.addUpdateListener { animatorItem ->
                binding.exOneCalendar.updateLayoutParams {
                    height = animatorItem.animatedValue as Int
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
                        binding.exOneCalendar.scrollToMonth(
                            minOf(
                                firstDate.yearMonth.next,
                                endMonth
                            )
                        )
                    }
                }
            }
            animator.duration = 250
            animator.start()
        }
    }

    private fun minOf(one: ILocalDate, another: ILocalDate): ILocalDate {
        return if (one.compareToYearMonth(another) < 0) one
        else another
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.statusBarColor =
                requireContext().getColorCompat(R.color.example_1_bg_light)
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.statusBarColor =
                requireContext().getColorCompat(R.color.purple_700)
        }
    }
}
