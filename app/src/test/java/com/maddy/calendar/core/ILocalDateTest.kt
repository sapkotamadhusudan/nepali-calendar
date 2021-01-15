package com.maddy.calendar.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields

class ILocalDateTest {

    @Test
    fun testDayOfWeek() {
        val firstDayOfWeek = com.maddy.calendar.core.DayOfWeek.MONDAY
        val weekOfMonthField = WeekFields.of(DayOfWeek.MONDAY, 1).weekOfMonth()

        val date = ILocalDate.nowAD()
        val javaDate = LocalDate.now()

        assertThat(javaDate.get(weekOfMonthField)).isEqualTo(ILocalDate.weekOfMonth(date, firstDayOfWeek))
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

        val firstDayOfWeek = com.maddy.calendar.core.DayOfWeek.THURSDAY
        val weekOfMonthField = WeekFields.of(DayOfWeek.THURSDAY, 1).weekOfMonth()
        val groupedJava =
            javaMonthDays.groupBy { it.get(weekOfMonthField) }.values.map { it.map { item -> item.toString() } }

        println("Java Calendar")
        println(
            com.maddy.calendar.core.DayOfWeek.weekDays(firstDayOfWeek)
                .joinToString(" ") { "  ${it.name.padEnd(10, ' ')}  " })

        groupedJava.forEach { items ->
            val fillDates = (7 - items.size)
            val row =
                if (fillDates == 0) items
                else (1..fillDates).map { "          " }.toMutableList()
                    .apply { addAll(items) }

            println(row.joinToString(" ") { "  $it  " })
        }


        val groupBy =
            monthDays.groupBy {
                ILocalDate.weekOfMonth(it, firstDayOfWeek)
            }.values.map { items ->
                items.map { date ->
                    "${date.year}-${
                        date.month.getValue().toString().padStart(2, '0')
                    }-${date.day.toString().padStart(2, '0')}"
                }
            }


        println("Custom Calendar")
        println(
            com.maddy.calendar.core.DayOfWeek.weekDays(firstDayOfWeek)
                .joinToString(" ") { "  ${it.name.padEnd(10, ' ')}  " })
        groupBy.forEach { items ->

            val fillDates = 7 - items.size
            val row =
                if (fillDates == 0) items
                else (1..fillDates).map { "          " }.toMutableList().apply { addAll(items) }

            println(row.joinToString(" ") { "  $it  " })
        }
    }

    @Test
    fun testDayOfWeekWithJavaTime2() {
        assertThat(
            ILocalDate.nowAD().dayOfWeek().getValue()
        ).isEqualTo(LocalDate.now().dayOfWeek.value)
        assertThat(
            ILocalDate.nowBS().dayOfWeek().getValue()
        ).isEqualTo(LocalDate.now().dayOfWeek.value)
    }

    @Test
    fun addAndRemoveDays() {
        val date = ILocalDate.nowAD()
        val changed = date.plusDays(100).plusDays(-100)
        assertThat(date.year).isEqualTo(changed.year)
        assertThat(date.month).isEqualTo(changed.month)
        assertThat(date.day).isEqualTo(changed.day)
    }

    @Test
    fun testReverse() {
        val date = ILocalDate.nowAD()
        val reversed = date.reverse().reverse()
        assertThat(date.year).isEqualTo(reversed.year)
        assertThat(date.month).isEqualTo(reversed.month)
        assertThat(date.day).isEqualTo(reversed.day)
    }

}