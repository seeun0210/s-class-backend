package com.sclass.lms.diagnosis.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.exception.DiagnosisPhoneNotMatchException
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.service.VerificationDomainService
import com.sclass.infrastructure.message.VerificationCodeSender
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@UseCase
class SendDiagnosisPhoneVerificationUseCase(
    private val diagnosisAdaptor: DiagnosisAdaptor,
    private val verificationService: VerificationDomainService,
    private val messageSender: VerificationCodeSender,
) {
    @Transactional
    fun execute(
        diagnosisId: String,
        phone: String,
    ) {
        val formatted = User.formatPhoneNumber(phone)
        val diagnosis = diagnosisAdaptor.findById(diagnosisId)

        if (diagnosis.studentPhone != formatted && diagnosis.parentPhone != formatted) {
            throw DiagnosisPhoneNotMatchException()
        }

        val verification =
            verificationService.createVerification(
                channel = VerificationChannel.PHONE,
                target = formatted,
            )
        afterCommit { messageSender.sendVerificationCode(formatted, verification.code) }
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
