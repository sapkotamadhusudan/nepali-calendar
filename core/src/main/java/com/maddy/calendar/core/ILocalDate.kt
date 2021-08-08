package com.maddy.calendar.core

import java.time.LocalDate
import java.util.*

/**
 * Constructor, previously validated.
 *
 * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
 * @param month  the month-of-year to represent, not null
 * @param dayOfMonth  the day-of-month to represent, valid for year-month, from 1 to 31
 *
 * Created by Madhusudan Sapkota on 8/12/20.
 */
abstract class ILocalDate private constructor(year: Int, month: Month, dayOfMonth: Int) {

    enum class Type {
        AD, BS
    }

    /**
     * The year.
     */
    var year: Int = year
        protected set

    /**
     * The month-of-year.
     */
    var month: Month = month
        private set


    /**
     * The day-of-month.
     */
    private var day: Int = dayOfMonth

    /**
     * The type of calendar.
     */
    abstract val type: Type


    /**
     * Gets the day-of-month field.
     *
     *
     * This method returns the primitive `int` value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    val dayOfMonth: Int
        get() = day

    /**
     * Gets the day-of-year field.
     *
     *
     * This method returns the primitive `int` value for the day-of-year.
     *
     * @return the day-of-year, from 1 to 365, or 366 in a leap year
     */
    val dayOfYear: Int
        get() = firstDayOfYear() + day - 1


    /**
     * Gets the month-of-year field from 1 to 12.
     *
     *
     * This method returns the month as an `int` from 1 to 12.
     * Application code is frequently clearer if the enum [Month]
     * is used by calling [.getMonth].
     *
     * @return the month-of-year, from 1 to 12
     */
    val monthValue: Int
        get() = month.value

    /**
     * Gets the name of month-of-year.
     *
     *
     * This method returns the month name as an `String`.
     *
     * @return the name of month-of-year
     */
    val monthName: String
        get() = Formatter.format(this, "MM")


    /**
     * Returns the length of the year.
     * <p>
     * This returns the length of the year in days, either 365 or 366.
     *
     * @return 366 if the year is leap, 365 otherwise
     */
    val lengthOfYear: Int
        get() = Utils.lengthOfYear(year, this.type)


    /**
     * Returns the length of the month, taking account of the year.
     * <p>
     * This returns the length of the month in days.
     * For example, a date in January would return 31.
     *
     * @return the length of the month in days, from 28 to 31
     */
    val lengthOfMonth: Int
        get() = Utils.lengthOfMonth(this.year, this.month, this.type)


    /**
     * Returns the day of week, taking account of the weekOfMonth.
     * <p>
     * This returns the day of week of current day.
     * For example, a day placed in Sunday would return 0.
     *
     * @return the length of the month in days, from 0 to 6
     */
    val dayOfWeek: DayOfWeek
        get() {
            val firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
            val daysSinceReferenceDate = this.daysSinceReferenceDate

            val weekOfMonthValue = firstDayOfWeek.value - 1
            val dayOfWeek = (if (daysSinceReferenceDate > 0) {
                ((daysSinceReferenceDate % 7) + weekOfMonthValue) % 7
            } else {
                (((7 - ((daysSinceReferenceDate * -1) % 7)) % 7) + weekOfMonthValue) % 7
            }).toInt()
            return DayOfWeek.of(dayOfWeek + 1)
        }

    /**
     * Returns the start week of month, taking account of the weekOfMonth.
     * <p>
     * This returns the start weekDay of current month.
     * For example, a month started with Sunday would return 0.
     *
     * @return the starting day of month
     */
    fun startDayOfWeek(): DayOfWeek {
        return this.atStartOfMonth().dayOfWeek
    }

    /**
     * Returns a copy of this {@code ILocalDate} with the specified number of days added.
     * <p>
     * This method adds the specified amount to the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one day would result in 2009-01-01.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param daysToAdd  the days to add, may be negative
     * @return a {@code ILocalDate} based on this date with the days added
     */
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
                monthLen = lengthOfMonth.toLong()
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

