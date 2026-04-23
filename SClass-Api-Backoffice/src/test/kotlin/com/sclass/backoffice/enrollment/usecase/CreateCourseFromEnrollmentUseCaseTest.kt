package com.sclass.backoffice.enrollment.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.exception.EnrollmentInvalidPurchaseTargetException
import com.sclass.domain.domains.enrollment.exception.EnrollmentInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.service.LessonDomainService
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.exception.TeacherNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CreateCourseFromEnrollmentUseCaseTest {
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var lessonDomainService: LessonDomainService
    private lateinit var useCase: CreateCourseFromEnrollmentUseCase

    @BeforeEach
    fun setUp() {
        enrollmentAdaptor = mockk()
        productAdaptor = mockk()
        courseAdaptor = mockk()
        teacherAdaptor = mockk()
        lessonDomainService = mockk(relaxed = true)
        useCase =
            CreateCourseFromEnrollmentUseCase(
                enrollmentAdaptor = enrollmentAdaptor,
                productAdaptor = productAdaptor,
                courseAdaptor = courseAdaptor,
                teacherAdaptor = teacherAdaptor,
                lessonDomainService = lessonDomainService,
            )
    }

    private fun pendingMatchEnrollment(): Enrollment =
        Enrollment
            .createForPurchase(
                productId = "product-id-00000000001",
                studentUserId = "student-id-00000000001",
                tuitionAmountWon = 300000,
                paymentId = "payment-id-00000000001",
            ).apply {
                markPendingMatch()
            }

    private fun matchingCourseProduct() =
        CourseProduct(
            name = "매칭형 수학 코스",
            priceWon = 300000,
            totalLessons = 12,
            curriculum = "기본 커리큘럼",
            requiresMatching = true,
        )

    @Nested
    inner class Success {
        @Test
        fun `pending match enrollment로 course를 생성하고 active로 전이한다`() {
            val enrollment = pendingMatchEnrollment()
            val product = matchingCourseProduct()
            val savedCourse =
                Course(
                    id = 10L,
                    productId = product.id,
                    teacherUserId = "teacher-user-id-0000001",
                    status = CourseStatus.UNLISTED,
                    maxEnrollments = 1,
                    totalLessons = 12,
                    curriculum = "기본 커리큘럼",
                )

            every { enrollmentAdaptor.findById(1L) } returns enrollment
            every { teacherAdaptor.findByUserId("teacher-user-id-0000001") } returns mockk<Teacher>()
            every { productAdaptor.findById("product-id-00000000001") } returns product
            every {
                courseAdaptor.save(
                    match {
                        it.productId == product.id &&
                            it.teacherUserId == "teacher-user-id-0000001" &&
                            it.status == CourseStatus.UNLISTED &&
                            it.maxEnrollments == 1 &&
                            it.totalLessons == 12 &&
                            it.curriculum == "기본 커리큘럼"
                    },
                )
            } returns savedCourse
            every { enrollmentAdaptor.save(enrollment) } returns enrollment

            val result =
                useCase.execute(
                    enrollmentId = 1L,
                    teacherUserId = "teacher-user-id-0000001",
                )

            assertThat(result.courseId).isEqualTo(10L)
            assertThat(result.status).isEqualTo(EnrollmentStatus.ACTIVE)
            verify(exactly = 1) { teacherAdaptor.findByUserId("teacher-user-id-0000001") }
            verify(exactly = 1) { enrollmentAdaptor.save(enrollment) }
            verify(exactly = 1) {
                lessonDomainService.createLessonsForEnrollment(
                    enrollment = enrollment,
                    teacherUserId = "teacher-user-id-0000001",
                    courseName = "매칭형 수학 코스",
                    totalLessons = 12,
                )
            }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `pending match가 아니면 예외가 발생한다`() {
            val enrollment =
                Enrollment.createForPurchase(
                    productId = "product-id-00000000001",
                    studentUserId = "student-id-00000000001",
                    tuitionAmountWon = 300000,
                    paymentId = "payment-id-00000000001",
                )

            every { enrollmentAdaptor.findById(1L) } returns enrollment
            every { teacherAdaptor.findByUserId("teacher-user-id-0000001") } returns mockk<Teacher>()

            assertThatThrownBy {
                useCase.execute(
                    enrollmentId = 1L,
                    teacherUserId = "teacher-user-id-0000001",
                )
            }.isInstanceOf(EnrollmentInvalidStatusTransitionException::class.java)
        }

        @Test
        fun `이미 course가 연결된 enrollment면 예외가 발생한다`() {
            val enrollment =
                pendingMatchEnrollment().apply {
                    courseId = 99L
                }

            every { enrollmentAdaptor.findById(1L) } returns enrollment
            every { teacherAdaptor.findByUserId("teacher-user-id-0000001") } returns mockk<Teacher>()

            assertThatThrownBy {
                useCase.execute(
                    enrollmentId = 1L,
                    teacherUserId = "teacher-user-id-0000001",
                )
            }.isInstanceOf(EnrollmentInvalidStatusTransitionException::class.java)
        }

        @Test
        fun `매칭형 상품이 아니면 예외가 발생한다`() {
            val enrollment = pendingMatchEnrollment()
            val product =
                CourseProduct(
                    name = "일반 수학 코스",
                    priceWon = 300000,
                    totalLessons = 12,
                    requiresMatching = false,
                )

            every { enrollmentAdaptor.findById(1L) } returns enrollment
            every { teacherAdaptor.findByUserId("teacher-user-id-0000001") } returns mockk<Teacher>()
            every { productAdaptor.findById("product-id-00000000001") } returns product

            assertThatThrownBy {
                useCase.execute(
                    enrollmentId = 1L,
                    teacherUserId = "teacher-user-id-0000001",
                )
            }.isInstanceOf(EnrollmentInvalidPurchaseTargetException::class.java)
        }

        @Test
        fun `course product가 아니면 예외가 발생한다`() {
            val enrollment = pendingMatchEnrollment()

            every { enrollmentAdaptor.findById(1L) } returns enrollment
            every { teacherAdaptor.findByUserId("teacher-user-id-0000001") } returns mockk<Teacher>()
            every { productAdaptor.findById("product-id-00000000001") } returns mockk<Product>()

            assertThatThrownBy {
                useCase.execute(
                    enrollmentId = 1L,
                    teacherUserId = "teacher-user-id-0000001",
                )
            }.isInstanceOf(ProductTypeMismatchException::class.java)
        }

        @Test
        fun `존재하지 않는 선생님이면 예외가 발생한다`() {
            every { enrollmentAdaptor.findById(1L) } returns pendingMatchEnrollment()
            every { teacherAdaptor.findByUserId("teacher-user-id-0000001") } throws TeacherNotFoundException()

            assertThatThrownBy {
                useCase.execute(
                    enrollmentId = 1L,
                    teacherUserId = "teacher-user-id-0000001",
                )
            }.isInstanceOf(TeacherNotFoundException::class.java)
        }
    }
}
