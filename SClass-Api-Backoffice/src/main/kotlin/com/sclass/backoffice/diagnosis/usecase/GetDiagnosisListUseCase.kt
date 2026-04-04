package com.sclass.backoffice.diagnosis.usecase

import com.sclass.backoffice.diagnosis.dto.DiagnosisListItemResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetDiagnosisListUseCase(
    private val diagnosisAdaptor: DiagnosisAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        status: DiagnosisStatus?,
        pageable: Pageable,
    ): Page<DiagnosisListItemResponse> =
        diagnosisAdaptor.findAll(pageable, status).map {
            DiagnosisListItemResponse.from(it)
        }
}
