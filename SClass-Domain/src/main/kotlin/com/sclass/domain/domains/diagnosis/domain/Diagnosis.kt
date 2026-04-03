package com.sclass.domain.domains.diagnosis.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "diagnoses")
class Diagnosis(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(length = 26, unique = true)
    val requestId: String? = null,

    @Column(nullable = false)
    val studentName: String,

    val studentPhone: String?,

    val parentPhone: String?,

    @Column(columnDefinition = "TEXT", nullable = false)
    val requestData: String,

    val callbackSecret: String =
        java.util.UUID
            .randomUUID()
            .toString(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DiagnosisStatus = DiagnosisStatus.PENDING,

    var resultUrl: String? = null,

    @Column(columnDefinition = "TEXT")
    var reportData: String? = null,
) : BaseTimeEntity() {
    fun markProcessing() {
        status = DiagnosisStatus.PROCESSING
    }

    fun complete(reportData: String) {
        status = DiagnosisStatus.COMPLETED
        this.reportData = reportData
        this.resultUrl = "https://report.aura.co.kr/$id"
    }

    fun fail() {
        status = DiagnosisStatus.FAILED
    }
}
