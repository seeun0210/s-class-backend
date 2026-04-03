package com.sclass.domain.domains.diagnosis.domain

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DiagnosisTest {
    private fun createDiagnosis() =
        Diagnosis(
            studentName = "홍길동",
            studentPhone = "010-1234-5678",
            parentPhone = null,
            requestData = "{}",
        )

    @Test
    fun `markProcessing 호출 시 status가 PROCESSING으로 변경된다`() {
        val diagnosis = createDiagnosis()
        diagnosis.markProcessing()
        assertEquals(DiagnosisStatus.PROCESSING, diagnosis.status)
    }

    @Test
    fun `complete 호출 시 status가 COMPLETED이고 resultUrl이 설정된다`() {
        val diagnosis = createDiagnosis()
        diagnosis.complete("https://result.example.com/123")
        assertAll(
            { assertEquals(DiagnosisStatus.COMPLETED, diagnosis.status) },
            { assertEquals("https://result.example.com/123", diagnosis.resultUrl) },
        )
    }

    @Test
    fun `fail 호출 시 status가 FAILED로 변경된다`() {
        val diagnosis = createDiagnosis()
        diagnosis.fail()
        assertEquals(DiagnosisStatus.FAILED, diagnosis.status)
    }
}
