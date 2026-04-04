package com.sclass.domain.domains.diagnosis.repository

import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface DiagnosisCustomRepository {
    fun findAll(
        pageable: Pageable,
        status: DiagnosisStatus?,
    ): Page<Diagnosis>
}
