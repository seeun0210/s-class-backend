package com.sclass.supporters.diagnosis.dto

import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus

data class DiagnosisResultResponse(
    val id: String,
    val studentName: String,
    val status: DiagnosisStatus,
    val reportData: String?,
    val resultUrl: String?,
) {
    companion object {
        fun from(diagnosis: Diagnosis) =
            DiagnosisResultResponse(
                id = diagnosis.id,
                studentName = diagnosis.studentName,
                status = diagnosis.status,
                reportData = diagnosis.reportData,
                resultUrl = diagnosis.resultUrl,
            )
    }
}
