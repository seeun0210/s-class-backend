package com.sclass.backoffice.commission.usecase

import com.sclass.backoffice.commission.dto.CommissionListStatus
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.commission.dto.CommissionWithDetailDto
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetCommissionListUseCaseTest {
    private val commissionAdaptor = mockk<CommissionAdaptor>()
    private val useCase = GetCommissionListUseCase(commissionAdaptor)

    private fun createCommission(
        studentUserId: String = "student01",
        teacherUserId: String = "teacher01",
        status: CommissionStatus = CommissionStatus.REQUESTED,
    ) = Commission(
        id = 1L,
        studentUserId = studentUserId,
        teacherUserId = teacherUserId,
        commissionPolicyId = "policy01",
        outputFormat = OutputFormat.REPORT,
        activityType = ActivityType.CAREER_EXPLORATION,
        status = status,
        guideInfo =
            GuideInfo(
                subject = "진로탐색",
                volume = "A4 3장",
                requiredElements = null,
                gradingCriteria = "평가 기준",
                teacherEmphasis = "강조 사항",
            ),
    )

    private fun createLesson(status: LessonStatus = LessonStatus.SCHEDULED) =
        Lesson(
            id = 1L,
            lessonType = LessonType.COMMISSION,
            sourceCommissionId = 1L,
            studentUserId = "student01",
            assignedTeacherUserId = "teacher01",
            name = "탐구 수업 1회차",
            status = status,
        )

    private fun createDto(
        studentUserId: String = "student01",
        teacherUserId: String = "teacher01",
        studentName: String = "홍길동",
        teacherName: String = "김선생",
        status: CommissionStatus = CommissionStatus.REQUESTED,
        lesson: Lesson? = null,
    ) = CommissionWithDetailDto(
        commission = createCommission(studentUserId, teacherUserId, status),
        studentName = studentName,
        teacherName = teacherName,
        lesson = lesson,
    )

    @Test
    fun `commission 목록을 페이지로 반환한다`() {
        val dtos =
            listOf(
                createDto(studentName = "홍길동", teacherName = "김선생"),
                createDto(studentName = "김철수", teacherName = "이선생"),
            )
        val pageable = PageRequest.of(0, 20)
        every { commissionAdaptor.searchCommissions(null, null, null, pageable) } returns PageImpl(dtos, pageable, 2)

        val result = useCase.execute(null, null, null, pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertEquals(1, result.totalPages) },
            { assertEquals(0, result.currentPage) },
            { assertEquals("홍길동", result.content[0].studentName) },
            { assertEquals("김선생", result.content[0].teacherName) },
            { assertEquals("김철수", result.content[1].studentName) },
            { assertEquals("이선생", result.content[1].teacherName) },
        )
    }

    @Test
    fun `studentUserId 필터를 적용하면 해당 학생의 commission만 반환한다`() {
        val dto = createDto(studentUserId = "student01", studentName = "홍길동")
        val pageable = PageRequest.of(0, 20)
        every { commissionAdaptor.searchCommissions("student01", null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute("student01", null, null, pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals("student01", result.content[0].studentUserId) },
            { assertEquals("홍길동", result.content[0].studentName) },
        )
    }

    @Test
    fun `teacherUserId 필터를 적용하면 해당 선생님의 commission만 반환한다`() {
        val dto = createDto(teacherUserId = "teacher01", teacherName = "김선생")
        val pageable = PageRequest.of(0, 20)
        every { commissionAdaptor.searchCommissions(null, "teacher01", null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, "teacher01", null, pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals("teacher01", result.content[0].teacherUserId) },
            { assertEquals("김선생", result.content[0].teacherName) },
        )
    }

    @Test
    fun `status 필터를 적용하면 해당 상태의 commission만 반환한다`() {
        val dto = createDto(status = CommissionStatus.ACCEPTED)
        val pageable = PageRequest.of(0, 20)
        every { commissionAdaptor.searchCommissions(null, null, CommissionStatus.ACCEPTED, pageable) } returns
            PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, CommissionStatus.ACCEPTED, pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(CommissionListStatus.ACCEPTED, result.content[0].status) },
        )
    }

    @Test
    fun `lesson이 있는 commission은 lessonSummary가 포함된다`() {
        val lesson = createLesson()
        val dto = createDto(lesson = lesson)
        val pageable = PageRequest.of(0, 20)
        every { commissionAdaptor.searchCommissions(null, null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, null, pageable)

        assertAll(
            { assertNotNull(result.content[0].lesson) },
            { assertEquals(1L, result.content[0].lesson!!.id) },
            { assertEquals("탐구 수업 1회차", result.content[0].lesson!!.name) },
            { assertEquals(LessonStatus.SCHEDULED, result.content[0].lesson!!.status) },
        )
    }

    @Test
    fun `lesson이 진행 상태이면 목록 status를 IN_PROGRESS로 반환한다`() {
        val lesson = createLesson(status = LessonStatus.IN_PROGRESS)
        val dto = createDto(status = CommissionStatus.ACCEPTED, lesson = lesson)
        val pageable = PageRequest.of(0, 20)
        every { commissionAdaptor.searchCommissions(null, null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, null, pageable)

        assertEquals(CommissionListStatus.IN_PROGRESS, result.content[0].status)
    }

    @Test
    fun `lesson이 완료 상태이면 목록 status를 COMPLETED로 반환한다`() {
        val lesson = createLesson(status = LessonStatus.COMPLETED)
        val dto = createDto(status = CommissionStatus.ACCEPTED, lesson = lesson)
        val pageable = PageRequest.of(0, 20)
        every { commissionAdaptor.searchCommissions(null, null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, null, pageable)

        assertEquals(CommissionListStatus.COMPLETED, result.content[0].status)
    }

    @Test
    fun `lesson이 없는 commission은 lessonSummary가 null이다`() {
        val dto = createDto(lesson = null)
        val pageable = PageRequest.of(0, 20)
        every { commissionAdaptor.searchCommissions(null, null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, null, pageable)

        assertNull(result.content[0].lesson)
    }

    @Test
    fun `guideSubject가 응답에 포함된다`() {
        val dto = createDto()
        val pageable = PageRequest.of(0, 20)
        every { commissionAdaptor.searchCommissions(null, null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, null, pageable)

        assertEquals("진로탐색", result.content[0].guideSubject)
    }

    @Test
    fun `commission이 없으면 빈 페이지를 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        every { commissionAdaptor.searchCommissions(null, null, null, pageable) } returns PageImpl(emptyList(), pageable, 0)

        val result = useCase.execute(null, null, null, pageable)

        assertAll(
            { assertEquals(0, result.totalElements) },
            { assertEquals(0, result.content.size) },
        )
    }
}
