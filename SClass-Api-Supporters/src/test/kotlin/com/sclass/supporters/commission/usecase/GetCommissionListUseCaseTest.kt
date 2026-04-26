package com.sclass.supporters.commission.usecase

import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.user.domain.Role
import com.sclass.supporters.commission.dto.CommissionSummaryStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetCommissionListUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var useCase: GetCommissionListUseCase

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        lessonAdaptor = mockk()
        useCase = GetCommissionListUseCase(commissionAdaptor, lessonAdaptor)
    }

    private fun createCommission(
        id: Long,
        studentUserId: String = "student-id",
        teacherUserId: String = "teacher-id",
        status: CommissionStatus = CommissionStatus.REQUESTED,
        acceptedLessonId: Long? = null,
    ) = Commission(
        id = id,
        studentUserId = studentUserId,
        teacherUserId = teacherUserId,
        commissionPolicyId = "policy-id-0000000000000001",
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
        acceptedLessonId = acceptedLessonId,
    )

    private fun createLesson(
        id: Long,
        status: LessonStatus = LessonStatus.SCHEDULED,
        completedAt: LocalDateTime? = null,
    ) = Lesson(
        id = id,
        lessonType = LessonType.COMMISSION,
        sourceCommissionId = 1L,
        studentUserId = "student-id",
        assignedTeacherUserId = "teacher-id",
        name = "의뢰 수업",
        status = status,
        completedAt = completedAt,
    )

    @Test
    fun `STUDENT 역할이면 학생 의뢰 목록을 반환한다`() {
        val commissions = listOf(createCommission(1L), createCommission(2L))
        every { commissionAdaptor.findByStudentUserId("user-id") } returns commissions

        val result = useCase.execute("user-id", Role.STUDENT)

        assertEquals(2, result.commissions.size)
        verify { commissionAdaptor.findByStudentUserId("user-id") }
        verify(exactly = 0) { lessonAdaptor.findAllByIds(any()) }
    }

    @Test
    fun `TEACHER 역할이면 선생님 의뢰 목록을 반환한다`() {
        val commissions = listOf(createCommission(1L))
        every { commissionAdaptor.findByTeacherUserId("user-id") } returns commissions

        val result = useCase.execute("user-id", Role.TEACHER)

        assertEquals(1, result.commissions.size)
        verify { commissionAdaptor.findByTeacherUserId("user-id") }
        verify(exactly = 0) { lessonAdaptor.findAllByIds(any()) }
    }

    @Test
    fun `의뢰가 없으면 빈 목록을 반환한다`() {
        every { commissionAdaptor.findByStudentUserId("user-id") } returns emptyList()

        val result = useCase.execute("user-id", Role.STUDENT)

        assertEquals(0, result.commissions.size)
        verify(exactly = 0) { lessonAdaptor.findAllByIds(any()) }
    }

    @Test
    fun `연결된 lesson이 완료 상태이면 목록 status를 COMPLETED로 반환한다`() {
        val completedAt = LocalDateTime.of(2026, 4, 26, 10, 30)
        val commissions =
            listOf(
                createCommission(id = 1L, status = CommissionStatus.ACCEPTED, acceptedLessonId = 100L),
                createCommission(id = 2L, status = CommissionStatus.REQUESTED),
            )
        val lesson = createLesson(id = 100L, status = LessonStatus.COMPLETED, completedAt = completedAt)
        every { commissionAdaptor.findByStudentUserId("user-id") } returns commissions
        every { lessonAdaptor.findAllByIds(listOf(100L)) } returns listOf(lesson)

        val result = useCase.execute("user-id", Role.STUDENT)

        assertAll(
            { assertEquals(CommissionSummaryStatus.COMPLETED, result.commissions[0].status) },
            { assertEquals(CommissionSummaryStatus.REQUESTED, result.commissions[1].status) },
        )
        verify { lessonAdaptor.findAllByIds(listOf(100L)) }
    }

    @Test
    fun `연결된 lesson이 완료 전이면 기존 commission status를 반환한다`() {
        val commissions =
            listOf(
                createCommission(id = 1L, status = CommissionStatus.ACCEPTED, acceptedLessonId = 100L),
            )
        val lesson = createLesson(id = 100L, status = LessonStatus.SCHEDULED)
        every { commissionAdaptor.findByStudentUserId("user-id") } returns commissions
        every { lessonAdaptor.findAllByIds(listOf(100L)) } returns listOf(lesson)

        val result = useCase.execute("user-id", Role.STUDENT)

        assertEquals(CommissionSummaryStatus.ACCEPTED, result.commissions[0].status)
    }

    @Test
    fun `연결된 lesson이 진행 상태이면 목록 status를 IN_PROGRESS로 반환한다`() {
        val commissions =
            listOf(
                createCommission(id = 1L, status = CommissionStatus.ACCEPTED, acceptedLessonId = 100L),
            )
        val lesson = createLesson(id = 100L, status = LessonStatus.IN_PROGRESS)
        every { commissionAdaptor.findByStudentUserId("user-id") } returns commissions
        every { lessonAdaptor.findAllByIds(listOf(100L)) } returns listOf(lesson)

        val result = useCase.execute("user-id", Role.STUDENT)

        assertEquals(CommissionSummaryStatus.IN_PROGRESS, result.commissions[0].status)
    }
}
