package com.maddy.calendar.core

import java.util.*

/**
 * Created by Madhusudan Sapkota on 8/12/20.
 */
abstract class ILocalDate {

    enum class Type {
        AD, BS
    }

    /**
     * The year.
     */
    var year: Int
        protected set

    /**
     * The month-of-year.
     */
    var month: Month
        protected set

    /**
     * The day-of-month.
     */
    var day: Int
        protected set

    abstract val type: Type
    protected abstract fun referenceDate(): ILocalDate
    protected abstract fun monthDays(): IntArray
    protected abstract fun maxYear(): Int
    protected abstract fun minYear(): Int

    protected fun isLeapYear(year: Int): Boolean {
        return if (type == Type.BS) {
            false
        } else {
            if (year % 4 == 0) {
                if (year % 100 == 0) {
                    // Century Year must be divisible by 400 for Leap year
                    year % 400 == 0
                } else true
            } else false
        }

    }

    protected fun getDaysSinceReferenceDate(): Long {
        return daysDifference(this.referenceDate(), this)
    }

    private fun lengthOfYear(year: Int): Int {
        val leap = if (isLeapYear(year)) 1 else 0
        return monthDays().sum() + leap
    }

    private fun lengthOfMonth(year: Int, month: Month): Int {
        return if (isLeapYear(year) && month == Month.FEBRUARY_JESTHA) 29
        else monthDays()[month.getValue() - 1]
    }

    /**
     * Returns the length of the year.
     * <p>
     * This returns the length of the year in days, either 365 or 366.
     *
     * @return 366 if the year is leap, 365 otherwise
     */
    fun lengthOfYear(): Int {
        return lengthOfYear(year)
    }

    /**
     * Returns the length of the month, taking account of the year.
     * <p>
     * This returns the length of the month in days.
     * For example, a date in January would return 31.
     *
     * @return the length of the month in days, from 28 to 31
     */
    fun lengthOfMonth(): Int {
        return lengthOfMonth(this.year, this.month)
    }

    /**
     * Returns the day of week, taking account of the weekOfMonth.
     * <p>
     * This returns the day of week of current day.
     * For example, a day placed in Sunday would return 0.
     *
     * @return the length of the month in days, from 0 to 6
     */
    fun dayOfWeek(): DayOfWeek {
        val firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
        val daysSinceReferenceDate = this.getDaysSinceReferenceDate()

        val weekOfMonthValue = firstDayOfWeek.getValue() - 1
        val dayOfWeek = (if (daysSinceReferenceDate > 0) {
            ((daysSinceReferenceDate % 7) + weekOfMonthValue) % 7
        } else {
            (((7 - ((daysSinceReferenceDate * -1) % 7)) % 7) + weekOfMonthValue) % 7
        }).toInt()
        return DayOfWeek.of(dayOfWeek + 1)
    }

    abstract fun firstDayOfYear(): Int

    /**
     * Returns the start week of month, taking account of the weekOfMonth.
     * <p>
     * This returns the start weekDay of current month.
     * For example, a month started with Sunday would return 0.
     *
     * @return the starting day of month
     */
    fun startDayOfWeek(): DayOfWeek =
        this.atStartOfMonth().dayOfWeek()

    fun isCurrentMonth(): Boolean {
        val month = if (type == Type.AD) nowAD().month else nowBS().month
        return this.month == month
    }

    fun plusDays(daysToAdd: Long): ILocalDate {
        if (daysToAdd == 0L) {
            return this
        }
        val dom = day.toLong() + daysToAdd
        val monthLen: Long

        if (dom > 0L) {

            if (dom <= 28L) {
                return instance(this.year, this.month, dom.toInt())
            }

            if (dom <= 59L) {
                monthLen = lengthOfMonth().toLong()
                if (dom <= monthLen) {
                    return instance(this.year, this.month, dom.toInt())
                }

                if (this.month < Month.DECEMBER_CHAITRA) {
                    return instance(this.year, this.month.plus(1), (dom - monthLen).toInt())
                }

                return instance(
                    checkValidYear(this.year + 1),
                    Month.JANUARY_BAISHAK,
                    (dom - monthLen).toInt()
                )
            }

            var dayDelta = daysToAdd
            val finalDate = this.copy()
            while (dayDelta > 0L) {
                dayDelta--
                finalDate.addSingleDay()
            }
            return finalDate

        } else {
            var dayDelta = daysToAdd
            val finalDate = this.copy()
            while (dayDelta < 0L) {
                dayDelta++
                finalDate.subtractSingleDay()
            }
            return finalDate
        }
    }

