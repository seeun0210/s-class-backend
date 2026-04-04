package com.sclass.domain.domains.diagnosis.repository

import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import org.springframework.data.jpa.repository.JpaRepository

interface DiagnosisRepository :
    JpaRepository<Diagnosis, String>,
    DiagnosisCustomRepository {
    fun findByRequestId(requestId: String): Diagnosis?
}
