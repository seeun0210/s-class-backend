package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CreateCourseRequest
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CreateCourseUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var useCase: CreateCourseUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        productAdaptor = mockk()
        useCase = CreateCourseUseCase(courseAdaptor, productAdaptor)
    }

    private fun request() =
        CreateCourseRequest(
            teacherUserId = "teacher-id-00000000001",
            organizationId = null,
            name = "2025 수학 코스",
            description = "수학 심화 과정",
            priceWon = 300000,
            totalLessons = 12,
        )

    @Nested
    inner class Success {
        @Test
        fun `코스와 상품을 한 트랜잭션에서 생성한다`() {
            val productSlot = slot<CourseProduct>()
            val courseSlot = slot<Course>()
            every { productAdaptor.save(capture(productSlot)) } answers { productSlot.captured }
            every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }

            val result = useCase.execute(request())

            assertThat(productSlot.captured.name).isEqualTo("2025 수학 코스")
            assertThat(productSlot.captured.priceWon).isEqualTo(300000)
            assertThat(productSlot.captured.totalLessons).isEqualTo(12)
            assertThat(productSlot.captured.description).isEqualTo("수학 심화 과정")
            assertThat(result.status).isEqualTo(CourseStatus.DRAFT)
            assertThat(result.name).isEqualTo("2025 수학 코스")
            assertThat(result.teacherUserId).isEqualTo("teacher-id-00000000001")
            assertThat(result.organizationId).isNull()
        }

        @Test
        fun `organizationId가 있으면 코스에 포함된다`() {
            val courseSlot = slot<Course>()
            every { productAdaptor.save(any()) } answers { firstArg() }
            every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }

            useCase.execute(request().copy(organizationId = "org-id-000000000001"))

            assertThat(courseSlot.captured.organizationId).isEqualTo("org-id-000000000001")
        }

        @Test
        fun `생성된 상품의 id를 코스의 productId로 연결한다`() {
            val courseSlot = slot<Course>()
            every { productAdaptor.save(any()) } answers { firstArg() }
            every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }

            useCase.execute(request())

            verify(exactly = 1) { productAdaptor.save(any()) }
            verify(exactly = 1) { courseAdaptor.save(any()) }
            assertThat(courseSlot.captured.productId).isNotBlank
        }

        @Test
        fun `카탈로그 자산과 모집 제약 필드가 상품과 코스에 반영된다`() {
            val productSlot = slot<CourseProduct>()
            val courseSlot = slot<Course>()
            every { productAdaptor.save(capture(productSlot)) } answers { productSlot.captured }
            every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }

            val enrollStart = LocalDateTime.of(2026, 5, 1, 0, 0)
            val enrollEnd = LocalDateTime.of(2026, 5, 31, 23, 59)
            val courseStart = LocalDateTime.of(2026, 6, 1, 0, 0)
            val courseEnd = LocalDateTime.of(2026, 8, 31, 23, 59)

            useCase.execute(
                request().copy(
                    curriculum = "1주차: 함수\n2주차: 극한",
                    thumbnailFileId = "file-id-000000000001",
                    maxEnrollments = 20,
                    enrollmentStartAt = enrollStart,
                    enrollmentDeadLine = enrollEnd,
                    startAt = courseStart,
                    endAt = courseEnd,
                ),
            )

            assertThat(productSlot.captured.curriculum).isEqualTo("1주차: 함수\n2주차: 극한")
            assertThat(productSlot.captured.thumbnailFileId).isEqualTo("file-id-000000000001")
            assertThat(courseSlot.captured.maxEnrollments).isEqualTo(20)
            assertThat(courseSlot.captured.enrollmentStartAt).isEqualTo(enrollStart)
            assertThat(courseSlot.captured.enrollmentDeadLine).isEqualTo(enrollEnd)
            assertThat(courseSlot.captured.startAt).isEqualTo(courseStart)
            assertThat(courseSlot.captured.endAt).isEqualTo(courseEnd)
        }
    }
}
