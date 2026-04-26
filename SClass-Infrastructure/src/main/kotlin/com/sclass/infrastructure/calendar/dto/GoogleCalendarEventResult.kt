package com.sclass.infrastructure.calendar.dto

data class GoogleCalendarEventResult(
    val eventId: String,
    val meetJoinUrl: String,
    val meetCode: String? = null,
)
