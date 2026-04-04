package com.sclass.lms.diagnosis.usecase

import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.domain.DiagnosisStatus
import com.sclass.domain.domains.diagnosis.exception.DiagnosisPhoneNotMatchException
import com.sclass.domain.domains.verification.domain.Verification
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.exception.VerificationCodeMismatchException
import com.sclass.domain.domains.verification.exception.VerificationExpiredException
import com.sclass.domain.domains.verification.service.VerificationDomainService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class GetDiagnosisResultUseCaseTest {
    private val diagnosisAdaptor = mockk<DiagnosisAdaptor>()
    private val verificationService = mockk<VerificationDomainService>()
    private val useCase = GetDiagnosisResultUseCase(diagnosisAdaptor, verificationService)

    private fun createDiagnosis(
        studentPhone: String? = "010-1234-5678",
        parentPhone: String? = "010-9876-5432",
    ) = Diagnosis(
        requestId = "req-001",
        studentName = "홍길동",
        studentPhone = studentPhone,
        parentPhone = parentPhone,
        requestData = "{}",
    )

    private fun stubVerifyCode(
        formattedPhone: String,
        code: String,
    ) {
        every {
            verificationService.verifyCode(VerificationChannel.PHONE, formattedPhone, code)
        } returns
            Verification(
                channel = VerificationChannel.PHONE,
                target = formattedPhone,
                code = code,
                expiresAt = LocalDateTime.now().plusMinutes(5),
            )
    }

    @Test
    fun `학생 전화번호로 인증 성공 시 진단 결과를 반환한다`() {
        val diagnosis = createDiagnosis()
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis
        stubVerifyCode("010-1234-5678", "123456")

        val result = useCase.execute(diagnosis.id, "01012345678", "123456")

        assertAll(
            { assertEquals(diagnosis.id, result.id) },
            { assertEquals("홍길동", result.studentName) },
            { assertEquals(DiagnosisStatus.PENDING, result.status) },
            { assertNull(result.reportData) },
            { assertNull(result.resultUrl) },
        )
    }

    @Test
    fun `학부모 전화번호로 인증 성공 시 진단 결과를 반환한다`() {
        val diagnosis = createDiagnosis()
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis
        stubVerifyCode("010-9876-5432", "123456")

        val result = useCase.execute(diagnosis.id, "01098765432", "123456")

        assertEquals(diagnosis.id, result.id)
    }

    @Test
    fun `완료된 진단은 reportData와 resultUrl을 포함한다`() {
        val diagnosis = createDiagnosis().also { it.complete("{\"score\":95}") }
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis
        stubVerifyCode("010-1234-5678", "123456")

        val result = useCase.execute(diagnosis.id, "01012345678", "123456")

        assertAll(
            { assertEquals(DiagnosisStatus.COMPLETED, result.status) },
            { assertEquals("{\"score\":95}", result.reportData) },
            { assertEquals("https://report.aura.co.kr/${diagnosis.id}", result.resultUrl) },
        )
    }

    @Test
    fun `등록되지 않은 전화번호면 DiagnosisPhoneNotMatchException이 발생한다`() {
        val diagnosis = createDiagnosis()
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis

        assertThrows<DiagnosisPhoneNotMatchException> {
            useCase.execute(diagnosis.id, "01011111111", "123456")
        }
    }

    @Test
    fun `잘못된 OTP 코드면 VerificationCodeMismatchException이 발생한다`() {
        val diagnosis = createDiagnosis()
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis
        every {
            verificationService.verifyCode(VerificationChannel.PHONE, "010-1234-5678", "999999")
        } throws VerificationCodeMismatchException()

        assertThrows<VerificationCodeMismatchException> {
            useCase.execute(diagnosis.id, "01012345678", "999999")
        }
    }

    @Test
    fun `만료된 OTP면 VerificationExpiredException이 발생한다`() {
        val diagnosis = createDiagnosis()
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis
        every {
            verificationService.verifyCode(VerificationChannel.PHONE, "010-1234-5678", "123456")
        } throws VerificationExpiredException()

        assertThrows<VerificationExpiredException> {
            useCase.execute(diagnosis.id, "01012345678", "123456")
        }
    }
}