    /**
     * Returns a copy of this {@code ILocalDate} with the specified number of days subtracted.
     * <p>
     * This method subtracts the specified amount from the days field decrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2009-01-01 minus one day would result in 2008-12-31.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param daysToSubtract  the days to subtract, may be negative
     * @return a {@code ILocalDate} based on this date with the days subtracted
     */
    fun minusDays(daysToSubtract: Long): ILocalDate {
        return plusDays(-daysToSubtract)
    }

    /**
     * Returns a copy of this {@code ILocalDate} with the specified number of months added.
     * <p>
     * This method adds the specified amount to the months field in three steps:
     * <ol>
     * <li>Add the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2007-03-31 plus one month would result in the invalid date
     * 2007-04-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-04-30, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthsToAdd  the months to add, may be negative
     * @return a {@code ILocalDate} based on this date with the months added
     */
    fun plusMonths(monthsToAdd: Long): ILocalDate {
        if (monthsToAdd == 0L) {
            return this
        }
        val isNegative = monthsToAdd < 0
        val monthCount = year * 12L + (monthValue - 1)
        val calcMonths: Long = monthCount + monthsToAdd // safe overflow

        val newYear = checkValidYear(
            Math.floorDiv(
                calcMonths,
                12.toLong()
            )
        )
        var newMonth = Math.floorMod(calcMonths, 12).toInt() + 1

        val newMaxDays = Utils.lengthOfMonth(newYear, checkValidMonth(newMonth), this.type)
        var newDay = this.day

        if (this.day > newMaxDays) {
            newDay = this.day - newMaxDays
            newMonth = if (isNegative) newMonth - 1 else newMonth + 1
        }
        return of(newYear, newMonth, newDay, this.type)
    }

    /**
     * Returns a copy of this {@code ILocalDate} with the specified number of months subtracted.
     * <p>
     * This method subtracts the specified amount from the months field in three steps:
     * <ol>
     * <li>Subtract the input months from the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2007-03-31 minus one month would result in the invalid date
     * 2007-02-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-02-28, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthsToSubtract  the months to subtract, may be negative
     * @return a {@code ILocalDate} based on this date with the months subtracted
     */
    fun minusMonths(monthsToSubtract: Long): ILocalDate {
        return plusMonths(-monthsToSubtract)
    }

    /**
     * Returns a copy of this {@code ILocalDate} with the specified number of years added.
     * <p>
     * This method adds the specified amount to the years field in three steps:
     * <ol>
     * <li>Add the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2008-02-29 (leap year) plus one year would result in the
     * invalid date 2009-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2009-02-28, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToAdd  the years to add, may be negative
     * @return a {@code ILocalDate} based on this date with the years added
     */
    fun plusYears(yearsToAdd: Long): ILocalDate {
        if (yearsToAdd == 0L) {
            return this
        }

        val newYear = checkValidYear(year + yearsToAdd.toInt())
        val newDay = this.day.coerceAtMost(Utils.lengthOfMonth(newYear, this.month, this.type))

        return instance(newYear, month, newDay)
    }

    /**
     * Returns a copy of this {@code ILocalDate} with the specified number of years subtracted.
     * <p>
     * This method subtracts the specified amount from the years field in three steps:
     * <ol>
     * <li>Subtract the input years from the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 2008-02-29 (leap year) minus one year would result in the
     * invalid date 2007-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2007-02-28, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToSubtract  the years to subtract, may be negative
     * @return a {@code ILocalDate} based on this date with the years subtracted
     */
    fun minusYear(yearsToSubtract: Long): ILocalDate {
        return plusYears(-yearsToSubtract)
    }

    fun reverse(): ILocalDate {
        return convert(this, if (this.type == Type.BS) Type.AD else Type.BS)
    }

