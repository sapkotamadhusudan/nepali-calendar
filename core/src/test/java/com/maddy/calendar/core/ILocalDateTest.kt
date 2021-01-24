package com.maddy.calendar.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

class ILocalDateTest {


    @Test
    fun `test monthsBetween`() {
        val start = ILocalDate.ofAD(2018, 9, 1)
        val end = ILocalDate.ofAD(2022, 4, 1)

        val s = LocalDate.of(2018, 9, 1)
        val e = LocalDate.of(2022, 4, 1)

        assertThat(Period.monthsBetween(start, end))
            .isEqualTo(ChronoUnit.MONTHS.between(s, e))
        assertThat(Period.monthsBetween(end, start))
            .isEqualTo(ChronoUnit.MONTHS.between(e, s))

        println(DayOfWeek.SUNDAY.name(ILocalDate.Type.AD))
    }

    @Test
    fun `test daysBetween`() {
        val start = ILocalDate.ofAD(2010, 10, 1)
        val end = ILocalDate.ofAD(2021, 1, 22)

        assertThat(ILocalDate.Utils.daysDifference(start, end))
            .isEqualTo(Period.daysBetween(start, end))
        assertThat(ILocalDate.Utils.daysDifference(end, start))
            .isEqualTo(Period.daysBetween(end, start))

        val javaStart = LocalDate.of(2010, 10, 1)
        val javaEnd = LocalDate.of(2021, 1, 22)

        assertThat(Period.daysBetween(start, end)).isEqualTo(
            ChronoUnit.DAYS.between(
                javaStart,
                javaEnd
            )
        )
        assertThat(Period.daysBetween(end, start)).isEqualTo(
            ChronoUnit.DAYS.between(
                javaEnd,
                javaStart
            )
        )

        // compare for BS dates
        assertThat(
            Period.daysBetween(
                start.reverse(),
                end.reverse()
            )
        ).isEqualTo(ChronoUnit.DAYS.between(javaStart, javaEnd))
    }

    @Test
    fun `test firstDayOfYear`() {
        val today = ILocalDate.ofAD(2020, 2, 22)

        val subMonth = ILocalDate.Utils.monthDaysAD().toList().subList(0, today.monthValue - 1)
        assertThat(today.dayOfYear).isEqualTo(subMonth.sum() + today.dayOfMonth)

        val javaToday = LocalDate.of(2020, 2, 22)
        assertThat(today.dayOfYear).isEqualTo(javaToday.dayOfYear)

        val todayBs = ILocalDate.nowBS()
        val subMonthBS =
            ILocalDate.Utils.monthDaysBS(todayBs.year).toList().subList(0, todayBs.monthValue - 1)
        assertThat(todayBs.dayOfYear).isEqualTo(subMonthBS.sum() + todayBs.dayOfMonth)
    }


