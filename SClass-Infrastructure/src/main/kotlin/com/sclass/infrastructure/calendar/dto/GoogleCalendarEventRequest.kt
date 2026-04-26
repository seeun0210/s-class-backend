package com.sclass.infrastructure.calendar.dto

data class GoogleCalendarEventRequest(
    val summary: String,
    val start: GoogleCalendarEventDateTime,
    val end: GoogleCalendarEventDateTime,
    val conferenceData: GoogleCalendarConferenceData,
)

data class GoogleCalendarEventDateTime(
    val dateTime: String,
    val timeZone: String,
)

data class GoogleCalendarConferenceData(
    val createRequest: GoogleCalendarConferenceCreateRequest,
)

data class GoogleCalendarConferenceCreateRequest(
    val requestId: String,
    val conferenceSolutionKey: GoogleCalendarConferenceSolutionKey =
        GoogleCalendarConferenceSolutionKey(type = "hangoutsMeet"),
)

data class GoogleCalendarConferenceSolutionKey(
    val type: String,
)
