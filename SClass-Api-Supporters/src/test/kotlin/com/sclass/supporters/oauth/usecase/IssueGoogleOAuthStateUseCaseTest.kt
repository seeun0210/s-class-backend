package com.sclass.supporters.oauth.usecase

import com.sclass.common.exception.ForbiddenException
import com.sclass.domain.domains.user.domain.Role
import com.sclass.infrastructure.oauth.state.GoogleOAuthStateStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

class IssueGoogleOAuthStateUseCaseTest {
    private lateinit var stateStore: GoogleOAuthStateStore
    private lateinit var useCase: IssueGoogleOAuthStateUseCase

    @BeforeEach
    fun setUp() {
        stateStore = mockk()
        useCase = IssueGoogleOAuthStateUseCase(stateStore)
    }

    @Test
    fun `선생님이면 Google OAuth state를 5분 TTL로 발급한다`() {
        val ttlSlot = slot<Duration>()
        every { stateStore.issue("teacher-user-id", capture(ttlSlot)) } returns "state-abc"

        val response = useCase.execute("teacher-user-id", Role.TEACHER)

        assertAll(
            { assertEquals("state-abc", response.state) },
            { assertEquals(300L, response.expiresInSeconds) },
            { assertEquals(Duration.ofMinutes(5), ttlSlot.captured) },
        )
    }

    @Test
    fun `선생님 권한이 아니면 state를 발급하지 않는다`() {
        assertThrows<ForbiddenException> {
            useCase.execute("student-user-id", Role.STUDENT)
        }

        verify(exactly = 0) { stateStore.issue(any(), any()) }
    }
}
