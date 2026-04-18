package com.sclass.backoffice.course.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ActivateCourseUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var useCase: ActivateCourseUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        productAdaptor = mockk()
        useCase = ActivateCourseUseCase(courseAdaptor, productAdaptor)
    }

    private fun draftCourse(productId: String) =
        Course(
            id = 1L,
            productId = productId,
            teacherUserId = "teacher-id-00000000000001",
            status = CourseStatus.DRAFT,
        )

    private fun product() =
        CourseProduct(
            name = "수학 기초",
            priceWon = 300000,
            totalLessons = 12,
        )

    @Test
    fun `코스를 활성화하면 course의 상태가 ACTIVE로 바뀌고 product가 visible=true 가 된다`() {
        val product = product()
        val course = draftCourse(product.id)
        every { courseAdaptor.findById(course.id) } returns course
        every { productAdaptor.findById(product.id) } returns product
        every { courseAdaptor.save(any()) } answers { firstArg() }
        every { productAdaptor.save(any()) } answers { firstArg() }

        val response = useCase.execute(course.id)

        assertAll(
            { assertEquals(CourseStatus.ACTIVE, course.status) },
            { assertTrue(product.visible) },
            { assertEquals(CourseStatus.ACTIVE, response.status) },
            { assertEquals(product.name, response.name) },
        )
    }

    @Test
    fun `코스와 상품을 모두 save 한다`() {
        val product = product()
        val course = draftCourse(product.id)
        val courseSlot = slot<Course>()
        val productSlot = slot<CourseProduct>()
        every { courseAdaptor.findById(course.id) } returns course
        every { productAdaptor.findById(product.id) } returns product
        every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }
        every { productAdaptor.save(capture(productSlot)) } answers { productSlot.captured }

        useCase.execute(course.id)

        verify(exactly = 1) { courseAdaptor.save(any()) }
        verify(exactly = 1) { productAdaptor.save(any()) }
        assertAll(
            { assertEquals(CourseStatus.ACTIVE, courseSlot.captured.status) },
            { assertTrue(productSlot.captured.visible) },
        )
    }
}
