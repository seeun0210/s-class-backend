package com.sclass.backoffice.diagnosis.dto

import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import java.time.LocalDateTime

data class DiagnosisListItemResponse(
    val id: String,
    val studentName: String,
    val status: DiagnosisStatus,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(diagnosis: Diagnosis) =
            DiagnosisListItemResponse(
                id = diagnosis.id,
                studentName = diagnosis.studentName,
                status = diagnosis.status,
                createdAt = diagnosis.createdAt,
            )
    }
}