    fun atDay(dayOfMonth: Int): ILocalDate {
        if (dayOfMonth < 1 || dayOfMonth > lengthOfMonth) {
            throw RuntimeException("Invalid value for DayOfMonth: $dayOfMonth")
        }
        return instance(year, month, dayOfMonth)
    }

    fun atStartOfMonth(): ILocalDate = instance(year, month, 1)

    fun atStartOfYear(): ILocalDate = instance(year, Month.JANUARY_BAISHAK, 1)

    fun atEndOfMonth(): ILocalDate = instance(year, month, lengthOfMonth)

    /**
     * Checks if this date is before the specified date.
     * <p>
     * This checks to see if this date represents a point on the
     * local time-line before the other date.
     * <pre>
     *   val a = ILocalDate.ofBS(2077, 6, 20)
     *   val b = ILocalDate.ofBS(2077, 7, 1)
     *   a.before(b) == true
     *   a.before(a) == false
     *   b.before(a) == false
     * </pre>
     * <p>
     * This method only considers the position of the two dates on the local time-line.
     * It does not take into account the chronology, or calendar system.
     * This is different from the comparison in {@link #compareTo(ChronoLocalDate)},
     * but is the same approach as {@link ChronoLocalDate#timeLineOrder()}.
     *
     * @param other  the other date to compare to, not null
     * @return true if this date is before the specified date
     */
    fun before(other: ILocalDate): Boolean {
        return year < other.year ||
                year <= other.year &&
                (month < other.month ||
                        (month <= other.month && day < other.day))
    }

