package com.sclass.backoffice.oauth.usecase

import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import com.sclass.infrastructure.calendar.GoogleCentralCalendarProperties
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetCentralGoogleConnectionStatusUseCaseTest {
    private val centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor = mockk()
    private val properties =
        GoogleCentralCalendarProperties().apply {
            allowedEmail = "central-google@example.com"
        }
    private val useCase =
        GetCentralGoogleConnectionStatusUseCase(
            centralGoogleAccountAdaptor = centralGoogleAccountAdaptor,
            properties = properties,
        )

    @Test
    fun `연결된 중앙 Google 계정 상태를 반환한다`() {
        val connectedAt = LocalDateTime.of(2026, 4, 26, 14, 0)
        every { centralGoogleAccountAdaptor.findGoogleOrNull() } returns
            CentralGoogleAccount(
                googleEmail = "central-google@example.com",
                encryptedRefreshToken = "encrypted-token",
                scope = "email https://www.googleapis.com/auth/calendar.events",
                connectedByAdminUserId = "admin-user-id-0000000001",
                connectedAt = connectedAt,
            )

        val response = useCase.execute()

        assertAll(
            { assertEquals(true, response.connected) },
            { assertEquals("central-google@example.com", response.googleEmail) },
            { assertEquals("central-google@example.com", response.allowedEmail) },
            { assertEquals("admin-user-id-0000000001", response.connectedByAdminUserId) },
            { assertEquals(connectedAt, response.connectedAt) },
        )
    }

    @Test
    fun `연결된 중앙 Google 계정이 없으면 not connected를 반환한다`() {
        every { centralGoogleAccountAdaptor.findGoogleOrNull() } returns null

        val response = useCase.execute()

        assertAll(
            { assertEquals(false, response.connected) },
            { assertEquals(null, response.googleEmail) },
            { assertEquals("central-google@example.com", response.allowedEmail) },
        )
    }
}
