package com.sclass.supporters.oauth.usecase

import com.sclass.domain.domains.oauth.adaptor.TeacherGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.TeacherGoogleAccount
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class ConnectGoogleAccountLockedUseCaseTest {
    private lateinit var accountAdaptor: TeacherGoogleAccountAdaptor
    private lateinit var useCase: ConnectGoogleAccountLockedUseCase

    private val userId = "user-id-00000000000000001"
    private val fixedNow = LocalDateTime.of(2026, 4, 26, 14, 0)
    private val clock =
        Clock.fixed(
            fixedNow.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault(),
        )

    @BeforeEach
    fun setUp() {
        accountAdaptor = mockk()
        useCase = ConnectGoogleAccountLockedUseCase(accountAdaptor, clock)

        every { accountAdaptor.save(any()) } answers { firstArg() }
    }

    @Test
    fun `신규 연결 시 lock 안에서 새 TeacherGoogleAccount를 저장한다`() {
        every { accountAdaptor.findByUserIdOrNull(userId) } returns null

        val account =
            useCase.execute(
                userId = userId,
                googleEmail = "teacher@gmail.com",
                encryptedRefreshToken = "encrypted-refresh-token",
                scope = "email https://www.googleapis.com/auth/calendar.events",
            )

        assertAll(
            { assertEquals(userId, account.userId) },
            { assertEquals("teacher@gmail.com", account.googleEmail) },
            { assertEquals("encrypted-refresh-token", account.encryptedRefreshToken) },
            { assertEquals("email https://www.googleapis.com/auth/calendar.events", account.scope) },
            { assertEquals(fixedNow, account.connectedAt) },
        )
        verify {
            accountAdaptor.save(
                match<TeacherGoogleAccount> {
                    it.userId == userId &&
                        it.googleEmail == "teacher@gmail.com" &&
                        it.encryptedRefreshToken == "encrypted-refresh-token" &&
                        it.connectedAt == fixedNow
                },
            )
        }
    }

    @Test
    fun `기존 연결이 있으면 lock 안에서 reconnect로 토큰과 이메일이 갱신된다`() {
        val existing =
            TeacherGoogleAccount(
                userId = userId,
                googleEmail = "old@gmail.com",
                encryptedRefreshToken = "encrypted-old-token",
                scope = "old-scope",
                connectedAt = fixedNow.minusDays(30),
            )
        every { accountAdaptor.findByUserIdOrNull(userId) } returns existing

        val account =
            useCase.execute(
                userId = userId,
                googleEmail = "teacher@gmail.com",
                encryptedRefreshToken = "encrypted-new-token",
                scope = "email https://www.googleapis.com/auth/calendar.events",
            )

        assertAll(
            { assertEquals(existing, account) },
            { assertEquals("teacher@gmail.com", existing.googleEmail) },
            { assertEquals("encrypted-new-token", existing.encryptedRefreshToken) },
            { assertEquals("email https://www.googleapis.com/auth/calendar.events", existing.scope) },
            { assertEquals(fixedNow.minusDays(30), existing.connectedAt) },
        )
        verify { accountAdaptor.save(existing) }
    }

    @Test
    fun `동시 최초 연결 저장을 막기 위해 userId 기반 분산 락이 적용된다`() {
        val method =
            ConnectGoogleAccountLockedUseCase::class.java.getDeclaredMethod(
                "execute",
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java,
            )

        val lock = method.getAnnotation(DistributedLock::class.java)

        assertAll(
            { assertNotNull(lock) },
            { assertEquals("teacher-google-account", lock.prefix) },
            { assertEquals(30L, lock.waitTime) },
            { assertTrue(method.parameters[0].isAnnotationPresent(LockKey::class.java)) },
        )
    }
}