    /**
     * Checks if this date is after the specified date.
     * <p>
     * This checks to see if this date represents a point on the
     * local time-line after the other date.
     * <pre>
     *   val a = ILocalDate.ofBS(2077, 6, 20)
     *   val b = ILocalDate.ofBS(2077, 7, 1)
     *   a.after(b) == false
     *   a.after(a) == false
     *   b.after(a) == true
     * </pre>
     * <p>
     *
     * @param other  the other date to compare to, not null
     * @return true if this date is after the specified date
     */
    fun after(other: ILocalDate): Boolean {
        return year > other.year ||
                year >= other.year &&
                (month > other.month ||
                        month >= other.month &&
                        (day > other.day))
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
    operator fun compareTo(other: ILocalDate): Int {
        var cmp = if (this.type == other.type) 0 else compareTo(other.reverse())
        if (cmp == 0) {
            cmp = year - other.year
            if (cmp == 0) {
                cmp = monthValue - other.monthValue
                if (cmp == 0) {
                    cmp = day - other.day
                }
            }
        }
        return cmp
    }

    /**
     * Checks if this date is equal to another date.
     * <p>
     * Compares this {@code ILocalDate} with another ensuring that the date is the same.
     * <p>
     * Only objects of type {@code ILocalDate} are compared, other types return false.
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other date
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is ILocalDate) {
            return compareTo(other) == 0
        }
        return false
    }

    /**
     * A hash code for this date.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        val yearValue = year
        val monthValue = monthValue
        val dayValue = day
        return yearValue and -0x800 xor (yearValue shl 11) + (monthValue shl 6) + dayValue
    }

    /**
     * Outputs this date as a {@code String}, such as {@code 2007-12-03 (BS)}.
     * <p>
     * The output will be in the ISO-8601 format {@code uuuu-MM-dd}.
     *
     * @return a string representation of this date, not null
     */
    override fun toString(): String {
        return "$year-$month-$day ($type)"
    }

    fun copy(): ILocalDate {
        return instance(year, month, day)
    }

    protected abstract fun maxYear(): Int
    protected abstract fun minYear(): Int
    protected abstract fun firstDayOfYear(): Int
    protected abstract fun referenceDate(): ILocalDate

    private fun addSingleDay() {
        this.day++
        if (this.day > this.lengthOfMonth) {
            this.day = 1
            val newMonth = this.month.plus(1)
            if (newMonth == Month.JANUARY_BAISHAK) {
                this.year++
            }
            this.month = newMonth
        }
    }

    private fun subtractSingleDay() {
        this.day--
        if (this.day < 1) {
            val newMonth = this.month.minus(1)
            if (newMonth == Month.DECEMBER_CHAITRA) {
                this.year--
            }
            this.month = newMonth
            this.day = this.lengthOfMonth
        }
    }

    private val daysSinceReferenceDate: Long
        get() = Period.daysBetween(this.referenceDate(), this)

    private fun checkValidYear(year: Int): Int {
        return year
    }

    private fun checkValidMonth(month: Int): Month {
        return if (month < Month.JANUARY_BAISHAK.value || month > Month.DECEMBER_CHAITRA.value) Month.JANUARY_BAISHAK
        else Month.of(month)
    }

    private fun instance(year: Int, month: Month, dayOfMonth: Int): ILocalDate {
        return if (this.type == Type.AD) ADLocalDate(year, month, dayOfMonth)
        else BSLocalDate(year, month, dayOfMonth)
    }

    companion object {

        fun nowBS(): ILocalDate {
            return BSLocalDate.now()
        }

        fun nowAD(): ILocalDate {
            return ADLocalDate.now()
        }

        fun now(type: Type): ILocalDate {
            return if (type == Type.AD) nowAD() else nowBS()
        }

        /**
         * Obtains an instance of {@code ILocalDate} from a year, month and day.
         * <p>
         * This returns a {@code ILocalDate} with the specified year, month and day-of-month.
         * The day must be valid for the year and month, otherwise an exception will be thrown.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @param type the type of calendar to represent by AD or BS
         * @return the local date, not null
         */
        fun of(year: Int, month: Int = 1, dayOfMonth: Int = 1, type: Type = Type.BS): ILocalDate {
            return if (type == Type.BS) ofBS(year, month, dayOfMonth)
            else ofAD(year, month, dayOfMonth)
        }

        fun ofBS(year: Int, month: Int = 1, dayOfMonth: Int = 1): ILocalDate {
            return BSLocalDate(year, Month.of(month), dayOfMonth)
        }

        fun ofAD(year: Int, month: Int = 1, dayOfMonth: Int = 1): ILocalDate {
            return ADLocalDate(year, Month.of(month), dayOfMonth)
        }

        fun of(date: Date, type: Type = Type.AD): ILocalDate {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date.time
            return of(calendar, type)
        }

        fun of(date: LocalDate, type: Type = Type.AD): ILocalDate {
            return when (type) {
                Type.AD -> ofAD(date.year, date.monthValue, date.dayOfMonth)
                Type.BS -> of(date).reverse()
            }
        }

        fun of(calendar: Calendar, type: Type = Type.AD): ILocalDate {
            return when (type) {
                Type.AD -> ofAD(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DATE)
                )
                Type.BS -> of(calendar).reverse()
            }
        }

        fun convert(date: ILocalDate, type: Type): ILocalDate {
            if (date.type == type) return date

            val iLocalDate = if (type == Type.AD) {
                ADLocalDate.REFERENCE_DATE
            } else {
                BSLocalDate.REFERENCE_DATE
            }
            val daysToAdd = date.daysSinceReferenceDate
            return iLocalDate.plusDays(daysToAdd)
        }

        fun convertToBS(year: Int, month: Int, dayOfMonth: Int): ILocalDate {
            return of(year, month, dayOfMonth, Type.AD).reverse()
        }
    }

    internal object Utils {

