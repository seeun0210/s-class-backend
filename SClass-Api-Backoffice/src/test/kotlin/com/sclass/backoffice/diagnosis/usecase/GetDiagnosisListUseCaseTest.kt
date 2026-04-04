package com.sclass.backoffice.diagnosis.usecase

import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetDiagnosisListUseCaseTest {
    private val diagnosisAdaptor = mockk<DiagnosisAdaptor>()
    private val useCase = GetDiagnosisListUseCase(diagnosisAdaptor)

    private fun createDiagnosis(
        studentName: String = "홍길동",
        status: DiagnosisStatus = DiagnosisStatus.PENDING,
    ) = Diagnosis(
        requestId = "req-001",
        studentName = studentName,
        studentPhone = "01012345678",
        parentPhone = "01098765432",
        requestData = "{}",
    ).also {
        when (status) {
            DiagnosisStatus.PROCESSING -> it.markProcessing()
            DiagnosisStatus.COMPLETED -> it.complete("{\"score\":95}")
            DiagnosisStatus.FAILED -> it.fail()
            DiagnosisStatus.PENDING -> Unit
        }
    }

    @Test
    fun `진단 목록을 페이지로 반환한다`() {
        val diagnoses = listOf(createDiagnosis("홍길동"), createDiagnosis("김철수"))
        val pageable = PageRequest.of(0, 20)
        every { diagnosisAdaptor.findAll(pageable, null) } returns PageImpl(diagnoses, pageable, 2)

        val result = useCase.execute(null, pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertEquals("홍길동", result.content[0].studentName) },
            { assertEquals("김철수", result.content[1].studentName) },
        )
    }

    @Test
    fun `status 필터를 적용하면 해당 상태의 진단만 반환한다`() {
        val completed = createDiagnosis(status = DiagnosisStatus.COMPLETED)
        val pageable = PageRequest.of(0, 20)
        every { diagnosisAdaptor.findAll(pageable, DiagnosisStatus.COMPLETED) } returns PageImpl(listOf(completed), pageable, 1)

        val result = useCase.execute(DiagnosisStatus.COMPLETED, pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(DiagnosisStatus.COMPLETED, result.content[0].status) },
        )
    }

    @Test
    fun `진단이 없으면 빈 페이지를 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        every { diagnosisAdaptor.findAll(pageable, null) } returns PageImpl(emptyList(), pageable, 0)

        val result = useCase.execute(null, pageable)

        assertEquals(0, result.totalElements)
    }
}
