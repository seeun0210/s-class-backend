package com.sclass.backoffice.diagnosis.usecase

import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import com.sclass.domain.domains.diagnosis.exception.DiagnosisNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetDiagnosisDetailUseCaseTest {
    private val diagnosisAdaptor = mockk<DiagnosisAdaptor>()
    private val useCase = GetDiagnosisDetailUseCase(diagnosisAdaptor)

    private fun createDiagnosis() =
        Diagnosis(
            requestId = "req-001",
            studentName = "홍길동",
            studentPhone = "01012345678",
            parentPhone = "01098765432",
            requestData = "{}",
        )

    @Test
    fun `진단 상세 정보를 반환한다`() {
        val diagnosis = createDiagnosis()
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis

        val result = useCase.execute(diagnosis.id)

        assertAll(
            { assertEquals(diagnosis.id, result.id) },
            { assertEquals("홍길동", result.studentName) },
            { assertEquals("01012345678", result.studentPhone) },
            { assertEquals("01098765432", result.parentPhone) },
            { assertEquals(DiagnosisStatus.PENDING, result.status) },
            { assertNull(result.reportData) },
            { assertNull(result.resultUrl) },
        )
    }

    @Test
    fun `완료된 진단은 reportData와 resultUrl을 포함한다`() {
        val diagnosis = createDiagnosis().also { it.complete("{\"score\":95}") }
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis

        val result = useCase.execute(diagnosis.id)

        assertAll(
            { assertEquals(DiagnosisStatus.COMPLETED, result.status) },
            { assertEquals("{\"score\":95}", result.reportData) },
            { assertEquals("https://report.aura.co.kr/${diagnosis.id}", result.resultUrl) },
        )
    }

    @Test
    fun `존재하지 않는 id면 예외를 던진다`() {
        every { diagnosisAdaptor.findById("nonexistent") } throws DiagnosisNotFoundException()

        assertThrows<DiagnosisNotFoundException> {
            useCase.execute("nonexistent")
        }
    }
}
