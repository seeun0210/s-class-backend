package com.sclass.lms.auth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.service.VerificationDomainService
import com.sclass.infrastructure.email.EmailSender
import com.sclass.lms.auth.dto.SendEmailCodeRequest
import com.sclass.lms.auth.dto.SendEmailCodeResponse
import com.sclass.lms.auth.dto.VerifyEmailCodeRequest
import com.sclass.lms.auth.dto.VerifyEmailCodeResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class EmailVerificationUseCase(
    private val verificationService: VerificationDomainService,
    private val tokenService: TokenDomainService,
    private val emailSender: EmailSender,
) {
    @Transactional
    fun sendCode(request: SendEmailCodeRequest): SendEmailCodeResponse {
        val verification =
            verificationService.createVerification(
                channel = VerificationChannel.EMAIL,
                target = request.email,
            )
        emailSender.sendVerificationCode(request.email, verification.code)
        return SendEmailCodeResponse()
    }

    @Transactional
    fun verifyCode(request: VerifyEmailCodeRequest): VerifyEmailCodeResponse {
        verificationService.verifyCode(
            channel = VerificationChannel.EMAIL,
            target = request.email,
            code = request.code,
        )
        val token =
            tokenService.issueVerificationToken(
                channel = VerificationChannel.EMAIL,
                target = request.email,
            )
        return VerifyEmailCodeResponse(emailVerificationToken = token)
    }
}
