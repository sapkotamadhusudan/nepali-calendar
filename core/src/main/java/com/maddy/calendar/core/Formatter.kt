package com.maddy.calendar.core

object Formatter {

    private val npNumberChars = mapOf(
        1 to '१',
        2 to '२',
        3 to '३',
        4 to '४',
        5 to '५',
        6 to '६',
        7 to '७',
        8 to '८',
        9 to '९',
        0 to '०'
    )


    private val bsMonths = mapOf(
        1 to "January",
        2 to "February",
        3 to "March",
        4 to "April",
        5 to "May",
        6 to "June",
        7 to "July",
        8 to "August",
        9 to "September",
        10 to "October",
        11 to "November",
        12 to "December",
    )

    private val npMonths = mapOf(
        1 to "बैशाख",
        2 to "जेठ",
        3 to "असार",
        4 to "श्रावण",
        5 to "भदौ",
        6 to "आश्विन",
        7 to "कार्तिक",
        8 to "मंसिर",
        9 to "पुष",
        10 to "माघ",
        11 to "फाल्गुन",
        12 to "चैत्र"
    )

    private val npWeekDays = mapOf(
        DayOfWeek.SUNDAY to "आइतबार",
        DayOfWeek.MONDAY to "सोमबार",
        DayOfWeek.TUESDAY to "मंगलबार",
        DayOfWeek.WEDNESDAY to "बुधबार",
        DayOfWeek.THURSDAY to "बिहिबार",
        DayOfWeek.FRIDAY to "शुक्रबार",
        DayOfWeek.SATURDAY to "शनिबार"
    )

    private fun monthName(month: Month, type: ILocalDate.Type, short: Boolean = true): String {
        return (
                if (type == ILocalDate.Type.BS) {
                    npMonths[month.value]
                } else {
                    if (short) bsMonths[month.value]?.substring(0, 3)
                    else bsMonths[month.value]
                }) ?: ""
    }

    fun weekDayName(dayOfWeek: DayOfWeek, type: ILocalDate.Type, short: Boolean = true): String {
        val name = ((if (type == ILocalDate.Type.BS) npWeekDays[dayOfWeek] else dayOfWeek.name))
        return (if (short) name?.substring(0, 3) else name) ?: ""
    }

    private fun getNpCharacter(number: Long, prefix: String = ""): String {
        if (number >= 10) {
            return getNpCharacter(number / 10, getNpCharacter((number % 10)) + prefix)
        }
        return npNumberChars[number.toInt()].toString() + prefix
    }

    private fun dayName(day: Int, type: ILocalDate.Type): String {
        return if (type == ILocalDate.Type.AD) day.toString().padStart(2, '0')
        else getNpCharacter(day.toLong()).padStart(2, npNumberChars[0] ?: '0')
    }

    private fun yearName(year: Int, type: ILocalDate.Type): String {
        return if (type == ILocalDate.Type.AD) year.toString()
        else getNpCharacter(year.toLong())
    }


    fun format(date: ILocalDate, pattern: String): String {
        return pattern.replace(
            Regex("yyyy|MMMM|MMM|MM|EEEE|EEE|dd|d|hh|HH|mm|ss|a")
        ) { matched ->
            when (matched.value) {
                "yyyy" -> yearName(date.year, date.type)
                "MMMM" -> monthName(date.month, date.type, false)
                "MMM" -> monthName(date.month, date.type, true)
                "MM" -> getNpCharacter(date.monthValue.toLong()).padStart(2, npNumberChars[1] ?: ' ')
                "EEEE" -> weekDayName(date.dayOfWeek, date.type, false)
                "EEE" -> weekDayName(date.dayOfWeek, date.type, true)
                "dd" -> dayName(date.dayOfMonth, date.type)
                "d" -> dayName(date.dayOfMonth, date.type)
                else -> ""
            }
        }
    }
}