    fun addSingleDay() {
        this.day++
        if (this.day > this.lengthOfMonth()) {
            this.day = 1
            val newMonth = this.month.plus(1)
            if (newMonth == Month.JANUARY_BAISHAK) {
                this.year++
            }
            this.month = newMonth
        }
    }

    fun subtractSingleDay() {
        this.day--
        if (this.day < 1) {
            val newMonth = this.month.minus(1)
            if (newMonth == Month.DECEMBER_CHAITRA) {
                this.year--
            }
            this.month = newMonth
            this.day = this.lengthOfMonth()
        }
    }

    fun plusMonths(monthsToAdd: Long): ILocalDate {
        if (monthsToAdd == 0L) {
            return this
        }
        val isNegative = monthsToAdd < 0
        val monthCount = year * 12L + (month.getValue() - 1 /* TODO: Need to check this value */)
        val calcMonths: Long = monthCount + monthsToAdd // safe overflow

        val newYear = checkValidYear(
            Helper.floorDiv(
                calcMonths,
                12.toLong()
            )
        )
        var newMonth = Helper.floorMod(calcMonths, 12).toInt() + 1

        val newMaxDays = lengthOfMonth(newYear, checkValidMonth(newMonth))
        var newDay = this.day

        if (this.day > newMaxDays) {
            newDay = this.day - newMaxDays
            newMonth = if (isNegative) newMonth - 1 else newMonth + 1
        }
        return of(newYear, newMonth, newDay, this.type)
    }

    fun plusYears(yearsToAdd: Long): ILocalDate {
        if (yearsToAdd == 0L) {
            return this
        }

        val newYear = checkValidYear(year + yearsToAdd.toInt())
        val newDay = this.day.coerceAtMost(lengthOfMonth(newYear, this.month))

        return instance(newYear, month, newDay)
    }

    fun setDates(year: Int, month: Int, dayOfMonth: Int): ILocalDate {
        val copy = this.copy()
        copy.year = checkValidYear(year)
        copy.month = checkValidMonth(month)
        copy.day = 1
        val lengthOfMonth = copy.lengthOfMonth()

        if (dayOfMonth in 1..lengthOfMonth) {
            copy.day = dayOfMonth
        }
        return copy
    }

    /**
     * Constructor, previously validated.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, valid for year-month, from 1 to 31
     */
    private constructor(year: Int, month: Month, dayOfMonth: Int) {
        this.year = year
        this.month = month
        this.day = dayOfMonth
    }

    private fun checkValidYear(year: Int): Int {
        return year
    }

    private fun checkValidMonth(month: Int): Month {
        return if (month < Month.JANUARY_BAISHAK.getValue() || month > Month.DECEMBER_CHAITRA.getValue()) Month.JANUARY_BAISHAK
        else Month.of(month)
    }

    fun reverse(): ILocalDate {
        return convert(this, if (this.type == Type.BS) Type.AD else Type.BS)
    }

    fun AD(): ILocalDate {
        if (this.type == Type.AD) return this

        return convert(this, Type.AD)
    }

    fun BS(): ILocalDate {
        if (this.type == Type.BS) return this

        return convert(this, Type.BS)
    }

    private fun instance(year: Int, month: Month, dayOfMonth: Int): ILocalDate {
        return if (this.type == Type.AD) ADLocalDate(year, month, dayOfMonth)
        else BSLocalDate(year, month, dayOfMonth)
    }

    fun copy(): ILocalDate {
        return instance(year, month, day)
    }

    fun before(from: ILocalDate): Boolean {
        return year < from.year ||
                year <= from.year &&
                (month < from.month ||
                        (month <= from.month && day < from.day))
    }

    fun after(baseCalendar: ILocalDate): Boolean {
        return year > baseCalendar.year ||
                year >= baseCalendar.year &&
                (month > baseCalendar.month ||
                        month >= baseCalendar.month &&
                        (day > baseCalendar.day))
    }

    fun weeks(): Int = countWeek(this)

    fun previousMonthWeeks(): Int = countWeek(instance(this.year, this.month - 1, 1))

