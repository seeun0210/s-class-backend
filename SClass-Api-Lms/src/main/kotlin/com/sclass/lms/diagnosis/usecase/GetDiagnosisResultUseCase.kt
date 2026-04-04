package com.sclass.lms.diagnosis.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.exception.DiagnosisPhoneNotMatchException
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.service.VerificationDomainService
import com.sclass.lms.diagnosis.dto.DiagnosisResultResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetDiagnosisResultUseCase(
    private val diagnosisAdaptor: DiagnosisAdaptor,
    private val verificationService: VerificationDomainService,
) {
    @Transactional
    fun execute(
        diagnosisId: String,
        phone: String,
        code: String,
    ): DiagnosisResultResponse {
        val formatted = User.formatPhoneNumber(phone)
        val diagnosis = diagnosisAdaptor.findById(diagnosisId)

        if (diagnosis.studentPhone != formatted && diagnosis.parentPhone != formatted) {
            throw DiagnosisPhoneNotMatchException()
        }

        verificationService.verifyCode(
            channel = VerificationChannel.PHONE,
            target = formatted,
            code = code,
        )

        return DiagnosisResultResponse.from(diagnosis)
    }
}