    @Test
    fun `test countWeek`() {
        val today = ILocalDate.nowAD()

        println(ILocalDate.Utils.weekOfMonth(today, DayOfWeek.SUNDAY))
        assertThat(
            Period.weeksBetween(
                today.atStartOfMonth(),
                today,
                DayOfWeek.SUNDAY
            )
        ).isEqualTo(ILocalDate.Utils.weekOfMonth(today, DayOfWeek.SUNDAY))


        val weeksCount = ILocalDate.Utils.countWeek(today, DayOfWeek.SUNDAY)
        val weeksCountNew = Period.weeksBetween(
            today.atStartOfMonth(),
            today.atEndOfMonth(),
            DayOfWeek.SUNDAY
        )

        assertThat(weeksCountNew).isEqualTo(weeksCount)

        val javaToday = YearMonth.now()
        val javaWeekCount = ChronoUnit.WEEKS.between(
            javaToday.atDay(1).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY)),
            javaToday.atEndOfMonth().with(TemporalAdjusters.next(java.time.DayOfWeek.SUNDAY))
        )
        assertThat(weeksCount).isEqualTo(javaWeekCount)
    }

    @Test
    fun `test operators`() {
        val start = ILocalDate.ofAD(2020, 1, 1)
        val end = ILocalDate.ofAD(2021, 1, 1)

        val function = start.before(end)
        assertThat(start < end).isEqualTo(function)
    }

    @Test
    fun `test before`() {
        val a = ILocalDate.ofBS(2077, 6, 20)
        val b = ILocalDate.ofBS(2077, 7, 1)

        assertThat(a.before(b)).isEqualTo(true)
        assertThat(a.before(a)).isEqualTo(false)
        assertThat(b.before(a)).isEqualTo(false)
    }

    @Test
    fun `test after`() {
        val a = ILocalDate.ofBS(2077, 6, 20)
        val b = ILocalDate.ofBS(2077, 7, 1)

        assertThat(a.after(b)).isEqualTo(false)
        assertThat(a.after(a)).isEqualTo(false)
        assertThat(b.after(a)).isEqualTo(true)
    }


    @Test
    fun `validate plusMonth`() {
        val date = ILocalDate.nowAD()
        val javaDate = LocalDate.now()

        var changed = date.plusMonths(14)
        var javaChanged = javaDate.plusMonths(14)

        changed = changed.plusMonths(-14)
        javaChanged = javaChanged.minusMonths(14)


        assertThat(date.year).isEqualTo(changed.year)
        assertThat(date.month).isEqualTo(changed.month)
        assertThat(date.dayOfMonth).isEqualTo(changed.dayOfMonth)

        assertThat(date.year).isEqualTo(javaChanged.year)
        assertThat(date.monthValue).isEqualTo(javaChanged.monthValue)
        assertThat(date.dayOfMonth).isEqualTo(javaChanged.dayOfMonth)
    }

    @Test
    fun testDayOfWeek() {
        val firstDayOfWeek = DayOfWeek.MONDAY
        val weekOfMonthField = WeekFields.of(java.time.DayOfWeek.MONDAY, 1).weekOfMonth()

        val date = ILocalDate.nowAD()
        val javaDate = LocalDate.now()

        assertThat(javaDate.get(weekOfMonthField)).isEqualTo(
            ILocalDate.Utils.weekOfMonth(
                date,
                firstDayOfWeek
            )
        )

        assertThat(javaDate.get(weekOfMonthField)).isEqualTo(
            Period.weeksBetween(
                date.atStartOfMonth(),
                date,
                firstDayOfWeek
            )
        )
    }

    @Test
    fun testDayOfWeekWithJavaTime() {

        val today = LocalDate.now()
        val javaMonthDays =
            (1..today.lengthOfMonth()).map { LocalDate.of(2021, today.monthValue, it) }
        val monthDays = (1..today.lengthOfMonth()).map {
            ILocalDate.of(
                2021,
                today.monthValue,
                it,
                ILocalDate.Type.AD
            )
        }

        val firstDayOfWeek = DayOfWeek.THURSDAY
        val weekOfMonthField = WeekFields.of(java.time.DayOfWeek.THURSDAY, 1).weekOfMonth()
        val groupedJava =
            javaMonthDays.groupBy { it.get(weekOfMonthField) }.values.map { it.map { item -> item.toString() } }

        println("Java Calendar")

        val weekDays = DayOfWeek.weekDays(firstDayOfWeek)
            .joinToString(" ") { "  ${it.name.padEnd(10, ' ')}  " }
        println(weekDays)

        val calendarText = groupedJava.map { items ->
            val fillDates = (7 - items.size)
            val row =
                if (fillDates == 0) items
                else (1..fillDates).map { "          " }.toMutableList()
                    .apply { addAll(items) }

            row.joinToString(" ") { "  $it  " }
        }.joinToString("\n")
        println(calendarText)


        val groupBy =
            monthDays.groupBy {
                ILocalDate.Utils.weekOfMonth(it, firstDayOfWeek)
            }.values.map { items ->
                items.map { date ->
                    "${date.year}-${
                        date.monthValue.toString().padStart(2, '0')
                    }-${date.dayOfMonth.toString().padStart(2, '0')}"
                }
            }


        println("Custom Calendar")
        val javaWeekDays = DayOfWeek.weekDays(firstDayOfWeek)
            .joinToString(" ") { "  ${it.name.padEnd(10, ' ')}  " }
        println(javaWeekDays)
        val javaCalendarText = groupBy.map { items ->

            val fillDates = 7 - items.size
            val row =
                if (fillDates == 0) items
                else (1..fillDates).map { "          " }.toMutableList().apply { addAll(items) }

            row.joinToString(" ") { "  $it  " }
        }.joinToString("\n")
        println(javaCalendarText)

        assertThat(javaWeekDays).isEqualTo(weekDays)
        assertThat(javaCalendarText).isEqualTo(calendarText)
    }

    @Test
    fun testDayOfWeekWithJavaTime2() {
        assertThat(
            ILocalDate.nowAD().dayOfWeek.value
        ).isEqualTo(LocalDate.now().dayOfWeek.value)
        assertThat(
            ILocalDate.nowBS().dayOfWeek.value
        ).isEqualTo(LocalDate.now().dayOfWeek.value)
    }

    @Test
    fun addAndRemoveDays() {
        val date = ILocalDate.nowAD()
        val changed = date.plusDays(100).plusDays(-100)
        assertThat(date.year).isEqualTo(changed.year)
        assertThat(date.month).isEqualTo(changed.month)
        assertThat(date.dayOfMonth).isEqualTo(changed.dayOfMonth)
    }

    @Test
    fun testReverse() {
        val date = ILocalDate.nowAD()
        val reversed = date.reverse().reverse()
        assertThat(date.year).isEqualTo(reversed.year)
        assertThat(date.month).isEqualTo(reversed.month)
        assertThat(date.dayOfMonth).isEqualTo(reversed.dayOfMonth)
    }

}