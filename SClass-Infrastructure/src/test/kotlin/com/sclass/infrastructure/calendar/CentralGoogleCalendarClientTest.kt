package com.sclass.infrastructure.calendar

import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventCreateCommand
import com.sclass.infrastructure.calendar.dto.GoogleCalendarEventResult
import com.sclass.infrastructure.oauth.client.CentralGoogleAuthorizationCodeClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
        val command =
            GoogleCalendarEventCreateCommand(
                summary = "수학 1회차",
                startAt = ZonedDateTime.of(2026, 4, 26, 14, 0, 0, 0, ZoneId.of("Asia/Seoul")),
                endAt = ZonedDateTime.of(2026, 4, 26, 15, 0, 0, 0, ZoneId.of("Asia/Seoul")),
                attendeeEmails = listOf("teacher@example.com", "student@example.com"),
            )
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
}
