package com.sclass.infrastructure.calendar

import com.fasterxml.jackson.databind.ObjectMapper
import com.sclass.common.exception.GoogleCalendarRequestFailedException
import com.sclass.common.exception.GoogleCalendarUnauthorizedException
import com.sclass.common.exception.GoogleOAuthProviderUnavailableException
import com.sclass.infrastructure.calendar.dto.CreateGoogleCalendarEventCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarConferenceCreateRequest
import com.sclass.infrastructure.calendar.dto.GoogleCalendarConferenceData
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventDateTime
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventRequest
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResponse
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResult
import com.sclass.infrastructure.oauth.client.GoogleAuthorizationCodeClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.UUID

@Component
class GoogleCalendarClient(
    @param:Qualifier("oAuthWebClient") private val webClient: WebClient,
    private val authorizationCodeClient: GoogleAuthorizationCodeClient,
) {
    private val log = LoggerFactory.getLogger(GoogleCalendarClient::class.java)
    private val objectMapper = ObjectMapper()

    fun createMeetEvent(command: CreateGoogleCalendarEventCommand): GoogleCalendarEventResult =
        try {
            createMeetEvent(command, command.accessToken)
        } catch (e: WebClientResponseException) {
            if (!e.isAuthorizationFailure()) throw e

            val refreshedAccessToken =
                runCatching {
                    authorizationCodeClient.refreshAccessToken(command.refreshToken)
                }.getOrElse {
                    if (it is GoogleOAuthProviderUnavailableException) throw it
                    throw GoogleCalendarUnauthorizedException()
                }

            try {
                createMeetEvent(command, refreshedAccessToken)
            } catch (retryException: WebClientResponseException) {
                if (retryException.isAuthorizationFailure()) throw GoogleCalendarUnauthorizedException()
                throw retryException
            }
        }

    private fun createMeetEvent(
        command: CreateGoogleCalendarEventCommand,
        accessToken: String,
    ): GoogleCalendarEventResult {
        val request = command.toRequest()
        val response =
            try {
                webClient
                    .post()
                    .uri(CALENDAR_EVENTS_URI)
                    .header("Authorization", "Bearer $accessToken")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GoogleCalendarEventResponse::class.java)
                    .block()
            } catch (e: WebClientResponseException) {
                log.warn("Google Calendar event create failed: ${e.responseBodyAsString}", e)
                if (e.isRetryableProviderFailure()) {
                    throw GoogleOAuthProviderUnavailableException()
                }
                if (e.isAuthorizationFailure()) throw e
                throw GoogleCalendarRequestFailedException()
            } catch (e: Exception) {
                log.warn("Google Calendar event create failed", e)
                throw GoogleOAuthProviderUnavailableException()
            }

        return response.toResult()
    }

    private fun CreateGoogleCalendarEventCommand.toRequest(): GoogleCalendarEventRequest =
        GoogleCalendarEventRequest(
            summary = summary,
            start =
                GoogleCalendarEventDateTime(
                    dateTime = startAt.toOffsetDateTime().toString(),
                    timeZone = startAt.zone.id,
                ),
            end =
                GoogleCalendarEventDateTime(
                    dateTime = endAt.toOffsetDateTime().toString(),
                    timeZone = endAt.zone.id,
                ),
            conferenceData =
                GoogleCalendarConferenceData(
                    createRequest =
                        GoogleCalendarConferenceCreateRequest(
                            requestId = UUID.randomUUID().toString(),
                        ),
                ),
        )

    private fun GoogleCalendarEventResponse?.toResult(): GoogleCalendarEventResult {
        val eventId = this?.id ?: throw GoogleCalendarRequestFailedException()
        val meetJoinUrl =
            conferenceData
                ?.entryPoints
                ?.firstOrNull { it.entryPointType == "video" && !it.uri.isNullOrBlank() }
                ?.uri
                ?: hangoutLink

        if (meetJoinUrl.isNullOrBlank()) throw GoogleCalendarRequestFailedException()

        return GoogleCalendarEventResult(
            eventId = eventId,
            meetJoinUrl = meetJoinUrl,
        )
    }

    private fun WebClientResponseException.isAuthorizationFailure(): Boolean =
        statusCode.value() == UNAUTHORIZED_STATUS || statusCode.value() == FORBIDDEN_STATUS

    private fun WebClientResponseException.isRetryableProviderFailure(): Boolean =
        statusCode.is5xxServerError ||
            statusCode.value() == TOO_MANY_REQUESTS_STATUS ||
            isForbiddenRateLimitFailure()

    private fun WebClientResponseException.isForbiddenRateLimitFailure(): Boolean {
        if (statusCode.value() != FORBIDDEN_STATUS) return false

        return runCatching {
            val error = objectMapper.readTree(responseBodyAsString).path("error")
            val status = error.path("status").asText()
            val reasons =
                error
                    .path("errors")
                    .mapNotNull { it.path("reason").asText(null) }

            status == GOOGLE_RESOURCE_EXHAUSTED_STATUS ||
                reasons.any { it in GOOGLE_CALENDAR_RETRYABLE_FORBIDDEN_REASONS }
        }.getOrDefault(false)
    }

    private companion object {
        const val CALENDAR_EVENTS_URI =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events?conferenceDataVersion=1"
        const val UNAUTHORIZED_STATUS = 401
        const val FORBIDDEN_STATUS = 403
        const val TOO_MANY_REQUESTS_STATUS = 429
        const val GOOGLE_RESOURCE_EXHAUSTED_STATUS = "RESOURCE_EXHAUSTED"
        val GOOGLE_CALENDAR_RETRYABLE_FORBIDDEN_REASONS =
            setOf(
                "dailyLimitExceeded",
                "quotaExceeded",
                "rateLimitExceeded",
                "userRateLimitExceeded",
            )
    }
}
