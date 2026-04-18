package com.sclass.backoffice.enrollment.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.lesson.service.LessonDomainService
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GrantEnrollmentUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var lessonService: LessonDomainService
    private lateinit var useCase: GrantEnrollmentUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        productAdaptor = mockk()
        enrollmentAdaptor = mockk()
        lessonService = mockk(relaxed = true)
        useCase = GrantEnrollmentUseCase(courseAdaptor, productAdaptor, enrollmentAdaptor, lessonService)
    }

    private fun activeCourse() =
        Course(
            id = 1L,
            productId = "product-id-00000000001",
            teacherUserId = "teacher-id-00000000001",
            status = CourseStatus.LISTED,
        )

    private fun courseProduct() =
        CourseProduct(
            name = "수학 코스",
            priceWon = 300000,
            totalLessons = 12,
        )

    @Nested
    inner class Success {
        @Test
        fun `어드민이 수강을 직접 등록하면 ACTIVE 상태로 생성된다`() {
            val enrollmentSlot = slot<Enrollment>()
            every { courseAdaptor.findById(1L) } returns activeCourse()
            every { productAdaptor.findById("product-id-00000000001") } returns courseProduct()
            every { enrollmentAdaptor.save(capture(enrollmentSlot)) } answers { enrollmentSlot.captured }

            val result =
                useCase.execute(
                    adminUserId = "admin-id-000000000001",
                    studentUserId = "student-id-00000000001",
                    courseId = 1L,
                    grantReason = "장학생 혜택",
                )

            assertThat(result.status).isEqualTo(EnrollmentStatus.ACTIVE)
            assertThat(result.enrollmentType).isEqualTo(EnrollmentType.ADMIN_GRANT)
            assertThat(result.studentUserId).isEqualTo("student-id-00000000001")
            assertThat(result.courseId).isEqualTo(1L)
        }

        @Test
        fun `수강료와 선생님 페이아웃이 상품 스냅샷으로 저장된다`() {
            val enrollmentSlot = slot<Enrollment>()
            every { courseAdaptor.findById(1L) } returns activeCourse()
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.save(capture(enrollmentSlot)) } answers { enrollmentSlot.captured }

            useCase.execute("admin-id-000000000001", "student-id-00000000001", 1L, "장학생 혜택")

            assertThat(enrollmentSlot.captured.tuitionAmountWon).isEqualTo(300000)
        }

        @Test
        fun `수강 등록 시 totalLessons만큼 레슨이 일괄 생성된다`() {
            val enrollmentSlot = slot<Enrollment>()
            every { courseAdaptor.findById(1L) } returns activeCourse()
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.save(capture(enrollmentSlot)) } answers { enrollmentSlot.captured }

            useCase.execute("admin-id-000000000001", "student-id-00000000001", 1L, "장학생 혜택")

            verify {
                lessonService.createLessonsForEnrollment(
                    enrollment = any(),
                    teacherUserId = "teacher-id-00000000001",
                    courseName = "수학 코스",
                    totalLessons = 12,
                )
            }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `CourseProduct가 아닌 상품이면 ProductTypeMismatchException이 발생한다`() {
            every { courseAdaptor.findById(1L) } returns activeCourse()
            every { productAdaptor.findById(any()) } returns mockk<Product>()

            assertThatThrownBy {
                useCase.execute("admin-id-000000000001", "student-id-00000000001", 1L, "혜택")
            }.isInstanceOf(ProductTypeMismatchException::class.java)
        }
    }
}
