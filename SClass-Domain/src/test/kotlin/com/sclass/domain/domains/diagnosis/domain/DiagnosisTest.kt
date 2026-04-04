package com.sclass.domain.domains.diagnosis.domain

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiagnosisTest {
    private fun createDiagnosis() =
        Diagnosis(
            requestId = "req-001",
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
    fun `complete 호출 시 status가 COMPLETED이고 reportData와 resultUrl이 설정된다`() {
        val diagnosis = createDiagnosis()
        diagnosis.complete("{\"score\":95}")
        assertAll(
            { assertEquals(DiagnosisStatus.COMPLETED, diagnosis.status) },
            { assertEquals("{\"score\":95}", diagnosis.reportData) },
            { assertEquals("https://report.aura.co.kr/reports/${diagnosis.id}", diagnosis.resultUrl) },
        )
    }

    @Test
    fun `생성 시 callbackSecret이 자동으로 UUID 형식으로 생성된다`() {
        val diagnosis = createDiagnosis()
        val uuidRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
        assertTrue(uuidRegex.matches(diagnosis.callbackSecret))
    }

    @Test
    fun `두 Diagnosis의 callbackSecret은 서로 다르다`() {
        val d1 = createDiagnosis()
        val d2 = createDiagnosis()
        assertTrue(d1.callbackSecret != d2.callbackSecret)
    }

    @Test
    fun `fail 호출 시 status가 FAILED로 변경된다`() {
        val diagnosis = createDiagnosis()
        diagnosis.fail()
        assertEquals(DiagnosisStatus.FAILED, diagnosis.status)
    }
}