    fun nextMonthWeeks(): Int = countWeek(instance(this.year, this.month + 1, 1))

    fun atStartOfMonth(): ILocalDate = instance(year, month, 1)

    fun atStartOfYear(): ILocalDate = instance(year, Month.of(1), 1)

    fun atEndOfMonth(): ILocalDate = instance(year, month, lengthOfMonth())

    operator fun compareTo(other: ILocalDate): Int {
        var cmp = if (type == other.type) 0 else 1
        if (cmp == 0) {
            (year - other.year)
            if (cmp == 0) {
                cmp = (month.getValue() - other.month.getValue())
                if (cmp == 0) {
                    cmp = (day - other.day)
                }
            }
        }
        return cmp
    }

    override fun toString(): String {
        return "ILocalDate { \"year\": $year, \"month\": $month, \"dayOfMonth\": $day, \"type\": $type  }"
    }

    companion object {

        fun nowBS(): ILocalDate {
            return BSLocalDate.now()
        }

        fun nowAD(): ILocalDate {
            return ADLocalDate.now()
        }

        fun of(year: Int, month: Int, dayOfMonth: Int, type: Type = Type.BS): ILocalDate {
            return if (type == Type.BS) ofBS(year, month, dayOfMonth)
            else ofAD(year, month, dayOfMonth)
        }

        fun ofBS(year: Int, month: Int, dayOfMonth: Int): ILocalDate {
            return BSLocalDate(year, Month.of(month), dayOfMonth)
        }

        fun ofAD(year: Int, month: Int, dayOfMonth: Int): ILocalDate {
            return ADLocalDate(year, Month.of(month), dayOfMonth)
        }

        fun convert(date: ILocalDate, type: Type): ILocalDate {
            if (date.type == type) return date

            val iLocalDate = if (type == Type.AD) {
                ADLocalDate.REFERENCE_DATE
            } else {
                BSLocalDate.REFERENCE_DATE
            }
            val daysToAdd = date.getDaysSinceReferenceDate()
            return iLocalDate.plusDays(daysToAdd)
        }

        fun daysDifference(from: ILocalDate, to: ILocalDate): Long {
            val toS = if (from.type != to.type) {
                to.reverse()
            } else to.copy()

            var fromDate: ILocalDate
            val toDate: ILocalDate

            val isNegative = if (from.before(toS)) {
                fromDate = from.copy()
                toDate = toS
                1
            } else {
                fromDate = toS
                toDate = from.copy()
                -1
            }

            var daysCount = 0L
            while (fromDate.before(toDate)) {
                fromDate = fromDate.plusDays(1)
                daysCount++
            }

            return daysCount * isNegative
        }

        /**
         * Returns the count of week, taking account of the weekOfMonth.
         * <p>
         * This returns the total number of week of current month.
         *
         * @return the length of the month in days, from 4 to 5
         */
        fun countWeek(date: ILocalDate, firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): Int {
            var startDayDate = date.atStartOfMonth()
            var weekCount = if (startDayDate.dayOfWeek() == firstDayOfWeek) 0 else 1
            val currentMonth = startDayDate.month
            val nextMonth = if (currentMonth == Month.DECEMBER_CHAITRA) 1 else currentMonth + 1

            while (true) {
                if (startDayDate.month == nextMonth) break
                if (startDayDate.dayOfWeek() == firstDayOfWeek) weekCount++
                startDayDate = startDayDate.plusDays(1)
            }
            return weekCount
        }

        fun weekOfMonth(date: ILocalDate, firstDayOfWeek: DayOfWeek): Int {
            var startDayDate = date.atStartOfMonth()
            var weekCount = if (startDayDate.dayOfWeek() == firstDayOfWeek) 0 else 1
            val currentMonth = startDayDate.month
            val nextMonth = if (currentMonth == Month.DECEMBER_CHAITRA) 1 else currentMonth + 1

            while (true) {
                if (startDayDate.month == nextMonth) break

                val dayOfWeek = startDayDate.dayOfWeek()
                if (dayOfWeek == firstDayOfWeek) weekCount++

                if (startDayDate.year == date.year
                    && startDayDate.month == startDayDate.month
                    && startDayDate.day == date.day
                    && startDayDate.type == date.type
                ) return weekCount

                startDayDate = startDayDate.plusDays(1)
            }
            return weekCount
        }
    }


