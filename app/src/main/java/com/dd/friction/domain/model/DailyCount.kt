package com.dd.friction.domain.model

import java.time.LocalDate

/** How many distraction apps were opened on [date]. */
data class DailyCount(val date: LocalDate, val count: Int) {

    /** The count for [day]; a stored count from an earlier day means nothing was opened yet. */
    fun countOn(day: LocalDate): Int = if (day == date) count else 0

    fun increment(day: LocalDate): DailyCount =
        if (day == date) copy(count = count + 1) else DailyCount(day, 1)
}
