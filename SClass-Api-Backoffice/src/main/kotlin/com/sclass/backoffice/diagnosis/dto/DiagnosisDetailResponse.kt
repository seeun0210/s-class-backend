package com.sclass.backoffice.diagnosis.dto

import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import java.time.LocalDateTime

data class DiagnosisDetailResponse(
    val id: String,
    val studentName: String,
    val studentPhone: String?,
    val parentPhone: String?,
    val status: DiagnosisStatus,
    val reportData: String?,
    val resultUrl: String?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(diagnosis: Diagnosis) =
            DiagnosisDetailResponse(
                id = diagnosis.id,
                studentName = diagnosis.studentName,
                studentPhone = diagnosis.studentPhone,
                parentPhone = diagnosis.parentPhone,
                status = diagnosis.status,
                reportData = diagnosis.reportData,
                resultUrl = diagnosis.resultUrl,
                createdAt = diagnosis.createdAt,
            )
    }
}
