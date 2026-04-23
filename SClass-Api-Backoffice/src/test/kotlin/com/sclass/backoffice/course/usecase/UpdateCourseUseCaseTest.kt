package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.UpdateCourseRequest
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.exception.CourseAlreadyStartedException
import com.sclass.domain.domains.course.exception.CourseMaxEnrollmentsTooLowException
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UpdateCourseUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var enrollmentAdaptor: EnrollmentAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: UpdateCourseUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        productAdaptor = mockk()
        enrollmentAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        every { thumbnailUrlResolver.resolve(any()) } answers {
            firstArg<String?>()?.let { "https://static.test.sclass.click/course_thumbnail/$it" }
        }
        useCase = UpdateCourseUseCase(courseAdaptor, productAdaptor, enrollmentAdaptor, thumbnailUrlResolver)
    }

    private fun listedCourse(startAt: LocalDateTime? = null) =
        Course(
            id = 1L,
            productId = "product-id-00000000001",
            teacherUserId = "teacher-id-00000000001",
            status = CourseStatus.LISTED,
            maxEnrollments = 10,
            startAt = startAt,
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
        fun `운영 fulfillment 정보는 startAt과 무관하게 변경된다`() {
            val course = listedCourse(startAt = LocalDateTime.now().minusDays(1))
            val product = courseProduct()
            every { courseAdaptor.findById(1L) } returns course
            every { productAdaptor.findById("product-id-00000000001") } returns product

            val result =
                useCase.execute(
                    1L,
                    UpdateCourseRequest(
                        curriculum = "1주차: 미분",
                        totalLessons = 16,
                    ),
                )

            assertAll(
                { assertThat(course.curriculum).isEqualTo("1주차: 미분") },
                { assertThat(course.totalLessons).isEqualTo(16) },
                { assertThat(product.name).isEqualTo("수학 코스") },
                { assertThat(product.priceWon).isEqualTo(300000) },
                { assertThat(result.curriculum).isEqualTo("1주차: 미분") },
                { assertThat(result.totalLessons).isEqualTo(16) },
            )
        }

        @Test
        fun `모집 제약 변경 시 현재 활성 등록자 수를 조회해 검증한다`() {
            val course = listedCourse()
            every { courseAdaptor.findById(1L) } returns course
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.countLiveEnrollments(1L) } returns 2L

            useCase.execute(1L, UpdateCourseRequest(maxEnrollments = 20))

            assertThat(course.maxEnrollments).isEqualTo(20)
        }

        @Test
        fun `일정 변경이 반영된다`() {
            val course = listedCourse()
            every { courseAdaptor.findById(1L) } returns course
            every { productAdaptor.findById(any()) } returns courseProduct()

            val newStart = LocalDateTime.now().plusDays(10)
            val newEnd = LocalDateTime.now().plusDays(40)
            useCase.execute(1L, UpdateCourseRequest(startAt = newStart, endAt = newEnd))

            assertAll(
                { assertThat(course.startAt).isEqualTo(newStart) },
                { assertThat(course.endAt).isEqualTo(newEnd) },
            )
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `CourseProduct가 아니면 ProductTypeMismatchException`() {
            every { courseAdaptor.findById(1L) } returns listedCourse()
            every { productAdaptor.findById(any()) } returns mockk<Product>()

            assertThatThrownBy {
                useCase.execute(1L, UpdateCourseRequest(maxEnrollments = 5))
            }.isInstanceOf(ProductTypeMismatchException::class.java)
        }

        @Test
        fun `이미 시작된 코스의 모집 제약 변경은 예외`() {
            val course = listedCourse(startAt = LocalDateTime.now().minusDays(1))
            every { courseAdaptor.findById(1L) } returns course
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.countLiveEnrollments(1L) } returns 0L

            assertThatThrownBy {
                useCase.execute(1L, UpdateCourseRequest(maxEnrollments = 20))
            }.isInstanceOf(CourseAlreadyStartedException::class.java)
        }

        @Test
        fun `현재 활성 등록자보다 낮은 maxEnrollments는 예외`() {
            val course = listedCourse()
            every { courseAdaptor.findById(1L) } returns course
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { enrollmentAdaptor.countLiveEnrollments(1L) } returns 5L

            assertThatThrownBy {
                useCase.execute(1L, UpdateCourseRequest(maxEnrollments = 3))
            }.isInstanceOf(CourseMaxEnrollmentsTooLowException::class.java)
        }
    }
}
