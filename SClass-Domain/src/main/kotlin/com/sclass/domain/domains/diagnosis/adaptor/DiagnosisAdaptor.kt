package com.sclass.domain.domains.diagnosis.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import com.sclass.domain.domains.diagnosis.exception.DiagnosisNotFoundException
import com.sclass.domain.domains.diagnosis.repository.DiagnosisRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class DiagnosisAdaptor(
    private val diagnosisRepository: DiagnosisRepository,
) {
    fun save(diagnosis: Diagnosis): Diagnosis = diagnosisRepository.save(diagnosis)

    fun findById(id: String): Diagnosis = diagnosisRepository.findById(id).orElseThrow { DiagnosisNotFoundException() }

    fun findByRequestId(requestId: String): Diagnosis = diagnosisRepository.findByRequestId(requestId) ?: throw DiagnosisNotFoundException()

    fun findAll(
        pageable: Pageable,
        status: DiagnosisStatus?,
    ): Page<Diagnosis> = diagnosisRepository.findAll(pageable, status)
}
