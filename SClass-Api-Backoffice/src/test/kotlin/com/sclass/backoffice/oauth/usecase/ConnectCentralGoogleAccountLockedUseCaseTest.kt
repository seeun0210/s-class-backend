package com.sclass.backoffice.oauth.usecase

import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
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

class ConnectCentralGoogleAccountLockedUseCaseTest {
    private lateinit var centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor
    private lateinit var useCase: ConnectCentralGoogleAccountLockedUseCase

    private val fixedNow = LocalDateTime.of(2026, 4, 26, 14, 0)
    private val clock =
        Clock.fixed(
            fixedNow.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault(),
        )

    @BeforeEach
    fun setUp() {
        centralGoogleAccountAdaptor = mockk()
        useCase = ConnectCentralGoogleAccountLockedUseCase(centralGoogleAccountAdaptor, clock)

        every { centralGoogleAccountAdaptor.save(any()) } answers { firstArg() }
    }

    @Test
    fun `신규 연결 시 lock 안에서 새 CentralGoogleAccount를 저장한다`() {
        every { centralGoogleAccountAdaptor.findGoogleOrNull() } returns null

        val account =
            useCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                googleEmail = "central-google@example.com",
                encryptedRefreshToken = "encrypted-refresh-token",
                scope = "openid https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/calendar.events",
                adminUserId = "admin-user-id-0000000001",
            )

        assertAll(
            { assertEquals(CentralGoogleAccount.PROVIDER_GOOGLE, account.provider) },
            { assertEquals("central-google@example.com", account.googleEmail) },
            { assertEquals("encrypted-refresh-token", account.encryptedRefreshToken) },
            { assertEquals("admin-user-id-0000000001", account.connectedByAdminUserId) },
            { assertEquals(fixedNow, account.connectedAt) },
        )
        verify {
            centralGoogleAccountAdaptor.save(
                match<CentralGoogleAccount> {
                    it.provider == CentralGoogleAccount.PROVIDER_GOOGLE &&
                        it.googleEmail == "central-google@example.com" &&
                        it.encryptedRefreshToken == "encrypted-refresh-token" &&
                        it.connectedAt == fixedNow
                },
            )
        }
    }

    @Test
    fun `기존 연결이 있으면 lock 안에서 reconnect로 토큰과 이메일이 갱신된다`() {
        val existing =
            CentralGoogleAccount(
                googleEmail = "old-central-google@example.com",
                encryptedRefreshToken = "old-token",
                scope = "old-scope",
                connectedByAdminUserId = "old-admin-user-id-00001",
                connectedAt = fixedNow.minusDays(1),
            )
        every { centralGoogleAccountAdaptor.findGoogleOrNull() } returns existing

        val account =
            useCase.execute(
                provider = CentralGoogleAccount.PROVIDER_GOOGLE,
                googleEmail = "central-google@example.com",
                encryptedRefreshToken = "new-encrypted-refresh-token",
                scope = "openid https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/calendar.events",
                adminUserId = "admin-user-id-0000000001",
            )

        assertAll(
            { assertEquals(existing, account) },
            { assertEquals("central-google@example.com", existing.googleEmail) },
            { assertEquals("new-encrypted-refresh-token", existing.encryptedRefreshToken) },
            { assertEquals("admin-user-id-0000000001", existing.connectedByAdminUserId) },
            { assertEquals(fixedNow, existing.connectedAt) },
        )
        verify { centralGoogleAccountAdaptor.save(existing) }
    }

    @Test
    fun `동시 최초 연결 저장을 막기 위해 provider 기반 분산 락이 적용된다`() {
        val method =
            ConnectCentralGoogleAccountLockedUseCase::class.java.getDeclaredMethod(
                "execute",
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java,
            )

        val lock = method.getAnnotation(DistributedLock::class.java)

        assertAll(
            { assertNotNull(lock) },
            { assertEquals("central-google-account", lock.prefix) },
            { assertEquals(30L, lock.waitTime) },
            { assertTrue(method.parameters[0].isAnnotationPresent(LockKey::class.java)) },
        )
    }
}
