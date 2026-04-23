package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CreateCourseRequest
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.exception.CourseMatchingProductNotCreatableException
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductNotFoundException
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CreateCourseUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var thumbnailUrlResolver: ThumbnailUrlResolver
    private lateinit var useCase: CreateCourseUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        productAdaptor = mockk()
        thumbnailUrlResolver = mockk()
        every { thumbnailUrlResolver.resolve(any()) } answers {
            firstArg<String?>()?.let { "https://static.test.sclass.click/course_thumbnail/$it" }
        }
        useCase = CreateCourseUseCase(courseAdaptor, productAdaptor, thumbnailUrlResolver)
    }

    private fun product(requiresMatching: Boolean = false) =
        CourseProduct(
            name = "2025 수학 코스",
            priceWon = 300000,
            totalLessons = 12,
            description = "수학 심화 과정",
            curriculum = "1주차: 함수\n2주차: 극한",
            thumbnailFileId = "file-id-000000000001",
            requiresMatching = requiresMatching,
        )

    private fun request() =
        CreateCourseRequest(
            productId = "product-id-00000000001",
            teacherUserId = "teacher-id-00000000001",
            organizationId = null,
        )

    @Nested
    inner class Success {
        @Test
        fun `기존 코스 상품으로 코스를 생성한다`() {
            val product = product()
            val courseSlot = slot<Course>()
            every { productAdaptor.findCourseProductById(product.id) } returns product
            every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }

            val result = useCase.execute(request().copy(productId = product.id))

            assertThat(result.status).isEqualTo(CourseStatus.DRAFT)
            assertThat(result.name).isEqualTo("2025 수학 코스")
            assertThat(result.teacherUserId).isEqualTo("teacher-id-00000000001")
            assertThat(result.organizationId).isNull()
            assertThat(courseSlot.captured.productId).isEqualTo(product.id)
            assertThat(courseSlot.captured.curriculum).isEqualTo("1주차: 함수\n2주차: 극한")
            assertThat(courseSlot.captured.totalLessons).isEqualTo(12)
        }

        @Test
        fun `organizationId가 있으면 코스에 포함된다`() {
            val product = product()
            val courseSlot = slot<Course>()
            every { productAdaptor.findCourseProductById(product.id) } returns product
            every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }

            useCase.execute(request().copy(productId = product.id, organizationId = "org-id-000000000001"))

            assertThat(courseSlot.captured.organizationId).isEqualTo("org-id-000000000001")
        }

        @Test
        fun `조회한 상품의 id를 코스의 productId로 연결한다`() {
            val product = product()
            val courseSlot = slot<Course>()
            every { productAdaptor.findCourseProductById(product.id) } returns product
            every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }

            useCase.execute(request().copy(productId = product.id))

            verify(exactly = 1) { productAdaptor.findCourseProductById(product.id) }
            verify(exactly = 1) { courseAdaptor.save(any()) }
            assertThat(courseSlot.captured.productId).isEqualTo(product.id)
        }

        @Test
        fun `상품 썸네일과 모집 제약 필드가 응답과 코스에 반영된다`() {
            val product = product()
            val courseSlot = slot<Course>()
            every { productAdaptor.findCourseProductById(product.id) } returns product
            every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }

            val enrollStart = LocalDateTime.of(2026, 5, 1, 0, 0)
            val enrollEnd = LocalDateTime.of(2026, 5, 31, 23, 59)
            val courseStart = LocalDateTime.of(2026, 6, 1, 0, 0)
            val courseEnd = LocalDateTime.of(2026, 8, 31, 23, 59)

            val result =
                useCase.execute(
                    request().copy(
                        productId = product.id,
                        maxEnrollments = 20,
                        enrollmentStartAt = enrollStart,
                        enrollmentDeadLine = enrollEnd,
                        startAt = courseStart,
                        endAt = courseEnd,
                    ),
                )

            assertThat(result.thumbnailUrl).isEqualTo("https://static.test.sclass.click/course_thumbnail/file-id-000000000001")
            assertThat(courseSlot.captured.maxEnrollments).isEqualTo(20)
            assertThat(courseSlot.captured.enrollmentStartAt).isEqualTo(enrollStart)
            assertThat(courseSlot.captured.enrollmentDeadLine).isEqualTo(enrollEnd)
            assertThat(courseSlot.captured.startAt).isEqualTo(courseStart)
            assertThat(courseSlot.captured.endAt).isEqualTo(courseEnd)
        }

        @Test
        fun `thumbnailFileId가 null이면 응답의 thumbnailUrl도 null`() {
            val product = product().also { it.thumbnailFileId = null }
            every { productAdaptor.findCourseProductById(product.id) } returns product
            every { courseAdaptor.save(any()) } answers { firstArg() }

            val result = useCase.execute(request().copy(productId = product.id))

            assertThat(result.thumbnailUrl).isNull()
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `course product가 아니면 예외가 발생한다`() {
            every { productAdaptor.findCourseProductById("product-id-00000000001") } throws ProductTypeMismatchException()

            assertThatThrownBy {
                useCase.execute(request())
            }.isInstanceOf(ProductTypeMismatchException::class.java)
        }

        @Test
        fun `상품이 존재하지 않으면 not found 예외가 발생한다`() {
            every { productAdaptor.findCourseProductById("product-id-00000000001") } throws ProductNotFoundException()

            assertThatThrownBy {
                useCase.execute(request())
            }.isInstanceOf(ProductNotFoundException::class.java)
        }

        @Test
        fun `매칭형 코스 상품으로는 일반 코스를 생성할 수 없다`() {
            val product = product(requiresMatching = true)
            every { productAdaptor.findCourseProductById(product.id) } returns product

            assertThatThrownBy {
                useCase.execute(request().copy(productId = product.id))
            }.isInstanceOf(CourseMatchingProductNotCreatableException::class.java)
        }
    }
}
