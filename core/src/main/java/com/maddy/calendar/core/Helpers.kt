package com.maddy.calendar.core

object Math {
    fun floorDiv(x: Long, y: Long): Int {
        var r = x / y
        // if the signs are different and modulo not zero, round down
        if (x xor y < 0 && r * y != x) {
            r--
        }
        return r.toInt()
    }

    fun floorMod(x: Long, y: Long): Long {
        return x - floorDiv(x, y) * y
    }

    fun addExact(x: Long, y: Long): Long {
        val r: Long = x + y
        return if (x xor r and (y xor r) < 0L) {
            throw ArithmeticException("long overflow")
        } else {
            r
        }
    }
}

object Period {

    fun weeksBetween(
        start: ILocalDate,
        end: ILocalDate,
        firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
    ): Int {
        val (isNegative, startEnd) = startEndType(start, end)
        val (earlyDate, laterDate) = startEnd

        var totalDays = daysBetween(earlyDate, laterDate) + 1

        if (totalDays <= 1L){
            return 1
        }

        val startDayOfWeek = earlyDate.dayOfWeek
        val dom = (firstDayOfWeek - startDayOfWeek).value
        var count = 0
        if (dom > 0){
            count++
            totalDays-=dom
        }

        val fullWeeks = (totalDays/7).toInt()
        val remDays = totalDays%7
        count += fullWeeks
        if (remDays > 0){
            count++
        }
        return count * isNegative
//        var weekCount = if (startDayOfWeek == firstDayOfWeek) 0 else 1
//        while (totalDays > 0) {
//            if (startDayOfWeek == firstDayOfWeek) {
//                weekCount++
//            }
//
//
//            startDayOfWeek = startDayOfWeek.plus(1)
//            totalDays--
//        }
//        return weekCount * isNegative
    }

    fun monthsBetween(start: ILocalDate, end: ILocalDate): Int {
        if (start == end) {
            return 0
        }

        val (isNegative, startEnd) = startEndType(start, end)
        val (earlyDate, laterDate) = startEnd

        val yearDifference = laterDate.year - earlyDate.year - 1
        var dom = yearDifference * Month.DECEMBER_CHAITRA.value
        dom += (Month.DECEMBER_CHAITRA - earlyDate.month).value + laterDate.monthValue
        return dom * isNegative
    }

    fun daysBetween(start: ILocalDate, end: ILocalDate): Long {
        if (start == end) {
            return 0
        }

        val (isNegative, startEnd) = startEndType(start, end)
        val (earlyDate, laterDate) = startEnd

        if (earlyDate.year == laterDate.year) {
            return (laterDate.dayOfYear - earlyDate.dayOfYear).toLong()
        }

        var dom: Long = 0
        for (year in (earlyDate.year + 1) until laterDate.year) {
            dom += ILocalDate.lengthOfYear(year, earlyDate.type)
        }

        dom += earlyDate.lengthOfYear - earlyDate.dayOfYear
        dom += laterDate.dayOfYear
        return dom * isNegative
    }

    internal fun startEndType(
        start: ILocalDate,
        end: ILocalDate
    ): Pair<Int, Pair<ILocalDate, ILocalDate>> {
        val toS = if (start.type != end.type) {
            end.reverse()
        } else end.copy()

        val fromDate: ILocalDate
        val toDate: ILocalDate

        val isNegative = when {
            start == toS -> {
                fromDate = start.copy()
                toDate = toS
                1
            }
            start < toS -> {
                fromDate = start.copy()
                toDate = toS
                1
            }
            else -> {
                fromDate = toS
                toDate = start.copy()
                -1
            }
        }
        return isNegative to (fromDate to toDate)
    }
}