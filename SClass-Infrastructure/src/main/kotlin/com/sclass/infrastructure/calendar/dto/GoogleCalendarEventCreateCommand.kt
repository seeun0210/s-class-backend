package com.sclass.infrastructure.calendar.dto

import java.time.ZonedDateTime

data class GoogleCalendarEventCreateCommand(
    val summary: String,
    val startAt: ZonedDateTime,
    val endAt: ZonedDateTime,
    val attendeeEmails: List<String> = emptyList(),
)
