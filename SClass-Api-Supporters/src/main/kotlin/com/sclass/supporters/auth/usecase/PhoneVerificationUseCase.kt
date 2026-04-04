package com.sclass.supporters.auth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.service.VerificationDomainService
import com.sclass.infrastructure.message.VerificationCodeSender
import com.sclass.supporters.auth.dto.SendPhoneCodeRequest
import com.sclass.supporters.auth.dto.SendPhoneCodeResponse
import com.sclass.supporters.auth.dto.VerifyPhoneCodeRequest
import com.sclass.supporters.auth.dto.VerifyPhoneCodeResponse
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@UseCase
class PhoneVerificationUseCase(
    private val verificationService: VerificationDomainService,
    private val tokenService: TokenDomainService,
    private val messageSender: VerificationCodeSender,
) {
    @Transactional
    fun sendCode(request: SendPhoneCodeRequest): SendPhoneCodeResponse {
        val phoneNumber = User.formatPhoneNumber(request.phoneNumber)
        val verification =
            verificationService.createVerification(
                channel = VerificationChannel.PHONE,
                target = phoneNumber,
            )
        afterCommit { messageSender.sendVerificationCode(phoneNumber, verification.code) }
        return SendPhoneCodeResponse()
    }

    @Transactional
    fun verifyCode(request: VerifyPhoneCodeRequest): VerifyPhoneCodeResponse {
        val phoneNumber = User.formatPhoneNumber(request.phoneNumber)
        verificationService.verifyCode(
            channel = VerificationChannel.PHONE,
            target = phoneNumber,
            code = request.code,
        )
        val token =
            tokenService.issueVerificationToken(
                channel = VerificationChannel.PHONE,
                target = phoneNumber,
            )
        return VerifyPhoneCodeResponse(phoneVerificationToken = token)
    }

    private fun afterCommit(action: () -> Unit) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() = action()
                },
            )
        } else {
            action()
        }
    }
}
