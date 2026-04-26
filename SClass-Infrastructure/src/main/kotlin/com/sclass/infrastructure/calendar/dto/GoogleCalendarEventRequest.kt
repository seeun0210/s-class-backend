package com.sclass.infrastructure.calendar.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GoogleCalendarEventRequest(
    val summary: String,
    val start: GoogleCalendarEventDateTime,
    val end: GoogleCalendarEventDateTime,
    val conferenceData: GoogleCalendarConferenceData? = null,
    val attendees: List<GoogleCalendarAttendee> = emptyList(),
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

data class GoogleCalendarAttendee(
    val email: String,
)
