package com.dd.friction.domain.time

import java.time.ZonedDateTime

fun interface TimeSource {
    fun now(): ZonedDateTime
}
