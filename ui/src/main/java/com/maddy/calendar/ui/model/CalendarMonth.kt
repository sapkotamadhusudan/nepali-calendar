package com.maddy.calendar.ui.model

import java.io.Serializable

data class CalendarMonth(
    val yearMonth: YearMonth,
    val weekDays: List<List<CalendarDay>>,
    internal val indexInSameMonth: Int,
    internal val numberOfSameMonth: Int
) : Comparable<CalendarMonth>, Serializable {

    val year: Int = yearMonth.year
    val month: Int = yearMonth.monthValue

    override fun hashCode(): Int {
        return 31 * yearMonth.hashCode() +
                weekDays.first().first().hashCode() +
                weekDays.last().last().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        (other as CalendarMonth)
        return yearMonth == other.yearMonth &&
                weekDays.first().first() == other.weekDays.first().first() &&
                weekDays.last().last() == other.weekDays.last().last()
    }

    override fun compareTo(other: CalendarMonth): Int {
        val monthResult = yearMonth.compareToYearMonth(other.yearMonth)
        if (monthResult == 0) { // Same yearMonth
            return indexInSameMonth.compareTo(other.indexInSameMonth)
        }
        return monthResult
    }

    override fun toString(): String {
        return "CalendarMonth { first = ${weekDays.first().first()}, last = ${
            weekDays.last().last()
        }} " +
                "indexInSameMonth = $indexInSameMonth, numberOfSameMonth = $numberOfSameMonth"
    }
}

/**
 * Compares this date to another date.
 * <p>
 * The comparison is primarily based on the date, from earliest to latest.
 * It is "consistent with equals", as defined by {@link Comparable}.
 * <p>
 * If all the dates being compared are instances of {@code ILocalDate},
 * then the comparison will be entirely based on the date.
 *
 * @param other  the other date to compare to, not null
 * @return the comparator value, negative if less, positive if greater
 */
fun YearMonth.compareToYearMonth(other: YearMonth): Int {
    var cmp = (year - other.year)
    if (cmp == 0) {
        cmp = (monthValue - other.monthValue)
    }
    return cmp
}
