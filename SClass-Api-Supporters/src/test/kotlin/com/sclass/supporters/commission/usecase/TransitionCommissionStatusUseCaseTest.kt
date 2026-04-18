package com.sclass.supporters.commission.usecase

import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.supporters.commission.dto.TransitionStatusRequest
import com.sclass.supporters.commission.scheduler.CommissionReminderScheduler
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TransitionCommissionStatusUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var commissionReminderScheduler: CommissionReminderScheduler
    private lateinit var useCase: TransitionCommissionStatusUseCase

    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-00000000001"
    private val commissionPolicyId = "policy-id-0000000000000001"

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        commissionReminderScheduler = mockk(relaxed = true)
        useCase = TransitionCommissionStatusUseCase(commissionAdaptor, commissionReminderScheduler)
    }

    private fun createCommission(
        id: Long = 1L,
        status: CommissionStatus = CommissionStatus.REQUESTED,
    ) = Commission(
        id = id,
        studentUserId = studentUserId,
        teacherUserId = teacherUserId,
        commissionPolicyId = commissionPolicyId,
        outputFormat = OutputFormat.REPORT,
        activityType = ActivityType.CAREER_EXPLORATION,
        status = status,
        guideInfo =
            GuideInfo(
                subject = "미시경제학",
                volume = "A4 3매",
                gradingCriteria = "평가기준",
                teacherEmphasis = "강조사항",
            ),
    )

    // --- REJECT ---

    @Test
    fun `선생님이 거절하면 REJECTED로 변경된다`() {
        val commission = createCommission(status = CommissionStatus.REQUESTED)
        every { commissionAdaptor.findById(1L) } returns commission

        val result = useCase.execute(teacherUserId, 1L, TransitionStatusRequest(status = CommissionStatus.REJECTED))

        assertEquals(CommissionStatus.REJECTED, result.status)
    }

    @Test
    fun `학생이 거절하면 예외가 발생한다`() {
        val commission = createCommission(status = CommissionStatus.REQUESTED)
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute(studentUserId, 1L, TransitionStatusRequest(status = CommissionStatus.REJECTED))
        }
    }

    // --- CANCEL ---

    @Test
    fun `학생이 취소하면 CANCELLED로 변경된다`() {
        val commission = createCommission(status = CommissionStatus.REQUESTED)
        every { commissionAdaptor.findById(1L) } returns commission

        val result = useCase.execute(studentUserId, 1L, TransitionStatusRequest(status = CommissionStatus.CANCELLED))

        assertEquals(CommissionStatus.CANCELLED, result.status)
    }

    @Test
    fun `선생님이 취소하면 예외가 발생한다`() {
        val commission = createCommission(status = CommissionStatus.REQUESTED)
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute(teacherUserId, 1L, TransitionStatusRequest(status = CommissionStatus.CANCELLED))
        }
    }

    // --- INVALID ---

    @Test
    fun `허용되지 않은 상태로 전이하면 예외가 발생한다`() {
        val commission = createCommission(status = CommissionStatus.REQUESTED)
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute(teacherUserId, 1L, TransitionStatusRequest(status = CommissionStatus.TOPIC_PROPOSED))
        }
    }
}
