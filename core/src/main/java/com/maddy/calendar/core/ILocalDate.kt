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
    var year = 0
        protected set(value) {
            field = value
        }

    /**
     * The month-of-year.
     */
     var month: Short = 0
        protected set(value) {
            field = value
        }

    /**
     * The day-of-month.
     */
     var day: Short = 0
        protected set(value) {
            field = value
        }

    abstract val type: Type
    protected abstract fun referenceDate(): ILocalDate
    protected abstract fun monthDays(): IntArray
    protected abstract fun maxYear(): Int
    protected abstract fun minYear(): Int

    private fun leapYear(year: Int): Boolean {
        return if (type == Type.BS) false
        else ((year % 4 == 0 || year % 100 != 0) && year % 400 == 0)
    }

    protected fun getFirstDayOfWeek(): Int = 0

    protected fun getDaysSinceReferenceDate(): Long {
        return daysDifference(this.referenceDate(), this)
    }

    private fun lengthOfYear(year: Int): Int {
        val leap = if (leapYear(year)) 1 else 0
        return monthDays().sum() + leap
    }

    private fun lengthOfMonth(year: Int, month: Int): Int {
        return if (leapYear(year) && month == 2) 29
        else monthDays()[month - 1]
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
        return lengthOfMonth(this.year, this.month.toInt())
    }

    fun dayOfWeek(): Int {
        val daysSinceReferenceDate = this.getDaysSinceReferenceDate()

        return (if (daysSinceReferenceDate > 0) {
            ((daysSinceReferenceDate % 7) + this.getFirstDayOfWeek()) % 7
        } else {
            (((7 - ((daysSinceReferenceDate * -1) % 7)) % 7) + this.getFirstDayOfWeek()) % 7
        }).toInt()
    }

    fun startDayOfWeek(): Int = this.atStartOfMonth().dayOfWeek()

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
                return instance(this.year, this.month.toInt(), dom.toInt())
            }

            if (dom <= 59L) {
                monthLen = lengthOfMonth().toLong()
                if (dom <= monthLen) {
                    return instance(this.year, this.month.toInt(), dom.toInt())
                }

                if (this.month < 12) {
                    return instance(this.year, this.month + 1, (dom - monthLen).toInt())
                }

                return instance(checkValidYear(this.year + 1), 1, (dom - monthLen).toInt())
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

    private fun addSingleDay() {
        this.day++
        if (this.day > this.lengthOfMonth()) {
            this.day = 1
            this.month++
            if (this.month > 12) {
                this.month = 1
                this.year++
            }
        }
    }

    private fun subtractSingleDay() {
        this.day--
        if (this.day < 1) {
            this.month--
            if (this.month < 1) {
                this.year--
                this.month = 12
            }
            this.day = this.lengthOfMonth().toShort()
        }
    }

    fun plusMonths(monthsToAdd: Long): ILocalDate {
        if (monthsToAdd == 0L) {
            return this
        }
        val isNegative = monthsToAdd < 0
        val monthCount = year * 12L + (month - 1)
        val calcMonths: Long = monthCount + monthsToAdd // safe overflow

        val newYear = checkValidYear(
            Helper.floorDiv(
                calcMonths,
                12.toLong()
            )
        )
        var newMonth = Helper.floorMod(calcMonths, 12).toInt() + 1

        val newMaxDays = lengthOfMonth(newYear, newMonth)
        var newDay = this.day.toInt()

        if (this.day > newMaxDays) {
            newDay = this.day.toInt() - newMaxDays
            newMonth = if (isNegative) newMonth - 1 else newMonth + 1
        }
        return of(newYear, newMonth, newDay, this.type)
    }

    fun plusYears(yearsToAdd: Long): ILocalDate {
        if (yearsToAdd == 0L) {
            return this
        }

        val newYear = checkValidYear(year+yearsToAdd.toInt())
        val newDay = this.day.toInt().coerceAtMost(lengthOfMonth(newYear, this.month.toInt()))

        return instance(newYear, month.toInt(), newDay)
    }

    fun setDates(year: Int, month: Int, dayOfMonth: Int): ILocalDate {
        val copy = this.copy()
        copy.year = checkValidYear(year)
        copy.month = checkValidMonth(month)
        copy.day = 1
        val lengthOfMonth = copy.lengthOfMonth()

        if (dayOfMonth in 1..lengthOfMonth) {
            copy.day = dayOfMonth.toShort()
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
    private constructor(year: Int, month: Int, dayOfMonth: Int) {
        this.year = year
        this.month = month.toShort()
        this.day = dayOfMonth.toShort()
    }

    private fun checkValidYear(year: Int): Int {
        return year
    }

    private fun checkValidMonth(month: Int): Short {
        return if (month <= 0 || month > 12) 1
        else month.toShort()
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

    private fun instance(year: Int, month: Int, dayOfMonth: Int): ILocalDate {
        return if (this.type == Type.AD) ADLocalDate(year, month, dayOfMonth)
        else BSLocalDate(year, month, dayOfMonth)
    }

    fun copy(): ILocalDate {
        return instance(year, month.toInt(), day.toInt())
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

    fun atStartOfMonth(): ILocalDate = instance(year, month.toInt(), 1)

    fun atStartOfYear(): ILocalDate = instance(year, 1, 1)

    fun atEndOfMonth(): ILocalDate = instance(year, month.toInt(), lengthOfMonth())

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

        fun of(year: Int, month: Int, dayOfMonth: Int, type: Type): ILocalDate {
            return if (type == Type.BS)
                BSLocalDate(year, month, dayOfMonth) else
                ADLocalDate(year, month, dayOfMonth)
        }

        fun convert(date: ILocalDate, type: Type): ILocalDate {
            if (date.type == type) return date

            val iLocalDate = if (type == Type.AD) {
                ADLocalDate.REFERENCE_DATE
            } else {
                BSLocalDate.REFERENCE_DATE
            }
            return iLocalDate.plusDays(date.getDaysSinceReferenceDate())
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

        fun countWeek(date: ILocalDate): Int {
            var startDayDate = date.atStartOfMonth()
            var weekCount = if (startDayDate.dayOfWeek() == 0) 0 else 1
            val currentMonth = startDayDate.month
            val nextMonth = if (currentMonth.toInt() == 12) 1 else currentMonth + 1

            while (true) {
                if (startDayDate.month.toInt() == nextMonth) break
                if (startDayDate.dayOfWeek() == 0) weekCount++
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
     class BSLocalDate : ILocalDate {

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

        override fun minYear() = 1901

        override val type: Type
            get() = Type.BS

        constructor(year: Int, month: Int, dayOfMonth: Int) : super(year, month, dayOfMonth)

        companion object {
            val REFERENCE_DATE = BSLocalDate(2059, 1, 1)
            fun now(): ILocalDate {
                return ADLocalDate.now().reverse()
            }
        }

        private fun yearIndex(): Int {
            val index = (this.year - 1901) % 100
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

        override val type: Type
            get() = Type.AD

        constructor(year: Int, month: Int, dayOfMonth: Int) : super(year, month, dayOfMonth)

        companion object {
            val REFERENCE_DATE = ADLocalDate(2002, 4, 14)
            fun now(): ILocalDate {
                val now = Calendar.getInstance()
                return ADLocalDate(
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
                )
            }
        }
    }
}

object Helper {
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