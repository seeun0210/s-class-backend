package com.sclass.supporters.oauth.usecase

import com.sclass.domain.domains.oauth.adaptor.TeacherGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.TeacherGoogleAccount
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetGoogleConnectionStatusUseCaseTest {
    private lateinit var accountAdaptor: TeacherGoogleAccountAdaptor
    private lateinit var useCase: GetGoogleConnectionStatusUseCase

    private val userId = "user-id-00000000000000001"

    @BeforeEach
    fun setUp() {
        accountAdaptor = mockk()
        useCase = GetGoogleConnectionStatusUseCase(accountAdaptor)
    }

    @Test
    fun `연결된 계정이 있으면 connected=true와 상세 정보 반환`() {
        val connectedAt = LocalDateTime.of(2026, 4, 26, 14, 0)
        every { accountAdaptor.findByUserIdOrNull(userId) } returns
            TeacherGoogleAccount(
                userId = userId,
                googleEmail = "teacher@gmail.com",
                encryptedRefreshToken = "encrypted-token",
                scope = "https://www.googleapis.com/auth/calendar.events",
                connectedAt = connectedAt,
            )

        val response = useCase.execute(userId)

        assertAll(
            { assertTrue(response.connected) },
            { assertEquals("teacher@gmail.com", response.googleEmail) },
            { assertEquals(connectedAt, response.connectedAt) },
            { assertEquals("https://www.googleapis.com/auth/calendar.events", response.scope) },
        )
    }

    @Test
    fun `연결된 계정이 없으면 connected=false`() {
        every { accountAdaptor.findByUserIdOrNull(userId) } returns null

        val response = useCase.execute(userId)

        assertAll(
            { assertFalse(response.connected) },
            { assertNull(response.googleEmail) },
            { assertNull(response.connectedAt) },
            { assertNull(response.scope) },
        )
    }
}
