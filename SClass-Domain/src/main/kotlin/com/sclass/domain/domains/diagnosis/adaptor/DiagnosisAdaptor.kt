package com.sclass.domain.domains.diagnosis.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.exception.DiagnosisNotFoundException
import com.sclass.domain.domains.diagnosis.repository.DiagnosisRepository

@Adaptor
class DiagnosisAdaptor(
    private val diagnosisRepository: DiagnosisRepository,
) {
    fun save(diagnosis: Diagnosis): Diagnosis = diagnosisRepository.save(diagnosis)

    fun findById(id: String): Diagnosis = diagnosisRepository.findById(id).orElseThrow { DiagnosisNotFoundException() }
}
