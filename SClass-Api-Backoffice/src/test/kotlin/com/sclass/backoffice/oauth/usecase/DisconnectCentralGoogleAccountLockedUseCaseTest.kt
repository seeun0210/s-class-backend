package com.sclass.backoffice.oauth.usecase

import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import com.sclass.infrastructure.oauth.client.CentralGoogleAuthorizationCodeClient
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class DisconnectCentralGoogleAccountLockedUseCaseTest {
    private lateinit var centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor
    private lateinit var googleClient: CentralGoogleAuthorizationCodeClient
    private lateinit var encryptor: AesTokenEncryptor
    private lateinit var useCase: DisconnectCentralGoogleAccountLockedUseCase

    @BeforeEach
    fun setUp() {
        centralGoogleAccountAdaptor = mockk()
        googleClient = mockk()
        encryptor = mockk()
        useCase = DisconnectCentralGoogleAccountLockedUseCase(centralGoogleAccountAdaptor, googleClient, encryptor)
    }

    private fun account() =
        CentralGoogleAccount(
            googleEmail = "central-google@example.com",
            encryptedRefreshToken = "encrypted-refresh-token",
            scope = "openid https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/calendar.events",
            connectedByAdminUserId = "admin-user-id-0000000001",
            connectedAt = LocalDateTime.of(2026, 4, 26, 14, 0),
        )

    @Test
    fun `연결된 중앙 계정이 있으면 Google revoke 후 DB 삭제`() {
        every { centralGoogleAccountAdaptor.findGoogleOrNull() } returns account()
        every { encryptor.decrypt("encrypted-refresh-token") } returns "raw-refresh-token"
        every { googleClient.revokeRefreshToken("raw-refresh-token") } just Runs
        every { centralGoogleAccountAdaptor.deleteGoogle() } just Runs

        useCase.execute(CentralGoogleAccount.PROVIDER_GOOGLE)

        verify { googleClient.revokeRefreshToken("raw-refresh-token") }
        verify { centralGoogleAccountAdaptor.deleteGoogle() }
    }

    @Test
    fun `연결된 중앙 계정이 없으면 아무 동작 없이 종료`() {
        every { centralGoogleAccountAdaptor.findGoogleOrNull() } returns null

        useCase.execute(CentralGoogleAccount.PROVIDER_GOOGLE)

        verify(exactly = 0) { googleClient.revokeRefreshToken(any()) }
        verify(exactly = 0) { centralGoogleAccountAdaptor.deleteGoogle() }
    }

    @Test
    fun `복호화 실패해도 DB 삭제는 진행된다`() {
        every { centralGoogleAccountAdaptor.findGoogleOrNull() } returns account()
        every { encryptor.decrypt(any()) } throws RuntimeException("decrypt failed")
        every { centralGoogleAccountAdaptor.deleteGoogle() } just Runs

        useCase.execute(CentralGoogleAccount.PROVIDER_GOOGLE)

        verify(exactly = 0) { googleClient.revokeRefreshToken(any()) }
        verify { centralGoogleAccountAdaptor.deleteGoogle() }
    }

    @Test
    fun `Google revoke 호출이 예외를 던져도 DB 삭제는 진행된다`() {
        every { centralGoogleAccountAdaptor.findGoogleOrNull() } returns account()
        every { encryptor.decrypt(any()) } returns "raw-refresh-token"
        every { googleClient.revokeRefreshToken(any()) } throws RuntimeException("revoke failed")
        every { centralGoogleAccountAdaptor.deleteGoogle() } just Runs

        useCase.execute(CentralGoogleAccount.PROVIDER_GOOGLE)

        verify { centralGoogleAccountAdaptor.deleteGoogle() }
    }

    @Test
    fun `connect와 disconnect 경합을 막기 위해 provider 기반 분산 락이 적용된다`() {
        val method = DisconnectCentralGoogleAccountLockedUseCase::class.java.getDeclaredMethod("execute", String::class.java)
        val lock = method.getAnnotation(DistributedLock::class.java)

        assertAll(
            { assertNotNull(lock) },
            { assertEquals("central-google-account", lock.prefix) },
            { assertEquals(30L, lock.waitTime) },
            { assertTrue(method.parameters[0].isAnnotationPresent(LockKey::class.java)) },
        )
    }
}
