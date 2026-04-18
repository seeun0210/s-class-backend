package com.sclass.supporters.enrollment.usecase

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithCourseDto
import com.sclass.domain.domains.product.domain.CourseProduct
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetMyEnrollmentsUseCaseTest {
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var useCase: GetMyEnrollmentsUseCase

    @BeforeEach
    fun setUp() {
        enrollmentAdaptor = mockk()
        useCase = GetMyEnrollmentsUseCase(enrollmentAdaptor)
    }

    private fun makeDto(
        course: Course? =
            Course(
                id = 1L,
                productId = "product-id-0000000000000001",
                teacherUserId = "teacher-id-0000000000000001",
                status = CourseStatus.LISTED,
                maxEnrollments = 10,
                enrollmentDeadLine = LocalDateTime.of(2026, 4, 30, 0, 0),
                startAt = LocalDateTime.of(2026, 5, 1, 0, 0),
                endAt = LocalDateTime.of(2026, 6, 30, 0, 0),
            ),
        courseProduct: CourseProduct? =
            CourseProduct(
                name = "수학 기초",
                priceWon = 300000,
                totalLessons = 12,
                description = "수학 심화",
                thumbnailFileId = "file-id-00000000000000001",
            ),
        teacherName: String? = "김선생",
    ): EnrollmentWithCourseDto {
        val enrollment =
            Enrollment.createForPurchase(
                courseId = 1L,
                studentUserId = "student-id-0000000000000001",
                tuitionAmountWon = 300000,
                paymentId = "payment-id-00000000000000",
            )
        return EnrollmentWithCourseDto(
            enrollment = enrollment,
            course = course,
            courseProduct = courseProduct,
            teacherName = teacherName,
        )
    }

    @Test
    fun `학생의 등록 목록과 코스 요약을 반환한다`() {
        every { enrollmentAdaptor.findAllByStudentWithCourse("student-id-0000000000000001") } returns listOf(makeDto())

        val result = useCase.execute("student-id-0000000000000001")

        val summary = result[0].course!!
        assertAll(
            { assertEquals(1, result.size) },
            { assertEquals(1L, result[0].courseId) },
            { assertEquals(EnrollmentStatus.PENDING_PAYMENT, result[0].status) },
            { assertEquals(300000, result[0].tuitionAmountWon) },
            { assertEquals("수학 기초", summary.name) },
            { assertEquals("수학 심화", summary.description) },
            { assertEquals("file-id-00000000000000001", summary.thumbnailFileId) },
            { assertEquals("김선생", summary.teacherName) },
            { assertEquals(CourseStatus.LISTED, summary.courseStatus) },
            { assertEquals(LocalDateTime.of(2026, 4, 30, 0, 0), summary.enrollmentDeadLine) },
            { assertEquals(LocalDateTime.of(2026, 5, 1, 0, 0), summary.startAt) },
            { assertEquals(LocalDateTime.of(2026, 6, 30, 0, 0), summary.endAt) },
        )
    }

    @Test
    fun `코스가 삭제되어 join 결과가 null 이면 course 요약은 null 이다`() {
        every { enrollmentAdaptor.findAllByStudentWithCourse("student-id-0000000000000001") } returns
            listOf(makeDto(course = null, courseProduct = null, teacherName = null))

        val result = useCase.execute("student-id-0000000000000001")

        assertNull(result[0].course)
    }

    @Test
    fun `등록 내역이 없으면 빈 리스트를 반환한다`() {
        every { enrollmentAdaptor.findAllByStudentWithCourse("student-id-0000000000000001") } returns emptyList()

        assertTrue(useCase.execute("student-id-0000000000000001").isEmpty())
    }
}