        fun isLeapYear(year: Int, type: Type): Boolean {
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

        fun monthDaysBS(year: Int): IntArray {
            return BSLocalDate.monthDays(year)
        }

        fun monthDaysAD(): IntArray {
            return ADLocalDate.monthDays
        }

        fun lengthOfYear(year: Int, type: Type): Int {
            val leap = if (isLeapYear(year, type)) 1 else 0
            val monthDays = if (type == Type.BS) monthDaysBS(year) else monthDaysAD()
            return monthDays.sum() + leap
        }

        fun lengthOfMonth(year: Int, month: Month, type: Type): Int {
            return if (isLeapYear(year, type) && month == Month.FEBRUARY_JESTHA) {
                29
            } else {
                val monthDays = if (type == Type.BS) monthDaysBS(year) else monthDaysAD()
                monthDays[month.value - 1]
            }
        }

        @Deprecated("Do not use")
        fun daysDifference(from: ILocalDate, to: ILocalDate): Long {
            val (isNegative, startEnd) = Period.startEndType(from, to)
            val (fromDate, toDate) = startEnd

            var daysCount = 0L
            while (fromDate.before(toDate)) {
                fromDate.addSingleDay()
                daysCount++
            }

            return daysCount * isNegative
        }

        @Deprecated("Do not use")
        fun countWeek(date: ILocalDate, firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): Int {
            val startDayDate = date.atStartOfMonth()
            var weekCount = if (startDayDate.dayOfWeek == firstDayOfWeek) 0 else 1
            val currentMonth = startDayDate.month
            val nextMonth = if (currentMonth == Month.DECEMBER_CHAITRA) 1 else currentMonth + 1

            while (true) {
                if (startDayDate.month == nextMonth) break
                if (startDayDate.dayOfWeek == firstDayOfWeek) weekCount++
                startDayDate.addSingleDay()
            }
            return weekCount
        }

        @Deprecated("Do not use")
        fun weekOfMonth(date: ILocalDate, firstDayOfWeek: DayOfWeek): Int {
            val startDayDate = date.atStartOfMonth()
            var weekCount = if (startDayDate.dayOfWeek == firstDayOfWeek) 0 else 1
            val currentMonth = startDayDate.month
            val nextMonth = if (currentMonth == Month.DECEMBER_CHAITRA) 1 else currentMonth + 1

            while (true) {
                if (startDayDate.month == nextMonth) break

                val dayOfWeek = startDayDate.dayOfWeek
                if (dayOfWeek == firstDayOfWeek) weekCount++

                if (startDayDate.year == date.year
                    && startDayDate.month == startDayDate.month
                    && startDayDate.day == date.day
                    && startDayDate.type == date.type
                ) return weekCount

                startDayDate.addSingleDay()
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
    private class BSLocalDate
    constructor(year: Int, month: Month, dayOfMonth: Int) :
        ILocalDate(year, month, dayOfMonth) {

        override fun referenceDate() = REFERENCE_DATE

        override fun maxYear() = 2200

        override fun minYear() = 1999

        override fun firstDayOfYear(): Int {
            return daysSum(this.month)
        }

        private fun daysSum(month: Month): Int {
            val monthDays = monthDays(this.year)
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
                Month.DECEMBER_CHAITRA -> daysSum(month - 1) + monthDays[month.value - 2]
            }
        }

        override val type: Type
            get() = Type.BS

        companion object {
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

            val REFERENCE_DATE = BSLocalDate(2059, Month.JANUARY_BAISHAK, 1)
            fun now(): ILocalDate {
                return ADLocalDate.now().reverse()
            }

            fun monthDays(year: Int): IntArray = yearMonthSpanLookupTable[yearIndex(year)]

            fun yearIndex(year: Int): Int {
                val index = (year - 1999) % 100
                return if (index < 0) index * -1 else index
            }
        }
    }

    /**
     * Constructor, previously validated.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, valid for year-month, from 1 to 31
     */
    private class ADLocalDate(year: Int, month: Month, dayOfMonth: Int) :
        ILocalDate(year, month, dayOfMonth) {

        override fun minYear() = -999999999

        override fun maxYear() = 999999999

        override fun referenceDate() = REFERENCE_DATE

        override fun firstDayOfYear(): Int {
            val leap = if (Utils.isLeapYear(this.year, this.type)) 1 else 0
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
            }
        }

        override val type: Type
            get() = Type.AD

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

            val monthDays = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        }
    }
}