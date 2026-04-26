package com.sclass.infrastructure.calendar

import com.sclass.common.exception.GoogleCalendarUnauthorizedException
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventCreateCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResult
import com.sclass.infrastructure.oauth.client.CentralGoogleAuthorizationCodeClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.ZoneId
import java.time.ZonedDateTime

class CentralGoogleCalendarClientTest {
    private val authorizationCodeClient: CentralGoogleAuthorizationCodeClient = mockk()
    private val googleCalendarClient: GoogleCalendarClient = mockk()
    private val properties =
        GoogleCentralCalendarProperties().apply {
            calendarId = "primary"
        }
    private val client =
        CentralGoogleCalendarClient(
            authorizationCodeClient = authorizationCodeClient,
            googleCalendarClient = googleCalendarClient,
            properties = properties,
        )

    @Test
    fun `central refresh token으로 access token을 갱신해 중앙 캘린더에 Meet 이벤트를 생성한다`() {
        val command = command()
        val expected = GoogleCalendarEventResult(eventId = "event-id", meetJoinUrl = "https://meet.google.com/abc-defg-hij")

        every { authorizationCodeClient.refreshAccessToken("central-refresh-token") } returns "central-access-token"
        every {
            googleCalendarClient.createMeetEventWithAccessToken(
                command = command,
                accessToken = "central-access-token",
                calendarId = "primary",
            )
        } returns expected

        val result =
            client.createMeetEventWithRefreshToken(
                refreshToken = "central-refresh-token",
                command = command,
            )

        assertEquals(expected, result)
        verify { authorizationCodeClient.refreshAccessToken("central-refresh-token") }
        verify {
            googleCalendarClient.createMeetEventWithAccessToken(
                command = command,
                accessToken = "central-access-token",
                calendarId = "primary",
            )
        }
    }

    @Test
    fun `중앙 캘린더 이벤트 생성에서 Google 인증 실패가 발생하면 도메인 예외로 변환한다`() {
        val command = command()
        every { authorizationCodeClient.refreshAccessToken("central-refresh-token") } returns "central-access-token"
        every {
            googleCalendarClient.createMeetEventWithAccessToken(
                command = command,
                accessToken = "central-access-token",
                calendarId = "primary",
            )
        } throws
            WebClientResponseException.create(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                HttpHeaders.EMPTY,
                ByteArray(0),
                null,
            )

        assertThrows<GoogleCalendarUnauthorizedException> {
            client.createMeetEventWithRefreshToken(
                refreshToken = "central-refresh-token",
                command = command,
            )
        }
    }

    @Test
    fun `central refresh token으로 access token을 갱신해 중앙 캘린더 Meet 이벤트를 수정한다`() {
        val command = command()
        val expected = GoogleCalendarEventResult(eventId = "event-id", meetJoinUrl = "https://meet.google.com/abc-defg-hij")

        every { authorizationCodeClient.refreshAccessToken("central-refresh-token") } returns "central-access-token"
        every {
            googleCalendarClient.updateMeetEventWithAccessToken(
                eventId = "event-id",
                command = command,
                accessToken = "central-access-token",
                calendarId = "primary",
            )
        } returns expected

        val result =
            client.updateMeetEventWithRefreshToken(
                refreshToken = "central-refresh-token",
                eventId = "event-id",
                command = command,
            )

        assertEquals(expected, result)
        verify { authorizationCodeClient.refreshAccessToken("central-refresh-token") }
        verify {
            googleCalendarClient.updateMeetEventWithAccessToken(
                eventId = "event-id",
                command = command,
                accessToken = "central-access-token",
                calendarId = "primary",
            )
        }
    }

    @Test
    fun `중앙 캘린더 이벤트 수정에서 Google 인증 실패가 발생하면 도메인 예외로 변환한다`() {
        val command = command()
        every { authorizationCodeClient.refreshAccessToken("central-refresh-token") } returns "central-access-token"
        every {
            googleCalendarClient.updateMeetEventWithAccessToken(
                eventId = "event-id",
                command = command,
                accessToken = "central-access-token",
                calendarId = "primary",
            )
        } throws
            WebClientResponseException.create(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                HttpHeaders.EMPTY,
                ByteArray(0),
                null,
            )

        assertThrows<GoogleCalendarUnauthorizedException> {
            client.updateMeetEventWithRefreshToken(
                refreshToken = "central-refresh-token",
                eventId = "event-id",
                command = command,
            )
        }
    }

    @Test
    fun `central refresh token으로 access token을 갱신해 중앙 캘린더 Meet 이벤트를 삭제한다`() {
        every { authorizationCodeClient.refreshAccessToken("central-refresh-token") } returns "central-access-token"
        every {
            googleCalendarClient.deleteMeetEventWithAccessToken(
                eventId = "event-id",
                accessToken = "central-access-token",
                calendarId = "primary",
            )
        } returns Unit

        client.deleteMeetEventWithRefreshToken(
            refreshToken = "central-refresh-token",
            eventId = "event-id",
        )

        verify { authorizationCodeClient.refreshAccessToken("central-refresh-token") }
        verify {
            googleCalendarClient.deleteMeetEventWithAccessToken(
                eventId = "event-id",
                accessToken = "central-access-token",
                calendarId = "primary",
            )
        }
    }

    @Test
    fun `중앙 캘린더 이벤트 삭제에서 Google 인증 실패가 발생하면 도메인 예외로 변환한다`() {
        every { authorizationCodeClient.refreshAccessToken("central-refresh-token") } returns "central-access-token"
        every {
            googleCalendarClient.deleteMeetEventWithAccessToken(
                eventId = "event-id",
                accessToken = "central-access-token",
                calendarId = "primary",
            )
        } throws
            WebClientResponseException.create(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                HttpHeaders.EMPTY,
                ByteArray(0),
                null,
            )

        assertThrows<GoogleCalendarUnauthorizedException> {
            client.deleteMeetEventWithRefreshToken(
                refreshToken = "central-refresh-token",
                eventId = "event-id",
            )
        }
    }

    private fun command() =
        GoogleCalendarEventCreateCommand(
            summary = "수학 1회차",
            startAt = ZonedDateTime.of(2026, 4, 26, 14, 0, 0, 0, ZoneId.of("Asia/Seoul")),
            endAt = ZonedDateTime.of(2026, 4, 26, 15, 0, 0, 0, ZoneId.of("Asia/Seoul")),
            attendeeEmails = listOf("teacher@example.com", "student@example.com"),
        )
}
