package com.sclass.backoffice.diagnosis.usecase

import com.sclass.backoffice.diagnosis.dto.DiagnosisDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetDiagnosisDetailUseCase(
    private val diagnosisAdaptor: DiagnosisAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(id: String): DiagnosisDetailResponse = DiagnosisDetailResponse.from(diagnosisAdaptor.findById(id))
}
