package com.maddy.calendar.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.google.android.material.snackbar.Snackbar
import com.maddy.calendar.core.Formatter
import com.maddy.calendar.core.ILocalDate
import com.maddy.calendar.sample.databinding.Example2CalendarDayBinding
import com.maddy.calendar.sample.databinding.Example2CalendarHeaderBinding
import com.maddy.calendar.sample.databinding.Example2FragmentBinding
import com.maddy.calendar.ui.model.CalendarDay
import com.maddy.calendar.ui.model.CalendarMonth
import com.maddy.calendar.ui.model.DayOwner
import com.maddy.calendar.ui.view.DayBinder
import com.maddy.calendar.ui.view.MonthHeaderFooterBinder
import com.maddy.calendar.ui.view.ViewContainer

class Example2Fragment : BaseFragment(R.layout.example_2_fragment), HasToolbar, HasBackButton {

    override val toolbar: Toolbar
        get() = binding.exTwoToolbar

    override val titleRes: Int = R.string.example_2_title

    private lateinit var binding: Example2FragmentBinding

    private var selectedDate: ILocalDate? = null
    private val today = ILocalDate.nowBS()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding = Example2FragmentBinding.bind(view)
        val daysOfWeek = daysOfWeekFromLocale()
        binding.legendLayout.root.children.forEachIndexed { index, headerItemView ->
            (headerItemView as TextView).apply {
                text = daysOfWeek[index].name(today.type).first().toString()
                setTextColorRes(R.color.example_2_white)
            }
        }

        binding.exTwoCalendar.setup(
            ILocalDate.now(today.type),
            ILocalDate.now(today.type).plusMonths(10),
            daysOfWeek.first()
        )

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = Example2CalendarDayBinding.bind(view).exTwoDayText

            init {
                textView.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDate == day.date) {
                            selectedDate = null
                            binding.exTwoCalendar.notifyDayChanged(day)
                        } else {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            binding.exTwoCalendar.notifyDateChanged(day.date)
                            oldDate?.let { binding.exTwoCalendar.notifyDateChanged(oldDate) }
                        }
                        menuItem.isVisible = selectedDate != null
                    }
                }
            }
        }

        binding.exTwoCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = Formatter.format(day.date, "dd")

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()
                    when (day.date) {
                        selectedDate -> {
                            textView.setTextColorRes(R.color.example_2_white)
                            textView.setBackgroundResource(R.drawable.example_2_selected_bg)
                        }
                        today -> {
                            textView.setTextColorRes(R.color.example_2_red)
                            textView.background = null
                        }
                        else -> {
                            textView.setTextColorRes(R.color.example_2_black)
                            textView.background = null
                        }
                    }
                } else {
                    textView.makeInVisible()
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = Example2CalendarHeaderBinding.bind(view).exTwoHeaderText
        }
        binding.exTwoCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    @SuppressLint("SetTextI18n") // Concatenation warning for `setText` call.
                    container.textView.text = Formatter.format(month.yearMonth, "MMM yyyy")
                }
            }
    }

    private lateinit var menuItem: MenuItem
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.example_2_menu, menu)
        menuItem = menu.getItem(0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuItemDone) {
            val date = selectedDate ?: return false
            val text = "Selected: ${Formatter.format(date, "d MMMM yyyy")}"
            Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
