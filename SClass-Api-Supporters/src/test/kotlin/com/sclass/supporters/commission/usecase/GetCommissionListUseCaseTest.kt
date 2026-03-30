package com.sclass.supporters.commission.usecase

import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.user.domain.Role
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCommissionListUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var useCase: GetCommissionListUseCase

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        useCase = GetCommissionListUseCase(commissionAdaptor)
    }

    private fun createCommission(
        id: Long,
        studentUserId: String = "student-id",
        teacherUserId: String = "teacher-id",
    ) = Commission(
        id = id,
        studentUserId = studentUserId,
        teacherUserId = teacherUserId,
        outputFormat = OutputFormat.REPORT,
        activityType = ActivityType.CAREER_EXPLORATION,
        status = CommissionStatus.REQUESTED,
        guideInfo =
            GuideInfo(
                subject = "미시경제학",
                volume = "A4 3매",
                gradingCriteria = "평가기준",
                teacherEmphasis = "강조사항",
            ),
    )

    @Test
    fun `STUDENT 역할이면 학생 의뢰 목록을 반환한다`() {
        val commissions = listOf(createCommission(1L), createCommission(2L))
        every { commissionAdaptor.findByStudentUserId("user-id") } returns commissions

        val result = useCase.execute("user-id", Role.STUDENT)

        assertEquals(2, result.commissions.size)
        verify { commissionAdaptor.findByStudentUserId("user-id") }
    }

    @Test
    fun `TEACHER 역할이면 선생님 의뢰 목록을 반환한다`() {
        val commissions = listOf(createCommission(1L))
        every { commissionAdaptor.findByTeacherUserId("user-id") } returns commissions

        val result = useCase.execute("user-id", Role.TEACHER)

        assertEquals(1, result.commissions.size)
        verify { commissionAdaptor.findByTeacherUserId("user-id") }
    }

    @Test
    fun `의뢰가 없으면 빈 목록을 반환한다`() {
        every { commissionAdaptor.findByStudentUserId("user-id") } returns emptyList()

        val result = useCase.execute("user-id", Role.STUDENT)

        assertEquals(0, result.commissions.size)
    }
}
