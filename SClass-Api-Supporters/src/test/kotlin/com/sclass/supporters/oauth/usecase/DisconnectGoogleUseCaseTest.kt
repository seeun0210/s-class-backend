package com.sclass.supporters.oauth.usecase

import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.adaptor.TeacherGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.TeacherGoogleAccount
import com.sclass.infrastructure.oauth.client.GoogleAuthorizationCodeClient
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

class DisconnectGoogleUseCaseTest {
    private lateinit var googleClient: GoogleAuthorizationCodeClient
    private lateinit var accountAdaptor: TeacherGoogleAccountAdaptor
    private lateinit var encryptor: AesTokenEncryptor
    private lateinit var useCase: DisconnectGoogleUseCase

    private val userId = "user-id-00000000000000001"

    @BeforeEach
    fun setUp() {
        googleClient = mockk()
        accountAdaptor = mockk()
        encryptor = mockk()
        useCase = DisconnectGoogleUseCase(googleClient, accountAdaptor, encryptor)
    }

    private fun account() =
        TeacherGoogleAccount(
            userId = userId,
            googleEmail = "teacher@gmail.com",
            encryptedRefreshToken = "encrypted-refresh-token",
            scope = "https://www.googleapis.com/auth/calendar.events",
            connectedAt = LocalDateTime.of(2026, 4, 26, 14, 0),
        )

    @Test
    fun `연결된 계정이 있으면 Google revoke 후 DB 삭제`() {
        every { accountAdaptor.findByUserIdOrNull(userId) } returns account()
        every { encryptor.decrypt("encrypted-refresh-token") } returns "raw-refresh-token"
        every { googleClient.revokeRefreshToken("raw-refresh-token") } just Runs
        every { accountAdaptor.deleteByUserId(userId) } just Runs

        useCase.execute(userId)

        verify { googleClient.revokeRefreshToken("raw-refresh-token") }
        verify { accountAdaptor.deleteByUserId(userId) }
    }

    @Test
    fun `연결된 계정이 없으면 아무 동작 없이 종료`() {
        every { accountAdaptor.findByUserIdOrNull(userId) } returns null

        useCase.execute(userId)

        verify(exactly = 0) { googleClient.revokeRefreshToken(any()) }
        verify(exactly = 0) { accountAdaptor.deleteByUserId(any()) }
    }

    @Test
    fun `복호화 실패해도 DB 삭제는 진행된다`() {
        every { accountAdaptor.findByUserIdOrNull(userId) } returns account()
        every { encryptor.decrypt(any()) } throws RuntimeException("decrypt failed")
        every { accountAdaptor.deleteByUserId(userId) } just Runs

        useCase.execute(userId)

        verify(exactly = 0) { googleClient.revokeRefreshToken(any()) }
        verify { accountAdaptor.deleteByUserId(userId) }
    }

    @Test
    fun `Google revoke 호출이 예외를 던져도 DB 삭제는 진행된다`() {
        every { accountAdaptor.findByUserIdOrNull(userId) } returns account()
        every { encryptor.decrypt(any()) } returns "raw-refresh-token"
        every { googleClient.revokeRefreshToken(any()) } throws RuntimeException("revoke failed")
        every { accountAdaptor.deleteByUserId(userId) } just Runs

        useCase.execute(userId)

        verify { accountAdaptor.deleteByUserId(userId) }
    }

    @Test
    fun `connect와 disconnect 경합을 막기 위해 userId 기반 분산 락이 적용된다`() {
        val method = DisconnectGoogleUseCase::class.java.getDeclaredMethod("execute", String::class.java)
        val lock = method.getAnnotation(DistributedLock::class.java)

        assertAll(
            { assertNotNull(lock) },
            { assertEquals("teacher-google-account", lock.prefix) },
            { assertEquals(30L, lock.waitTime) },
            { assertTrue(method.parameters[0].isAnnotationPresent(LockKey::class.java)) },
        )
    }
}
