package com.sclass.domain.domains.diagnosis.adaptor

import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.exception.DiagnosisNotFoundException
import com.sclass.domain.domains.diagnosis.repository.DiagnosisRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Optional

class DiagnosisAdaptorTest {
    private val diagnosisRepository = mockk<DiagnosisRepository>()
    private val adaptor = DiagnosisAdaptor(diagnosisRepository)

    private fun createDiagnosis() =
        Diagnosis(
            studentName = "홍길동",
            studentPhone = "010-1234-5678",
            parentPhone = null,
            requestData = "{}",
        )

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 diagnosis를 반환한다`() {
            val diagnosis = createDiagnosis()
            every { diagnosisRepository.findById(diagnosis.id) } returns Optional.of(diagnosis)

            val result = adaptor.findById(diagnosis.id)

            assertEquals(diagnosis, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 DiagnosisNotFoundException을 던진다`() {
            every { diagnosisRepository.findById(any()) } returns Optional.empty()

            assertThrows(DiagnosisNotFoundException::class.java) {
                adaptor.findById(Ulid.generate())
            }
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `diagnosis를 저장하고 반환한다`() {
            val diagnosis = createDiagnosis()
            every { diagnosisRepository.save(diagnosis) } returns diagnosis

            val result = adaptor.save(diagnosis)

            verify { diagnosisRepository.save(diagnosis) }
            assertEquals(diagnosis, result)
        }
    }
}
