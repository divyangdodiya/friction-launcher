package com.dd.friction.data.time

import com.dd.friction.domain.time.TimeSource
import java.time.ZonedDateTime
import javax.inject.Inject

/** Reads the zone on every call so time zone changes show up immediately. */
class SystemTimeSource @Inject constructor() : TimeSource {
    override fun now(): ZonedDateTime = ZonedDateTime.now()
}
