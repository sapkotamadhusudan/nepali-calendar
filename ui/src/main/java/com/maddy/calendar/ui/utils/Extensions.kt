package com.maddy.calendar.ui.utils

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.maddy.calendar.ui.model.LocalDate
import com.maddy.calendar.ui.model.YearMonth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

internal fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

internal fun Boolean?.orFalse(): Boolean = this ?: false

internal fun Int?.orZero(): Int = this ?: 0

val LocalDate.yearMonth: YearMonth
    get() = YearMonth.of(this.year, this.monthValue)

val YearMonth.next: YearMonth
    get() = this.plusMonths(1)

val YearMonth.previous: YearMonth
    get() = this.minusMonths(1)

internal const val NO_INDEX = -1

internal val Rect.namedString: String
    get() = "[L: $left, T: $top][R: $right, B: $bottom]"

internal val CoroutineScope.job: Job
    get() = requireNotNull(coroutineContext[Job])

