package com.sclass.domain.domains.verification.service

import com.sclass.domain.domains.verification.adaptor.VerificationAdaptor
import com.sclass.domain.domains.verification.domain.Verification
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.exception.VerificationCodeMismatchException
import com.sclass.domain.domains.verification.exception.VerificationExpiredException
import com.sclass.domain.domains.verification.exception.VerificationMaxAttemptsException
import com.sclass.domain.domains.verification.exception.VerificationNotFoundException
import com.sclass.domain.domains.verification.exception.VerificationSendRateLimitException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class VerificationDomainServiceTest {
    private lateinit var verificationAdaptor: VerificationAdaptor
    private lateinit var verificationDomainService: VerificationDomainService

    @BeforeEach
    fun setUp() {
        verificationAdaptor = mockk()
        verificationDomainService = VerificationDomainService(verificationAdaptor)
    }

    @Nested
    inner class CreateVerification {
        @Test
        fun `인증을 생성하고 저장한다`() {
            val slot = slot<Verification>()
            every { verificationAdaptor.countRecent(VerificationChannel.PHONE, "010-1234-5678", any()) } returns 0L
            every { verificationAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = verificationDomainService.createVerification(VerificationChannel.PHONE, "010-1234-5678")

            assertEquals(VerificationChannel.PHONE, result.channel)
            assertEquals("010-1234-5678", result.target)
            assertEquals(6, result.code.length)
            assertNotNull(result.expiresAt)
            verify { verificationAdaptor.save(any()) }
        }

        @Test
        fun `1시간 내 발송 횟수가 4회면 정상 발송된다`() {
            val slot = slot<Verification>()
            every { verificationAdaptor.countRecent(VerificationChannel.PHONE, "010-1234-5678", any()) } returns 4L
            every { verificationAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = verificationDomainService.createVerification(VerificationChannel.PHONE, "010-1234-5678")

            assertNotNull(result)
        }

        @Test
        fun `1시간 내 발송 횟수가 5회 이상이면 VerificationSendRateLimitException이 발생한다`() {
            every { verificationAdaptor.countRecent(VerificationChannel.PHONE, "010-1234-5678", any()) } returns 5L

            assertThrows<VerificationSendRateLimitException> {
                verificationDomainService.createVerification(VerificationChannel.PHONE, "010-1234-5678")
            }
        }
    }

    @Nested
    inner class VerifyCode {
        @Test
        fun `올바른 코드로 인증하면 verified 상태가 된다`() {
            val verification =
                Verification(
                    channel = VerificationChannel.PHONE,
                    target = "010-1234-5678",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(5),
                )

            every { verificationAdaptor.findLatestOrNull(VerificationChannel.PHONE, "010-1234-5678") } returns verification

            val result = verificationDomainService.verifyCode(VerificationChannel.PHONE, "010-1234-5678", "123456")

            assertEquals(true, result.verified)
            assertEquals(1, result.attemptCount)
        }

        @Test
        fun `인증 요청이 없으면 VerificationNotFoundException이 발생한다`() {
            every { verificationAdaptor.findLatestOrNull(VerificationChannel.PHONE, "010-0000-0000") } returns null

            assertThrows<VerificationNotFoundException> {
                verificationDomainService.verifyCode(VerificationChannel.PHONE, "010-0000-0000", "123456")
            }
        }

        @Test
        fun `만료된 인증코드로 인증하면 VerificationExpiredException이 발생한다`() {
            val verification =
                Verification(
                    channel = VerificationChannel.PHONE,
                    target = "010-1234-5678",
                    code = "123456",
                    expiresAt = LocalDateTime.now().minusMinutes(1),
                )

            every { verificationAdaptor.findLatestOrNull(VerificationChannel.PHONE, "010-1234-5678") } returns verification

            assertThrows<VerificationExpiredException> {
                verificationDomainService.verifyCode(VerificationChannel.PHONE, "010-1234-5678", "123456")
            }
        }

        @Test
        fun `시도 횟수가 5회 이상이면 VerificationMaxAttemptsException이 발생한다`() {
            val verification =
                Verification(
                    channel = VerificationChannel.PHONE,
                    target = "010-1234-5678",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(5),
                    attemptCount = 5,
                )

            every { verificationAdaptor.findLatestOrNull(VerificationChannel.PHONE, "010-1234-5678") } returns verification

            assertThrows<VerificationMaxAttemptsException> {
                verificationDomainService.verifyCode(VerificationChannel.PHONE, "010-1234-5678", "123456")
            }
        }

        @Test
        fun `잘못된 코드로 인증하면 VerificationCodeMismatchException이 발생하고 시도 횟수가 증가한다`() {
            val verification =
                Verification(
                    channel = VerificationChannel.PHONE,
                    target = "010-1234-5678",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(5),
                )

            every { verificationAdaptor.findLatestOrNull(VerificationChannel.PHONE, "010-1234-5678") } returns verification

            assertThrows<VerificationCodeMismatchException> {
                verificationDomainService.verifyCode(VerificationChannel.PHONE, "010-1234-5678", "999999")
            }
            assertEquals(1, verification.attemptCount)
        }
    }
}
