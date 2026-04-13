package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CreateBulkCourseRequest
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

class CreateBulkCourseUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var useCase: CreateBulkCourseUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        productAdaptor = mockk()
        useCase = CreateBulkCourseUseCase(courseAdaptor, productAdaptor)
    }

    private fun courseProduct() =
        CourseProduct(
            name = "수학 코스",
            priceWon = 300000,
            totalLessons = 12,
        )

    private fun request(teacherUserIds: List<String>) =
        CreateBulkCourseRequest(
            productId = "product-id-00000000001",
            teacherUserIds = teacherUserIds,
            organizationId = null,
            name = "2025 수학 코스",
            description = null,
        )

    @Nested
    inner class Success {
        @Test
        fun `여러 선생님에게 코스를 일괄 생성한다`() {
            val teacherIds = listOf("teacher-id-00000000001", "teacher-id-00000000002", "teacher-id-00000000003")
            val coursesSlot = slot<List<Course>>()
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { courseAdaptor.saveAll(capture(coursesSlot)) } answers { coursesSlot.captured }

            val results = useCase.execute(request(teacherIds))

            assertThat(results).hasSize(3)
            assertThat(results.map { it.teacherUserId }).containsExactlyInAnyOrder(*teacherIds.toTypedArray())
            assertThat(results.all { it.status == CourseStatus.DRAFT }).isTrue()
        }

        @Test
        fun `saveAll을 한 번만 호출한다`() {
            val teacherIds = listOf("teacher-id-00000000001", "teacher-id-00000000002")
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { courseAdaptor.saveAll(any()) } answers { firstArg() }

            useCase.execute(request(teacherIds))

            verify(exactly = 1) { courseAdaptor.saveAll(any()) }
        }

        @Test
        fun `선생님이 1명이어도 정상 동작한다`() {
            every { productAdaptor.findById(any()) } returns courseProduct()
            every { courseAdaptor.saveAll(any()) } answers { firstArg() }

            val results = useCase.execute(request(listOf("teacher-id-00000000001")))

            assertThat(results).hasSize(1)
        }
    }
}
