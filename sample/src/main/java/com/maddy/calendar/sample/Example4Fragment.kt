package com.maddy.calendar.sample

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.google.android.material.snackbar.Snackbar
import com.maddy.calendar.core.Formatter
import com.maddy.calendar.core.ILocalDate
import com.maddy.calendar.sample.databinding.Example4CalendarDayBinding
import com.maddy.calendar.sample.databinding.Example4CalendarHeaderBinding
import com.maddy.calendar.sample.databinding.Example4FragmentBinding
import com.maddy.calendar.ui.model.CalendarDay
import com.maddy.calendar.ui.model.CalendarMonth
import com.maddy.calendar.ui.model.DayOwner
import com.maddy.calendar.ui.model.compareToYearMonth
import com.maddy.calendar.ui.view.DayBinder
import com.maddy.calendar.ui.view.MonthHeaderFooterBinder
import com.maddy.calendar.ui.view.ViewContainer
import com.maddy.calendar.ui.utils.yearMonth

class Example4Fragment : BaseFragment(R.layout.example_4_fragment), HasToolbar, HasBackButton {

    override val toolbar: Toolbar
        get() = binding.exFourToolbar

    override val titleRes: Int? = null

    private val today = ILocalDate.nowBS()

    private var startDate: ILocalDate? = null
    private var endDate: ILocalDate? = null

    private val headerDateFormatter = "EEE\nd MMM"

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
        binding.legendLayout.root.children.forEachIndexed { index, legendItemView ->
            (legendItemView as TextView).apply {
                text = daysOfWeek[index].name(today.type, true)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColorRes(R.color.example_4_grey)
            }
        }

        val currentMonth = ILocalDate.now(today.type)
        binding.exFourCalendar.setup(currentMonth, currentMonth.plusMonths(12), daysOfWeek.first())
        binding.exFourCalendar.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = Example4CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH && (day.date == today || day.date.after(
                            today
                        ))
                    ) {
                        val date = day.date
                        if (startDate != null) {
                            if (date < startDate!! || endDate != null) {
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
                        textView.text = Formatter.format(day.date, "dd")
                        if (day.date.before(today)) {
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
                                startDate != null && endDate != null && (day.date > startDate && day.date < endDate) -> {
                                    textView.setTextColorRes(R.color.white)
                                    textView.setBackgroundResource(R.drawable.example_4_continuous_selected_bg_middle)
                                }
                                day.date == endDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    textView.background = endBackground
                                }
                                day.date == today -> {
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
                        if (startDate != null && endDate != null && isInDateBetween(
                                day.date,
                                startDate,
                                endDate
                            )
                        ) {
                            textView.setBackgroundResource(R.drawable.example_4_continuous_selected_bg_middle)
                        }
                    DayOwner.NEXT_MONTH ->
                        if (startDate != null && endDate != null && isOutDateBetween(
                                day.date,
                                startDate,
                                endDate
                            )
                        ) {
                            textView.setBackgroundResource(R.drawable.example_4_continuous_selected_bg_middle)
                        }
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = Example4CalendarHeaderBinding.bind(view).exFourHeaderText
        }
        binding.exFourCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    val monthTitle = Formatter.format(month.yearMonth, "MMM yyyy")
                    container.textView.text = monthTitle
                }
            }

        binding.exFourSaveButton.setOnClickListener click@{
            val startDate = startDate
            val endDate = endDate
            if (startDate != null && endDate != null) {
                val formatter = "d MMMM yyyy"
                val text = "Selected: ${Formatter.format(startDate, formatter)} - ${
                    Formatter.format(
                        endDate,
                        formatter
                    )
                }"
                Snackbar.make(requireView(), text, Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(
                    requireView(),
                    "No selection. Searching all Airbnb listings.",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
            parentFragmentManager.popBackStack()
        }

        bindSummaryViews()
    }

    private fun isInDateBetween(
        inDate: ILocalDate,
        startDate: ILocalDate,
        endDate: ILocalDate
    ): Boolean {
        if (startDate.yearMonth.compareToYearMonth(endDate.yearMonth) == 0) return false
        if (inDate.yearMonth.compareToYearMonth(startDate.yearMonth) == 0) return true
        if (inDate.yearMonth.plusMonths(1).compareToYearMonth(endDate.yearMonth) == 0) return true
        return inDate > startDate && inDate < endDate
    }

    private fun isOutDateBetween(
        outDate: ILocalDate,
        startDate: ILocalDate,
        endDate: ILocalDate
    ): Boolean {
        if (startDate.yearMonth.compareToYearMonth( endDate.yearMonth) == 0) return false
        if (outDate.yearMonth.compareToYearMonth(endDate.yearMonth) == 0) return true
        if (outDate.yearMonth.minusMonths(1).compareToYearMonth(startDate.yearMonth) == 0) return true
        return outDate > startDate && outDate < endDate
    }

    private fun bindSummaryViews() {
        binding.exFourStartDateText.apply {
            if (startDate != null) {
                text = startDate?.let { Formatter.format(it, headerDateFormatter) }
                setTextColorRes(R.color.example_4_grey)
            } else {
                text = getString(R.string.start_date)
                setTextColor(Color.GRAY)
            }
        }

        binding.exFourEndDateText.apply {
            if (endDate != null) {
                text = endDate?.let { Formatter.format(it, headerDateFormatter) }
                setTextColorRes(R.color.example_4_grey)
            } else {
                text = getString(R.string.end_date)
                setTextColor(Color.GRAY)
            }
        }

        // Enable save button if a range is selected or no date is selected at all, Airbnb style.
        binding.exFourSaveButton.isEnabled =
            endDate != null || (startDate == null && endDate == null)
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
            setColorFilter(
                requireContext().getColorCompat(R.color.example_4_grey),
                PorterDuff.Mode.SRC_ATOP
            )
        }
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(closeIndicator)
        requireActivity().window.apply {
            // Update StatusBar color to match toolbar color.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                statusBarColor = requireContext().getColorCompat(R.color.white)
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBarColor = Color.GRAY
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.apply {
            // Reset StatusBar color.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                statusBarColor = requireContext().getColorCompat(R.color.purple_700)
            }
            decorView.systemUiVisibility = 0
        }
    }
}
