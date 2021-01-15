package com.maddy.calendar.ui.model

import com.maddy.calendar.ui.utils.next
import com.maddy.calendar.ui.utils.previous
import com.maddy.calendar.ui.utils.yearMonth
import java.io.Serializable

data class CalendarDay internal constructor(val date: LocalDate, val owner: DayOwner) :
    Comparable<CalendarDay>, Serializable {

    val day = date.day

    // Find the actual month on the calendar that owns this date.
    internal val positionYearMonth: YearMonth
        get() = when (owner) {
            DayOwner.THIS_MONTH -> date.yearMonth
            DayOwner.PREVIOUS_MONTH -> date.previous
            DayOwner.NEXT_MONTH -> date.next
        }

    override fun toString(): String {
        return "CalendarDay { date =  $date, owner = $owner}"
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
