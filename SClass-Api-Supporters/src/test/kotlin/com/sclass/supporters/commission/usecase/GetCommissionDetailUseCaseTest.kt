package com.sclass.supporters.commission.usecase

import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.commission.exception.CommissionNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetCommissionDetailUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var useCase: GetCommissionDetailUseCase

    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        useCase = GetCommissionDetailUseCase(commissionAdaptor)
    }

    private fun createCommission(id: Long = 1L) =
        Commission(
            id = id,
            studentUserId = studentUserId,
            teacherUserId = teacherUserId,
            outputFormat = OutputFormat.REPORT,
            activityType = ActivityType.CAREER_EXPLORATION,
            status = CommissionStatus.REQUESTED,
            guideInfo =
                GuideInfo(
                    subject = "미시경제학",
                    volume = "A4 3매 이내",
                    gradingCriteria = "평가기준",
                    teacherEmphasis = "강조사항",
                ),
        )

    @Test
    fun `학생이 본인 의뢰를 조회하면 CommissionResponse를 반환한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        val result = useCase.execute(studentUserId, 1L)

        assertAll(
            { assertEquals(1L, result.id) },
            { assertEquals(OutputFormat.REPORT, result.outputFormat) },
            { assertEquals("미시경제학", result.guideInfo.subject) },
        )
    }

    @Test
    fun `선생님이 담당 의뢰를 조회하면 CommissionResponse를 반환한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        val result = useCase.execute(teacherUserId, 1L)

        assertEquals(1L, result.id)
    }

    @Test
    fun `관련 없는 유저가 의뢰를 조회하면 예외가 발생한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute("other-user-id-00000000001", 1L)
        }
    }

    @Test
    fun `존재하지 않는 의뢰를 조회하면 예외가 발생한다`() {
        every { commissionAdaptor.findById(999L) } throws CommissionNotFoundException()

        assertThrows<CommissionNotFoundException> {
            useCase.execute(studentUserId, 999L)
        }
    }
}
