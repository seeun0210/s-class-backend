package com.sclass.lms.auth.usecase

import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.verification.domain.Verification
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.exception.VerificationCodeMismatchException
import com.sclass.domain.domains.verification.exception.VerificationSendRateLimitException
import com.sclass.domain.domains.verification.service.VerificationDomainService
import com.sclass.infrastructure.message.VerificationCodeSender
import com.sclass.lms.auth.dto.SendPhoneCodeRequest
import com.sclass.lms.auth.dto.VerifyPhoneCodeRequest
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class PhoneVerificationUseCaseTest {
    private lateinit var verificationService: VerificationDomainService
    private lateinit var tokenService: TokenDomainService
    private lateinit var messageSender: VerificationCodeSender
    private lateinit var useCase: PhoneVerificationUseCase

    @BeforeEach
    fun setUp() {
        verificationService = mockk()
        tokenService = mockk()
        messageSender = mockk()
        useCase = PhoneVerificationUseCase(verificationService, tokenService, messageSender, messageSender)
    }

    @Test
    fun `인증코드 발송 요청 시 인증을 생성하고 메시지를 발송한다`() {
        val request = SendPhoneCodeRequest(phoneNumber = "010-1234-5678")
        val verification =
            Verification(
                channel = VerificationChannel.PHONE,
                target = "010-1234-5678",
                code = "123456",
                expiresAt = LocalDateTime.now().plusMinutes(5),
            )

        every {
            verificationService.createVerification(
                channel = VerificationChannel.PHONE,
                target = "010-1234-5678",
            )
        } returns verification
        every { messageSender.sendVerificationCode("010-1234-5678", "123456") } just runs

        val result = useCase.sendCode(request)

        assertNotNull(result)
        assertEquals(300L, result.expiresInSeconds)
        verify { messageSender.sendVerificationCode("010-1234-5678", "123456") }
    }

    @Test
    fun `발송 횟수 초과 시 VerificationSendRateLimitException이 발생한다`() {
        val request = SendPhoneCodeRequest(phoneNumber = "010-1234-5678")

        every {
            verificationService.createVerification(
                channel = VerificationChannel.PHONE,
                target = "010-1234-5678",
            )
        } throws VerificationSendRateLimitException()

        assertThrows<VerificationSendRateLimitException> {
            useCase.sendCode(request)
        }
    }

    @Test
    fun `올바른 인증코드로 인증하면 phoneVerificationToken을 반환한다`() {
        val request = VerifyPhoneCodeRequest(phoneNumber = "010-1234-5678", code = "123456")
        val verification =
            Verification(
                channel = VerificationChannel.PHONE,
                target = "010-1234-5678",
                code = "123456",
                expiresAt = LocalDateTime.now().plusMinutes(5),
            )

        every {
            verificationService.verifyCode(
                channel = VerificationChannel.PHONE,
                target = "010-1234-5678",
                code = "123456",
            )
        } returns verification
        every {
            tokenService.issueVerificationToken(
                channel = VerificationChannel.PHONE,
                target = "010-1234-5678",
            )
        } returns "encrypted-verification-token"

        val result = useCase.verifyCode(request)

        assertEquals("encrypted-verification-token", result.phoneVerificationToken)
    }

    @Test
    fun `잘못된 인증코드로 인증하면 VerificationCodeMismatchException이 발생한다`() {
        val request = VerifyPhoneCodeRequest(phoneNumber = "010-1234-5678", code = "999999")

        every {
            verificationService.verifyCode(
                channel = VerificationChannel.PHONE,
                target = "010-1234-5678",
                code = "999999",
            )
        } throws VerificationCodeMismatchException()

        assertThrows<VerificationCodeMismatchException> {
            useCase.verifyCode(request)
        }
    }
}
