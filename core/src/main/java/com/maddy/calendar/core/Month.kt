package com.maddy.calendar.core

import java.time.DateTimeException

/**
 * This enum contains literal names of AD/BS month names
 */
enum class Month {
    /**
     * The singleton instance for the month of January with 31 days.
     * This has the numeric value of `1`.
     */
    JANUARY_BAISHAK,

    /**
     * The singleton instance for the month of February with 28 days, or 29 in a leap year.
     * This has the numeric value of `2`.
     */
    FEBRUARY_JESTHA,

    /**
     * The singleton instance for the month of March with 31 days.
     * This has the numeric value of `3`.
     */
    MARCH_ASADH,

    /**
     * The singleton instance for the month of April with 30 days.
     * This has the numeric value of `4`.
     */
    APRIL_SHRWAN,

    /**
     * The singleton instance for the month of May with 31 days.
     * This has the numeric value of `5`.
     */
    MAY_BHADRA,

    /**
     * The singleton instance for the month of June with 30 days.
     * This has the numeric value of `6`.
     */
    JUNE_ASHWIN,

    /**
     * The singleton instance for the month of July with 31 days.
     * This has the numeric value of `7`.
     */
    JULY_KARTIK,

    /**
     * The singleton instance for the month of August with 31 days.
     * This has the numeric value of `8`.
     */
    AUGUST_MANGSIR,

    /**
     * The singleton instance for the month of September with 30 days.
     * This has the numeric value of `9`.
     */
    SEPTEMBER_PAUSH,

    /**
     * The singleton instance for the month of October with 31 days.
     * This has the numeric value of `10`.
     */
    OCTOBER_MAGH,

    /**
     * The singleton instance for the month of November with 30 days.
     * This has the numeric value of `11`.
     */
    NOVEMBER_FALGUN,

    /**
     * The singleton instance for the month of December with 31 days.
     * This has the numeric value of `12`.
     */
    DECEMBER_CHAITRA;


    companion object {

        /**
         * Private cache of all the constants.
         */
        private val ENUMS = values()

        /**
         * Obtains an instance of `Month` from an `int` value.
         *
         *
         * `Month` is an enum representing the 12 months of the year.
         * This factory allows the enum to be obtained from the `int` value.
         * The `int` value follows the ISO-8601 standard, from 1 (January) to 12 (December).
         *
         * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
         * @return the month-of-year, not null
         * @throws DateTimeException if the month-of-year is invalid
         */
        fun of(month: Int): Month {
            if (month < 1 || month > 12) {
                throw RuntimeException("Invalid value for MonthOfYear: $month")
            }
            return ENUMS[month - 1]
        }
    }

    /**
     * Gets the month-of-year `int` value.
     *
     *
     * The values are numbered following the ISO-8601 standard,
     * from 1 (January) to 12 (December).
     *
     * @return the month-of-year, from 1 (January) to 12 (December)
     */
    val value: Int
        get() = ordinal + 1


    /**
     * Returns the month-of-year that is the specified number of months after this one.
     *
     *
     * The calculation rolls around the end of the year from December to January.
     * The specified period may be negative.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, positive or negative
     * @return the resulting month, not null
     */
    open operator fun plus(months: Int): Month {
        val amount = (months % 12)
        return ENUMS[(ordinal + (amount + 12)) % 12]
    }

    open operator fun plus(months: Month): Month {
        return ENUMS[(ordinal + (months.value + 12)) % 12]
    }

    /**
     * Returns the month-of-year that is the specified number of months before this one.
     *
     *
     * The calculation rolls around the start of the year from January to December.
     * The specified period may be negative.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, positive or negative
     * @return the resulting month, not null
     */
    open operator fun minus(months: Int): Month {
        return plus(-(months % 12))
    }

    open operator fun minus(months: Month): Month {
        return plus(-months.value)
    }
}