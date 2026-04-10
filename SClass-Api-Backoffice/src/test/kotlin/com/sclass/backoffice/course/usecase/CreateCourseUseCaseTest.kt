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

    private fun courseProduct() =
        CourseProduct(
            name = "수학 코스",
            priceWon = 300000,
            totalLessons = 12,
            teacherPayoutPerLessonWon = 20000,
        )

    private fun request() =
        CreateCourseRequest(
            productId = "product-id-00000000001",
            teacherUserId = "teacher-id-00000000001",
            organizationId = null,
            name = "2025 수학 코스",
            description = "수학 심화 과정",
        )

    @Nested
    inner class Success {
        @Test
        fun `코스를 생성하고 DRAFT 상태로 반환한다`() {
            val courseSlot = slot<Course>()
            val product = courseProduct()
            every { productAdaptor.findById("product-id-00000000001") } returns product
            every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }

            val result = useCase.execute(request())

            assertThat(result.status).isEqualTo(CourseStatus.DRAFT)
            assertThat(result.name).isEqualTo("2025 수학 코스")
            assertThat(result.teacherUserId).isEqualTo("teacher-id-00000000001")
            assertThat(result.organizationId).isNull()
        }

        @Test
        fun `organizationId가 있으면 코스에 포함된다`() {
            val courseSlot = slot<Course>()
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }

            useCase.execute(request().copy(organizationId = "org-id-000000000001"))

            assertThat(courseSlot.captured.organizationId).isEqualTo("org-id-000000000001")
        }

        @Test
        fun `productAdaptor로 상품 유효성을 검증한다`() {
            every { productAdaptor.findById("product-id-00000000001") } returns courseProduct()
            every { courseAdaptor.save(any()) } answers { firstArg() }

            useCase.execute(request())

            verify(exactly = 1) { productAdaptor.findById("product-id-00000000001") }
        }
    }
}