    /**
     * Constructor, previously validated.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, valid for year-month, from 1 to 31
     */
    private class BSLocalDate : ILocalDate {

        val yearMonthSpanLookupTable = arrayOf(
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 29, 30, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 32, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(30, 32, 31, 32, 31, 31, 29, 30, 30, 29, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 32, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(30, 32, 31, 32, 31, 31, 29, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 29, 30, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(31, 32, 31, 32, 30, 31, 30, 30, 29, 30, 30, 30),
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 30, 29, 30, 30, 30),
            intArrayOf(30, 31, 32, 32, 30, 31, 30, 30, 29, 30, 30, 30),
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 30, 29, 30, 30, 30),
            intArrayOf(30, 31, 32, 32, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 30, 30, 30),
            intArrayOf(30, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30),
            intArrayOf(31, 31, 32, 31, 31, 31, 29, 30, 29, 30, 29, 31),
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 29, 30, 30, 31)
        )

        override fun monthDays(): IntArray = yearMonthSpanLookupTable[yearIndex()]

        override fun referenceDate() = REFERENCE_DATE

        override fun maxYear() = 2200

        override fun minYear() = 1999

        override fun firstDayOfYear(): Int {
            return daysSum(this.month)
        }

        private fun daysSum(month: Month): Int {
            val monthDays = monthDays()
            return when (month) {
                Month.JANUARY_BAISHAK -> 1
                Month.FEBRUARY_JESTHA,
                Month.MARCH_ASADH,
                Month.APRIL_SHRWAN,
                Month.MAY_BHADRA,
                Month.JUNE_ASHWIN,
                Month.JULY_KARTIK,
                Month.AUGUST_MANGSIR,
                Month.SEPTEMBER_PAUSH,
                Month.OCTOBER_MAGH,
                Month.NOVEMBER_FALGUN,
                Month.DECEMBER_CHAITRA -> daysSum(month - 1) + monthDays[month.getValue() - 2]
                else -> daysSum(Month.DECEMBER_CHAITRA) + monthDays[Month.DECEMBER_CHAITRA.getValue() - 1]
            }
        }

        override val type: Type
            get() = Type.BS

        constructor(year: Int, month: Month, dayOfMonth: Int) : super(year, month, dayOfMonth)

        companion object {
            val REFERENCE_DATE = BSLocalDate(2059, Month.JANUARY_BAISHAK, 1)
            fun now(): ILocalDate {
                return ADLocalDate.now().reverse()
            }
        }

        private fun yearIndex(): Int {
            val index = (this.year - 1999) % 100
            return if (index < 0) index * -1 else index
        }
    }

    /**
     * Constructor, previously validated.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, valid for year-month, from 1 to 31
     */
    private class ADLocalDate : ILocalDate {

        override fun minYear() = -999999999

        override fun maxYear() = 999999999

        override fun monthDays() = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        override fun referenceDate() = REFERENCE_DATE

        override fun firstDayOfYear(): Int {
            val leap = if (isLeapYear(this.year)) 1 else 0
            return when (this.month) {
                Month.JANUARY_BAISHAK -> 1
                Month.FEBRUARY_JESTHA -> 32
                Month.MARCH_ASADH -> 60 + leap
                Month.APRIL_SHRWAN -> 91 + leap
                Month.MAY_BHADRA -> 121 + leap
                Month.JUNE_ASHWIN -> 152 + leap
                Month.JULY_KARTIK -> 182 + leap
                Month.AUGUST_MANGSIR -> 213 + leap
                Month.SEPTEMBER_PAUSH -> 244 + leap
                Month.OCTOBER_MAGH -> 274 + leap
                Month.NOVEMBER_FALGUN -> 305 + leap
                Month.DECEMBER_CHAITRA -> 335 + leap
                else -> 335 + leap
            }
        }

        override val type: Type
            get() = Type.AD

        constructor(year: Int, month: Month, dayOfMonth: Int) : super(year, month, dayOfMonth)

        companion object {
            val REFERENCE_DATE = ADLocalDate(2002, Month.of(4), 14)
            fun now(): ILocalDate {
                val now = Calendar.getInstance()
                return ADLocalDate(
                    now.get(Calendar.YEAR),
                    Month.of(now.get(Calendar.MONTH) + 1),
                    now.get(Calendar.DAY_OF_MONTH)
                )
            }
        }
    }
}