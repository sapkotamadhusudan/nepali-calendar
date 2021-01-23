package com.maddy.calendar.core

enum class DayOfWeek {
    /**
     * The singleton instance for the day-of-week of Monday.
     * This has the numeric value of `1`.
     */
    MONDAY,

    /**
     * The singleton instance for the day-of-week of Tuesday.
     * This has the numeric value of `2`.
     */
    TUESDAY,

    /**
     * The singleton instance for the day-of-week of Wednesday.
     * This has the numeric value of `3`.
     */
    WEDNESDAY,

    /**
     * The singleton instance for the day-of-week of Thursday.
     * This has the numeric value of `4`.
     */
    THURSDAY,

    /**
     * The singleton instance for the day-of-week of Friday.
     * This has the numeric value of `5`.
     */
    FRIDAY,

    /**
     * The singleton instance for the day-of-week of Saturday.
     * This has the numeric value of `6`.
     */
    SATURDAY,

    /**
     * The singleton instance for the day-of-week of Sunday.
     * This has the numeric value of `7`.
     */
    SUNDAY;


    companion object {
        /**
         * Private cache of all the constants.
         */
        private val ENUMS = values()

        /**
         * Obtains an instance of `DayOfWeek` from an `int` value.
         *
         *
         * `DayOfWeek` is an enum representing the 7 days of the week.
         * This factory allows the enum to be obtained from the `int` value.
         * The `int` value follows the ISO-8601 standard, from 1 (Monday) to 7 (Sunday).
         *
         * @param dayOfWeek  the day-of-week to represent, from 1 (Monday) to 7 (Sunday)
         * @return the day-of-week singleton, not null
         * @throws DateTimeException if the day-of-week is invalid
         */
        fun of(dayOfWeek: Int): DayOfWeek {
            if (dayOfWeek < 1 || dayOfWeek > 7) {
                throw RuntimeException("Invalid value for DayOfWeek: $dayOfWeek")
            }
            return ENUMS[dayOfWeek - 1]
        }

        fun weekDays(firstDayOfWeek: DayOfWeek = SUNDAY): Array<DayOfWeek> {
            var daysOfWeek = ENUMS.copyOf()
            // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
            // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
            if (firstDayOfWeek != MONDAY) {
                val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
                val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
                daysOfWeek = rhs + lhs
            }
            return daysOfWeek
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the day-of-week `int` value.
     *
     *
     * The values are numbered following the ISO-8601 standard, from 1 (Monday) to 7 (Sunday).
     *
     * @return the day-of-week, from 1 (Monday) to 7 (Sunday)
     */
    val value: Int
        get() = ordinal + 1


    /**
     * Returns the day-of-week that is the specified number of days after this one.
     *
     *
     * The calculation rolls around the end of the week from Sunday to Monday.
     * The specified period may be negative.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, positive or negative
     * @return the resulting day-of-week, not null
     */
    open operator fun plus(days: Long): DayOfWeek {
        val amount = (days % 7).toInt()
        return ENUMS[(ordinal + (amount + 7)) % 7]
    }

    /**
     * Returns the day-of-week that is the specified number of days after this one.
     *
     *
     * The calculation rolls around the end of the week from Sunday to Monday.
     * The specified period may be negative.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, positive or negative
     * @return the resulting day-of-week, not null
     */
    open operator fun plus(days: DayOfWeek): DayOfWeek {
        val amount = days.value % 7
        return ENUMS[(ordinal + (amount + 7)) % 7]
    }

    /**
     * Returns the day-of-week that is the specified number of days before this one.
     *
     *
     * The calculation rolls around the start of the year from Monday to Sunday.
     * The specified period may be negative.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, positive or negative
     * @return the resulting day-of-week, not null
     */
    open operator fun minus(days: Long): DayOfWeek {
        return plus(-(days % 7))
    }

    /**
     * Returns the day-of-week that is the specified number of days before this one.
     *
     *
     * The calculation rolls around the start of the year from Monday to Sunday.
     * The specified period may be negative.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, positive or negative
     * @return the resulting day-of-week, not null
     */
    open operator fun minus(days: DayOfWeek): DayOfWeek {
        return plus(-(days.value % 7).toLong())
    }

    fun name(type: ILocalDate.Type = ILocalDate.Type.BS): String {
        return Formatter.weekDayName(this, type, false)
    }
}