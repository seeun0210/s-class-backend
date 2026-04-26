package com.sclass.infrastructure.calendar.dto

import java.time.ZonedDateTime

data class CreateGoogleCalendarEventCommand(
    val accessToken: String,
    val refreshToken: String,
    val summary: String,
    val startAt: ZonedDateTime,
    val endAt: ZonedDateTime,
)
