package com.sclass.lms.diagnosis.usecase

import com.sclass.domain.domains.diagnosis.adaptor.DiagnosisAdaptor
import com.sclass.domain.domains.diagnosis.domain.Diagnosis
import com.sclass.domain.domains.diagnosis.exception.DiagnosisNotFoundException
import com.sclass.domain.domains.diagnosis.exception.DiagnosisPhoneNotMatchException
import com.sclass.domain.domains.verification.domain.Verification
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.exception.VerificationSendRateLimitException
import com.sclass.domain.domains.verification.service.VerificationDomainService
import com.sclass.infrastructure.message.VerificationCodeSender
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class SendDiagnosisPhoneVerificationUseCaseTest {
    private val diagnosisAdaptor = mockk<DiagnosisAdaptor>()
    private val verificationService = mockk<VerificationDomainService>()
    private val messageSender = mockk<VerificationCodeSender>()
    private val useCase = SendDiagnosisPhoneVerificationUseCase(diagnosisAdaptor, verificationService, messageSender)

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

    private fun createVerification(phone: String) =
        Verification(
            channel = VerificationChannel.PHONE,
            target = phone,
            code = "123456",
            expiresAt = LocalDateTime.now().plusMinutes(5),
        )

    @Test
    fun `학생 전화번호로 OTP를 발송한다`() {
        val diagnosis = createDiagnosis()
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis
        every { verificationService.createVerification(VerificationChannel.PHONE, "010-1234-5678") } returns
            createVerification("010-1234-5678")
        every { messageSender.sendVerificationCode("010-1234-5678", "123456") } just runs

        useCase.execute(diagnosis.id, "01012345678")

        verify { messageSender.sendVerificationCode("010-1234-5678", "123456") }
    }

    @Test
    fun `학부모 전화번호로 OTP를 발송한다`() {
        val diagnosis = createDiagnosis()
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis
        every { verificationService.createVerification(VerificationChannel.PHONE, "010-9876-5432") } returns
            createVerification("010-9876-5432")
        every { messageSender.sendVerificationCode("010-9876-5432", "123456") } just runs

        useCase.execute(diagnosis.id, "01098765432")

        verify { messageSender.sendVerificationCode("010-9876-5432", "123456") }
    }

    @Test
    fun `등록되지 않은 전화번호면 DiagnosisPhoneNotMatchException이 발생한다`() {
        val diagnosis = createDiagnosis()
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis

        assertThrows<DiagnosisPhoneNotMatchException> {
            useCase.execute(diagnosis.id, "01011111111")
        }
    }

    @Test
    fun `존재하지 않는 진단 id면 DiagnosisNotFoundException이 발생한다`() {
        every { diagnosisAdaptor.findById("nonexistent") } throws DiagnosisNotFoundException()

        assertThrows<DiagnosisNotFoundException> {
            useCase.execute("nonexistent", "01012345678")
        }
    }

    @Test
    fun `OTP 발송 횟수 초과 시 VerificationSendRateLimitException이 발생한다`() {
        val diagnosis = createDiagnosis()
        every { diagnosisAdaptor.findById(diagnosis.id) } returns diagnosis
        every { verificationService.createVerification(VerificationChannel.PHONE, "010-1234-5678") } throws
            VerificationSendRateLimitException()

        assertThrows<VerificationSendRateLimitException> {
            useCase.execute(diagnosis.id, "01012345678")
        }
    }
}
