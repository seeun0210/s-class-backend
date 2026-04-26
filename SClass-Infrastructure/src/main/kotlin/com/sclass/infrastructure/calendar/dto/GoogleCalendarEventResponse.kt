package com.sclass.infrastructure.calendar.dto

data class GoogleCalendarEventResponse(
    val id: String?,
    val hangoutLink: String?,
    val conferenceData: GoogleCalendarConferenceResponse?,
)

data class GoogleCalendarConferenceResponse(
    val entryPoints: List<GoogleCalendarEntryPoint> = emptyList(),
)

data class GoogleCalendarEntryPoint(
    val entryPointType: String?,
    val uri: String?,
)
