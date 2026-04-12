package com.sclass.backoffice.enrollment.usecase

import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithDetailDto
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class GetEnrollmentListUseCaseTest {
    private val enrollmentAdaptor = mockk<EnrollmentAdaptor>()
    private val useCase = GetEnrollmentListUseCase(enrollmentAdaptor)

    private fun createEnrollmentDto(
        studentUserId: String = "student01",
        studentName: String = "홍길동",
        courseId: Long = 1L,
        courseName: String = "수학 기초",
        teacherUserId: String = "teacher01",
        teacherName: String = "김선생",
        status: EnrollmentStatus = EnrollmentStatus.ACTIVE,
    ) = EnrollmentWithDetailDto(
        enrollment =
            Enrollment.createByGrant(
                courseId = courseId,
                studentUserId = studentUserId,
                grantedByUserId = "admin01",
                grantReason = "테스트",
                teacherPayoutPerLessonWon = 50000,
                tuitionAmountWon = 100000,
            ),
        studentName = studentName,
        courseName = courseName,
        teacherUserId = teacherUserId,
        teacherName = teacherName,
    )

    @Test
    fun `enrollment 목록을 페이지로 반환한다`() {
        val dtos =
            listOf(
                createEnrollmentDto(studentName = "홍길동", courseName = "수학 기초"),
                createEnrollmentDto(studentName = "김철수", courseName = "영어 회화"),
            )
        val pageable = PageRequest.of(0, 20)
        every { enrollmentAdaptor.searchEnrollments(null, null, null, null, pageable) } returns PageImpl(dtos, pageable, 2)

        val result = useCase.execute(null, null, null, null, pageable)

        assertAll(
            { assertEquals(2, result.totalElements) },
            { assertEquals(1, result.totalPages) },
            { assertEquals(0, result.currentPage) },
            { assertEquals("홍길동", result.content[0].studentName) },
            { assertEquals("수학 기초", result.content[0].courseName) },
            { assertEquals("김철수", result.content[1].studentName) },
            { assertEquals("영어 회화", result.content[1].courseName) },
        )
    }

    @Test
    fun `studentUserId 필터를 적용하면 해당 학생의 enrollment만 반환한다`() {
        val dto = createEnrollmentDto(studentUserId = "student01", studentName = "홍길동")
        val pageable = PageRequest.of(0, 20)
        every { enrollmentAdaptor.searchEnrollments("student01", null, null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute("student01", null, null, null, pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals("student01", result.content[0].studentUserId) },
            { assertEquals("홍길동", result.content[0].studentName) },
        )
    }

    @Test
    fun `courseId 필터를 적용하면 해당 코스의 enrollment만 반환한다`() {
        val dto = createEnrollmentDto(courseId = 1L, courseName = "수학 기초")
        val pageable = PageRequest.of(0, 20)
        every { enrollmentAdaptor.searchEnrollments(null, null, 1L, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, 1L, null, pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals("수학 기초", result.content[0].courseName) },
        )
    }

    @Test
    fun `status 필터를 적용하면 해당 상태의 enrollment만 반환한다`() {
        val dto = createEnrollmentDto(status = EnrollmentStatus.ACTIVE)
        val pageable = PageRequest.of(0, 20)
        every { enrollmentAdaptor.searchEnrollments(null, null, null, EnrollmentStatus.ACTIVE, pageable) } returns
            PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, null, EnrollmentStatus.ACTIVE, pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals(EnrollmentStatus.ACTIVE, result.content[0].status) },
        )
    }

    @Test
    fun `teacherUserId 필터를 적용하면 해당 선생님의 enrollment만 반환한다`() {
        val dto = createEnrollmentDto(teacherUserId = "teacher01", teacherName = "김선생")
        val pageable = PageRequest.of(0, 20)
        every { enrollmentAdaptor.searchEnrollments(null, "teacher01", null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, "teacher01", null, null, pageable)

        assertAll(
            { assertEquals(1, result.totalElements) },
            { assertEquals("teacher01", result.content[0].teacherUserId) },
            { assertEquals("김선생", result.content[0].teacherName) },
        )
    }

    @Test
    fun `teacherUserId와 teacherName이 응답에 포함된다`() {
        val dto = createEnrollmentDto(teacherUserId = "teacher01", teacherName = "김선생")
        val pageable = PageRequest.of(0, 20)
        every { enrollmentAdaptor.searchEnrollments(null, null, null, null, pageable) } returns PageImpl(listOf(dto), pageable, 1)

        val result = useCase.execute(null, null, null, null, pageable)

        assertAll(
            { assertEquals("teacher01", result.content[0].teacherUserId) },
            { assertEquals("김선생", result.content[0].teacherName) },
        )
    }

    @Test
    fun `enrollment이 없으면 빈 페이지를 반환한다`() {
        val pageable = PageRequest.of(0, 20)
        every { enrollmentAdaptor.searchEnrollments(null, null, null, null, pageable) } returns PageImpl(emptyList(), pageable, 0)

        val result = useCase.execute(null, null, null, null, pageable)

        assertAll(
            { assertEquals(0, result.totalElements) },
            { assertEquals(0, result.content.size) },
        )
    }
}
