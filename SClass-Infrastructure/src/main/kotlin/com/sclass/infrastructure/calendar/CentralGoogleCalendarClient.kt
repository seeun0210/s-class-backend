package com.sclass.infrastructure.calendar

import com.sclass.common.exception.GoogleCalendarUnauthorizedException
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventCreateCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResult
import com.sclass.infrastructure.oauth.client.CentralGoogleAuthorizationCodeClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException

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
        return try {
            googleCalendarClient.createMeetEventWithAccessToken(
                command = command,
                accessToken = accessToken,
                calendarId = properties.calendarId,
            )
        } catch (e: WebClientResponseException) {
            if (e.isAuthorizationFailure()) throw GoogleCalendarUnauthorizedException()
            throw e
        }
    }

    fun updateMeetEventWithRefreshToken(
        refreshToken: String,
        eventId: String,
        command: GoogleCalendarEventCreateCommand,
    ): GoogleCalendarEventResult {
        val accessToken = authorizationCodeClient.refreshAccessToken(refreshToken)
        return try {
            googleCalendarClient.updateMeetEventWithAccessToken(
                eventId = eventId,
                command = command,
                accessToken = accessToken,
                calendarId = properties.calendarId,
            )
        } catch (e: WebClientResponseException) {
            if (e.isAuthorizationFailure()) throw GoogleCalendarUnauthorizedException()
            throw e
        }
    }

    fun deleteMeetEventWithRefreshToken(
        refreshToken: String,
        eventId: String,
    ) {
        val accessToken = authorizationCodeClient.refreshAccessToken(refreshToken)
        try {
            googleCalendarClient.deleteMeetEventWithAccessToken(
                eventId = eventId,
                accessToken = accessToken,
                calendarId = properties.calendarId,
            )
        } catch (e: WebClientResponseException) {
            if (e.isAuthorizationFailure()) throw GoogleCalendarUnauthorizedException()
            throw e
        }
    }

    private fun WebClientResponseException.isAuthorizationFailure(): Boolean =
        statusCode.value() == UNAUTHORIZED_STATUS || statusCode.value() == FORBIDDEN_STATUS

    private companion object {
        const val UNAUTHORIZED_STATUS = 401
        const val FORBIDDEN_STATUS = 403
    }
}
