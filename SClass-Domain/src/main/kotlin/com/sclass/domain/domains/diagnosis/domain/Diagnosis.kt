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

    @Column(nullable = false)
    val studentName: String,

    val studentPhone: String?,

    val parentPhone: String?,

    @Column(columnDefinition = "TEXT", nullable = false)
    val requestData: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DiagnosisStatus = DiagnosisStatus.PENDING,

    var resultUrl: String? = null,
) : BaseTimeEntity() {
    fun markProcessing() {
        status = DiagnosisStatus.PROCESSING
    }

    fun complete(resultUrl: String) {
        status = DiagnosisStatus.COMPLETED
        this.resultUrl = resultUrl
    }

    fun fail() {
        status = DiagnosisStatus.FAILED
    }
}
