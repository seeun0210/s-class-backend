package com.sclass.supporters.enrollment.usecase

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithCourseDto
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.RollingMembershipProduct
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetMyEnrollmentsUseCaseTest {
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: GetMyEnrollmentsUseCase

    @BeforeEach
    fun setUp() {
        enrollmentAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        every { thumbnailUrlResolver.resolve(any()) } answers {
            firstArg<String?>()?.let { "https://static.test.sclass.click/course_thumbnail/$it" }
        }
        useCase = GetMyEnrollmentsUseCase(enrollmentAdaptor, thumbnailUrlResolver)
    }

    private fun makeCourseDto(
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
                productId = "product-id-0000000000000001",
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

    private fun makeMembershipDto(): EnrollmentWithCourseDto {
        val product =
            RollingMembershipProduct(
                name = "프리미엄 멤버십",
                priceWon = 50000,
                periodDays = 30,
                coinPackageId = "cp-0000000000000000000000001",
                thumbnailFileId = "mp-thumb-00000000000000001",
            )
        product.show()
        val enrollment =
            Enrollment.createForMembershipPurchase(
                productId = product.id,
                studentUserId = "student-id-0000000000000001",
                tuitionAmountWon = 50000,
                paymentId = "payment-id-00000000000000",
            )
        return EnrollmentWithCourseDto(
            enrollment = enrollment,
            course = null,
            courseProduct = null,
            teacherName = null,
            membershipProduct = product,
        )
    }

    @Test
    fun `학생의 등록 목록과 코스 요약을 반환한다`() {
        every { enrollmentAdaptor.findAllByStudentWithCourse("student-id-0000000000000001") } returns listOf(makeCourseDto())

        val result = useCase.execute("student-id-0000000000000001")

        val summary = result[0].course!!
        assertAll(
            { assertEquals(1, result.size) },
            { assertEquals(1L, result[0].courseId) },
            { assertEquals(EnrollmentStatus.PENDING_PAYMENT, result[0].status) },
            { assertEquals(300000, result[0].tuitionAmountWon) },
            { assertNull(result[0].membership) },
            { assertEquals("수학 기초", summary.name) },
            { assertEquals("수학 심화", summary.description) },
            { assertEquals("https://static.test.sclass.click/course_thumbnail/file-id-00000000000000001", summary.thumbnailUrl) },
            { assertEquals("김선생", summary.teacherName) },
            { assertEquals(CourseStatus.LISTED, summary.courseStatus) },
            { assertEquals(LocalDateTime.of(2026, 4, 30, 0, 0), summary.enrollmentDeadLine) },
            { assertEquals(LocalDateTime.of(2026, 5, 1, 0, 0), summary.startAt) },
            { assertEquals(LocalDateTime.of(2026, 6, 30, 0, 0), summary.endAt) },
        )
    }

    @Test
    fun `멤버십 enrollment는 membership 요약을 포함하고 course는 null이다`() {
        every { enrollmentAdaptor.findAllByStudentWithCourse("student-id-0000000000000001") } returns listOf(makeMembershipDto())

        val result = useCase.execute("student-id-0000000000000001")

        val membership = result[0].membership!!
        assertAll(
            { assertNull(result[0].course) },
            { assertNotNull(result[0].membership) },
            { assertEquals(ProductType.ROLLING_MEMBERSHIP, membership.productType) },
            { assertEquals("프리미엄 멤버십", membership.productName) },
            { assertEquals("https://static.test.sclass.click/course_thumbnail/mp-thumb-00000000000000001", membership.thumbnailUrl) },
        )
    }

    @Test
    fun `코스가 삭제되어 join 결과가 null 이면 course 요약은 null 이다`() {
        every { enrollmentAdaptor.findAllByStudentWithCourse("student-id-0000000000000001") } returns
            listOf(makeCourseDto(course = null, courseProduct = null, teacherName = null))

        val result = useCase.execute("student-id-0000000000000001")

        assertNull(result[0].course)
    }

    @Test
    fun `등록 내역이 없으면 빈 리스트를 반환한다`() {
        every { enrollmentAdaptor.findAllByStudentWithCourse("student-id-0000000000000001") } returns emptyList()

        assertTrue(useCase.execute("student-id-0000000000000001").isEmpty())
    }
}
