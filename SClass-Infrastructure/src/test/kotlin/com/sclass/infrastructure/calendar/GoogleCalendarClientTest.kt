package com.sclass.infrastructure.calendar

import com.fasterxml.jackson.databind.ObjectMapper
import com.sclass.common.exception.GoogleCalendarRequestFailedException
import com.sclass.common.exception.GoogleCalendarUnauthorizedException
import com.sclass.common.exception.GoogleOAuthProviderUnavailableException
import com.sclass.infrastructure.calendar.dto.CreateGoogleCalendarEventCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarConferenceResponse
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEntryPoint
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventCreateCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventRequest
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResponse
import com.sclass.infrastructure.oauth.client.GoogleAuthorizationCodeClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.ZoneId
import java.time.ZonedDateTime

class GoogleCalendarClientTest {
    private lateinit var webClient: WebClient
    private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec
    private lateinit var requestBodySpec: WebClient.RequestBodySpec
    private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>
    private lateinit var responseSpec: WebClient.ResponseSpec
    private lateinit var authorizationCodeClient: GoogleAuthorizationCodeClient
    private lateinit var client: GoogleCalendarClient

    private val command =
        CreateGoogleCalendarEventCommand(
            accessToken = "expired-access-token",
            refreshToken = "refresh-token",
            summary = "수학 1회차",
            startAt = ZonedDateTime.of(2026, 4, 26, 14, 0, 0, 0, ZoneId.of("Asia/Seoul")),
            endAt = ZonedDateTime.of(2026, 4, 26, 15, 0, 0, 0, ZoneId.of("Asia/Seoul")),
        )

    @BeforeEach
    fun setUp() {
        webClient = mockk()
        requestBodyUriSpec = mockk()
        requestBodySpec = mockk()
        requestHeadersSpec = mockk()
        responseSpec = mockk()
        authorizationCodeClient = mockk()
        client = GoogleCalendarClient(webClient, authorizationCodeClient)
    }

