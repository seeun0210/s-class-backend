package com.sclass.supporters.auth.usecase

import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.verification.domain.Verification
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.exception.VerificationCodeMismatchException
import com.sclass.domain.domains.verification.exception.VerificationSendRateLimitException
import com.sclass.domain.domains.verification.service.VerificationDomainService
import com.sclass.infrastructure.email.EmailSender
import com.sclass.supporters.auth.dto.SendEmailCodeRequest
import com.sclass.supporters.auth.dto.VerifyEmailCodeRequest
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

class EmailVerificationUseCaseTest {
    private lateinit var verificationService: VerificationDomainService
    private lateinit var tokenService: TokenDomainService
    private lateinit var emailSender: EmailSender
    private lateinit var useCase: EmailVerificationUseCase

    @BeforeEach
    fun setUp() {
        verificationService = mockk()
        tokenService = mockk()
        emailSender = mockk()
        useCase = EmailVerificationUseCase(verificationService, tokenService, emailSender)
    }

    @Test
    fun `인증코드 발송 요청 시 인증을 생성하고 이메일을 발송한다`() {
        val request = SendEmailCodeRequest(email = "test@example.com")
        val verification =
            Verification(
                channel = VerificationChannel.EMAIL,
                target = "test@example.com",
                code = "123456",
                expiresAt = LocalDateTime.now().plusMinutes(5),
            )

        every {
            verificationService.createVerification(
                channel = VerificationChannel.EMAIL,
                target = "test@example.com",
            )
        } returns verification
        every { emailSender.sendVerificationCode("test@example.com", "123456", any()) } just runs

        val result = useCase.sendCode(request)

        assertNotNull(result)
        assertEquals(300L, result.expiresInSeconds)
        verify { emailSender.sendVerificationCode("test@example.com", "123456", "S-Class Supporters") }
    }

    @Test
    fun `발송 횟수 초과 시 VerificationSendRateLimitException이 발생한다`() {
        val request = SendEmailCodeRequest(email = "test@example.com")

        every {
            verificationService.createVerification(
                channel = VerificationChannel.EMAIL,
                target = "test@example.com",
            )
        } throws VerificationSendRateLimitException()

        assertThrows<VerificationSendRateLimitException> {
            useCase.sendCode(request)
        }
    }

    @Test
    fun `올바른 인증코드로 인증하면 emailVerificationToken을 반환한다`() {
        val request = VerifyEmailCodeRequest(email = "test@example.com", code = "123456")
        val verification =
            Verification(
                channel = VerificationChannel.EMAIL,
                target = "test@example.com",
                code = "123456",
                expiresAt = LocalDateTime.now().plusMinutes(5),
            )

        every {
            verificationService.verifyCode(
                channel = VerificationChannel.EMAIL,
                target = "test@example.com",
                code = "123456",
            )
        } returns verification
        every {
            tokenService.issueVerificationToken(
                channel = VerificationChannel.EMAIL,
                target = "test@example.com",
            )
        } returns "encrypted-verification-token"

        val result = useCase.verifyCode(request)

        assertEquals("encrypted-verification-token", result.emailVerificationToken)
    }

    @Test
    fun `잘못된 인증코드로 인증하면 VerificationCodeMismatchException이 발생한다`() {
        val request = VerifyEmailCodeRequest(email = "test@example.com", code = "999999")

        every {
            verificationService.verifyCode(
                channel = VerificationChannel.EMAIL,
                target = "test@example.com",
                code = "999999",
            )
        } throws VerificationCodeMismatchException()

        assertThrows<VerificationCodeMismatchException> {
            useCase.verifyCode(request)
        }
    }
}
