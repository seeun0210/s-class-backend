package com.sclass.infrastructure.calendar

import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventCreateCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResult
import com.sclass.infrastructure.oauth.client.CentralGoogleAuthorizationCodeClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "google.calendar.central", name = ["enabled"], havingValue = "true")
class CentralGoogleCalendarClient(
    private val authorizationCodeClient: CentralGoogleAuthorizationCodeClient,
    private val googleCalendarClient: GoogleCalendarClient,
    private val properties: GoogleCentralCalendarProperties,
) {
    fun createMeetEventWithRefreshToken(
        refreshToken: String,
        command: GoogleCalendarEventCreateCommand,
    ): GoogleCalendarEventResult {
        val accessToken = authorizationCodeClient.refreshAccessToken(refreshToken)
        return googleCalendarClient.createMeetEventWithAccessToken(
            command = command,
            accessToken = accessToken,
            calendarId = properties.calendarId,
        )
    }
}