    private fun mockCreateEventCall(
        requestSlot: MutableList<GoogleCalendarEventRequest> = mutableListOf(),
        uri: String = CALENDAR_EVENTS_URI,
    ) {
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(uri) } returns requestBodySpec
        every { requestBodySpec.header(HttpHeaders.AUTHORIZATION, any()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(capture(requestSlot)) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
    }

    private fun mockUpdateEventCall(
        requestSlot: MutableList<GoogleCalendarEventRequest> = mutableListOf(),
        uri: String = CALENDAR_EVENT_URI,
    ) {
        every { webClient.patch() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(uri) } returns requestBodySpec
        every { requestBodySpec.header(HttpHeaders.AUTHORIZATION, any()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(capture(requestSlot)) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
    }

    @Test
    fun `Calendar event 생성 응답에서 video entryPoint를 Meet 링크로 반환한다`() {
        val requestSlot = mutableListOf<GoogleCalendarEventRequest>()
        mockCreateEventCall(requestSlot)
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.just(calendarResponse(meetUri = "https://meet.google.com/abc-defg-hij"))

        val result = client.createMeetEvent(command)

        assertAll(
            { assertEquals("event-id-1", result.eventId) },
            { assertEquals("https://meet.google.com/abc-defg-hij", result.meetJoinUrl) },
            { assertEquals("수학 1회차", requestSlot.single().summary) },
            { assertEquals("2026-04-26T14:00+09:00", requestSlot.single().start.dateTime) },
            { assertEquals("Asia/Seoul", requestSlot.single().start.timeZone) },
            {
                assertEquals(
                    "hangoutsMeet",
                    requestSlot
                        .single()
                        .conferenceData!!
                        .createRequest
                        .conferenceSolutionKey
                        .type,
                )
            },
            { assertEquals("abc-defg-hij", result.meetCode) },
        )
    }

    @Test
    fun `video entryPoint가 없으면 hangoutLink를 Meet 링크로 반환한다`() {
        mockCreateEventCall()
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.just(
                calendarResponse(
                    meetUri = null,
                    hangoutLink = "https://meet.google.com/hangout-link",
                    conferenceId = null,
                ),
            )

        val result = client.createMeetEvent(command)

        assertAll(
            { assertEquals("https://meet.google.com/hangout-link", result.meetJoinUrl) },
            { assertEquals("hangout-link", result.meetCode) },
        )
    }

    @Test
    fun `attendee가 있으면 이벤트 요청에 참석자를 포함하고 업데이트를 전송한다`() {
        val requestSlot = mutableListOf<GoogleCalendarEventRequest>()
        mockCreateEventCall(requestSlot, CALENDAR_EVENTS_WITH_SEND_UPDATES_URI)
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.just(calendarResponse(meetUri = "https://meet.google.com/abc-defg-hij"))
        val commandWithAttendees =
            command.copy(
                attendeeEmails = listOf("teacher@example.com", "student@example.com"),
            )

        val result = client.createMeetEvent(commandWithAttendees)

        assertEquals("https://meet.google.com/abc-defg-hij", result.meetJoinUrl)
        assertEquals(listOf("teacher@example.com", "student@example.com"), requestSlot.single().attendees.map { it.email })
    }

    @Test
    fun `calendarId가 이메일 형식이면 URL path에 안전하게 인코딩한다`() {
        mockCreateEventCall(uri = CALENDAR_EVENTS_WITH_ENCODED_CALENDAR_ID_URI)
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.just(calendarResponse(meetUri = "https://meet.google.com/abc-defg-hij"))

        val result =
            client.createMeetEventWithAccessToken(
                command =
                    GoogleCalendarEventCreateCommand(
                        summary = command.summary,
                        startAt = command.startAt,
                        endAt = command.endAt,
                    ),
                accessToken = "central-access-token",
                calendarId = "central@example.com",
            )

        assertEquals("https://meet.google.com/abc-defg-hij", result.meetJoinUrl)
    }

    @Test
    fun `Calendar event 수정은 기존 Meet 생성을 요청하지 않고 일정과 참석자만 전송한다`() {
        val requestSlot = mutableListOf<GoogleCalendarEventRequest>()
        mockUpdateEventCall(requestSlot, CALENDAR_EVENT_WITH_SEND_UPDATES_URI)
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.just(calendarResponse(meetUri = "https://meet.google.com/updated-code", conferenceId = "updated-code"))
        val updateCommand =
            GoogleCalendarEventCreateCommand(
                summary = "변경된 수학 1회차",
                startAt = command.startAt.plusDays(1),
                endAt = command.endAt.plusDays(1),
                attendeeEmails = listOf("teacher@example.com"),
            )

        val result =
            client.updateMeetEventWithAccessToken(
                eventId = "event-id-1",
                command = updateCommand,
                accessToken = "access-token",
            )

        assertAll(
            { assertEquals("event-id-1", result.eventId) },
            { assertEquals("https://meet.google.com/updated-code", result.meetJoinUrl) },
            { assertEquals("updated-code", result.meetCode) },
            { assertEquals("변경된 수학 1회차", requestSlot.single().summary) },
            { assertEquals("2026-04-27T14:00+09:00", requestSlot.single().start.dateTime) },
            { assertEquals("Asia/Seoul", requestSlot.single().start.timeZone) },
            { assertEquals(null, requestSlot.single().conferenceData) },
            { assertEquals(listOf("teacher@example.com"), requestSlot.single().attendees.map { it.email }) },
        )
        assertFalse(ObjectMapper().writeValueAsString(requestSlot.single()).contains("conferenceData"))
        verify { requestBodySpec.header(HttpHeaders.AUTHORIZATION, "Bearer access-token") }
    }

    @Test
    fun `event id와 calendar id는 update URL path에 안전하게 인코딩한다`() {
        mockUpdateEventCall(uri = CALENDAR_EVENT_WITH_ENCODED_IDS_URI)
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.just(calendarResponse(meetUri = "https://meet.google.com/updated-code"))

        client.updateMeetEventWithAccessToken(
            eventId = "event/id with space",
            command =
                GoogleCalendarEventCreateCommand(
                    summary = command.summary,
                    startAt = command.startAt,
                    endAt = command.endAt,
                ),
            accessToken = "access-token",
            calendarId = "central@example.com",
        )

        verify { requestBodyUriSpec.uri(CALENDAR_EVENT_WITH_ENCODED_IDS_URI) }
    }

    @Test
    fun `access token 인증 실패 시 refresh token으로 갱신 후 한 번 재시도한다`() {
        mockCreateEventCall()
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returnsMany
            listOf(
                Mono.error(webClientException(HttpStatus.UNAUTHORIZED)),
                Mono.just(calendarResponse(meetUri = "https://meet.google.com/refreshed")),
            )
        every { authorizationCodeClient.refreshAccessToken("refresh-token") } returns "refreshed-access-token"

        val result = client.createMeetEvent(command)

        assertEquals("https://meet.google.com/refreshed", result.meetJoinUrl)
        verify { requestBodySpec.header(HttpHeaders.AUTHORIZATION, "Bearer expired-access-token") }
        verify { requestBodySpec.header(HttpHeaders.AUTHORIZATION, "Bearer refreshed-access-token") }
        verify { authorizationCodeClient.refreshAccessToken("refresh-token") }
    }

    @Test
    fun `refresh token 갱신에 실패하면 GoogleCalendarUnauthorizedException`() {
        mockCreateEventCall()
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.error(webClientException(HttpStatus.UNAUTHORIZED))
        every { authorizationCodeClient.refreshAccessToken("refresh-token") } throws RuntimeException("revoked")

        assertThrows<GoogleCalendarUnauthorizedException> {
            client.createMeetEvent(command)
        }
    }

    @Test
    fun `refresh token 갱신 중 Google 서버 장애는 provider unavailable 예외를 유지한다`() {
        mockCreateEventCall()
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.error(webClientException(HttpStatus.UNAUTHORIZED))
        every { authorizationCodeClient.refreshAccessToken("refresh-token") } throws GoogleOAuthProviderUnavailableException()

        assertThrows<GoogleOAuthProviderUnavailableException> {
            client.createMeetEvent(command)
        }
    }

    @Test
    fun `refresh 후 재시도도 인증 실패하면 GoogleCalendarUnauthorizedException`() {
        mockCreateEventCall()
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returnsMany
            listOf(
                Mono.error(webClientException(HttpStatus.UNAUTHORIZED)),
                Mono.error(webClientException(HttpStatus.FORBIDDEN)),
            )
        every { authorizationCodeClient.refreshAccessToken("refresh-token") } returns "refreshed-access-token"

        assertThrows<GoogleCalendarUnauthorizedException> {
            client.createMeetEvent(command)
        }
    }

    @Test
    fun `Google Calendar 4xx 응답은 GoogleCalendarRequestFailedException`() {
        mockCreateEventCall()
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.error(webClientException(HttpStatus.BAD_REQUEST))

        assertThrows<GoogleCalendarRequestFailedException> {
            client.createMeetEvent(command)
        }
    }

    @Test
    fun `Google Calendar 5xx 응답은 GoogleOAuthProviderUnavailableException`() {
        mockCreateEventCall()
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.error(webClientException(HttpStatus.SERVICE_UNAVAILABLE))

        assertThrows<GoogleOAuthProviderUnavailableException> {
            client.createMeetEvent(command)
        }
    }

    @Test
    fun `Google Calendar 403 rate limit 응답은 GoogleOAuthProviderUnavailableException`() {
        mockCreateEventCall()
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.error(
                webClientException(
                    status = HttpStatus.FORBIDDEN,
                    responseBody =
                        """
                        {
                          "error": {
                            "code": 403,
                            "status": "RESOURCE_EXHAUSTED",
                            "errors": [
                              {
                                "reason": "rateLimitExceeded"
                              }
                            ]
                          }
                        }
                        """.trimIndent(),
                ),
            )

        assertThrows<GoogleOAuthProviderUnavailableException> {
            client.createMeetEvent(command)
        }
        verify(exactly = 0) { authorizationCodeClient.refreshAccessToken(any()) }
    }

    @Test
    fun `응답에 event id가 없으면 GoogleCalendarRequestFailedException`() {
        mockCreateEventCall()
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.just(calendarResponse(id = null, meetUri = "https://meet.google.com/abc-defg-hij"))

        assertThrows<GoogleCalendarRequestFailedException> {
            client.createMeetEvent(command)
        }
    }

    @Test
    fun `응답에 Meet 링크가 없으면 GoogleCalendarRequestFailedException`() {
        mockCreateEventCall()
        every { responseSpec.bodyToMono(GoogleCalendarEventResponse::class.java) } returns
            Mono.just(calendarResponse(meetUri = null, hangoutLink = null))

        assertThrows<GoogleCalendarRequestFailedException> {
            client.createMeetEvent(command)
        }
    }

    private fun calendarResponse(
        id: String? = "event-id-1",
        meetUri: String?,
        hangoutLink: String? = null,
        conferenceId: String? = "abc-defg-hij",
    ): GoogleCalendarEventResponse =
        GoogleCalendarEventResponse(
            id = id,
            hangoutLink = hangoutLink,
            conferenceData =
                GoogleCalendarConferenceResponse(
                    conferenceId = conferenceId,
                    entryPoints =
                        listOfNotNull(
                            meetUri?.let {
                                GoogleCalendarEntryPoint(
                                    entryPointType = "video",
                                    uri = it,
                                )
                            },
                        ),
                ),
        )

    private fun webClientException(
        status: HttpStatus,
        responseBody: String = "",
    ): WebClientResponseException =
        WebClientResponseException.create(
            status.value(),
            status.reasonPhrase,
            HttpHeaders.EMPTY,
            responseBody.toByteArray(),
            Charsets.UTF_8,
        )

    private companion object {
        const val CALENDAR_EVENTS_URI =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events?conferenceDataVersion=1"
        const val CALENDAR_EVENTS_WITH_SEND_UPDATES_URI =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events?conferenceDataVersion=1&sendUpdates=all"
        const val CALENDAR_EVENTS_WITH_ENCODED_CALENDAR_ID_URI =
            "https://www.googleapis.com/calendar/v3/calendars/central%40example.com/events?conferenceDataVersion=1"
        const val CALENDAR_EVENT_URI =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events/event-id-1?conferenceDataVersion=1"
        const val CALENDAR_EVENT_WITH_SEND_UPDATES_URI =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events/event-id-1?conferenceDataVersion=1&sendUpdates=all"
        const val CALENDAR_EVENT_WITH_ENCODED_IDS_URI =
            "https://www.googleapis.com/calendar/v3/calendars/central%40example.com/events/event%2Fid+with+space?conferenceDataVersion=1"
    }
}
