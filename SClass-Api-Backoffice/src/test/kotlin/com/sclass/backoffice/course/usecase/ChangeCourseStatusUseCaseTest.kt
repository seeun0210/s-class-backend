package com.sclass.backoffice.course.usecase

import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.exception.CourseInvalidStatusTransitionException
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChangeCourseStatusUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var productAdaptor: ProductAdaptor
    private lateinit var useCase: ChangeCourseStatusUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        productAdaptor = mockk()
        useCase = ChangeCourseStatusUseCase(courseAdaptor, productAdaptor)
    }

    private fun courseOf(
        productId: String,
        status: CourseStatus,
    ) = Course(
        id = 1L,
        productId = productId,
        teacherUserId = "teacher-id-00000000000001",
        status = status,
    )

    private fun product() =
        CourseProduct(
            name = "수학 기초",
            priceWon = 300000,
            totalLessons = 12,
        )

    @Test
    fun `LISTED로 전이하면 course가 LISTED가 되고 product가 visible=true 가 된다`() {
        val product = product()
        val course = courseOf(product.id, CourseStatus.DRAFT)
        every { courseAdaptor.findById(course.id) } returns course
        every { productAdaptor.findById(product.id) } returns product
        every { courseAdaptor.save(any()) } answers { firstArg() }
        every { productAdaptor.save(any()) } answers { firstArg() }

        val response = useCase.execute(course.id, CourseStatus.LISTED)

        assertAll(
            { assertEquals(CourseStatus.LISTED, course.status) },
            { assertTrue(product.visible) },
            { assertEquals(CourseStatus.LISTED, response.status) },
            { assertEquals(product.name, response.name) },
        )
    }

    @Test
    fun `UNLISTED로 전이하면 course가 UNLISTED가 되고 product가 visible=false 가 된다`() {
        val product = product().also { it.show() }
        val course = courseOf(product.id, CourseStatus.LISTED)
        every { courseAdaptor.findById(course.id) } returns course
        every { productAdaptor.findById(product.id) } returns product
        every { courseAdaptor.save(any()) } answers { firstArg() }
        every { productAdaptor.save(any()) } answers { firstArg() }

        useCase.execute(course.id, CourseStatus.UNLISTED)

        assertAll(
            { assertEquals(CourseStatus.UNLISTED, course.status) },
            { assertFalse(product.visible) },
        )
    }

    @Test
    fun `ARCHIVED로 전이하면 course가 ARCHIVED가 되고 product가 visible=false 가 된다`() {
        val product = product().also { it.show() }
        val course = courseOf(product.id, CourseStatus.LISTED)
        every { courseAdaptor.findById(course.id) } returns course
        every { productAdaptor.findById(product.id) } returns product
        every { courseAdaptor.save(any()) } answers { firstArg() }
        every { productAdaptor.save(any()) } answers { firstArg() }

        useCase.execute(course.id, CourseStatus.ARCHIVED)

        assertAll(
            { assertEquals(CourseStatus.ARCHIVED, course.status) },
            { assertFalse(product.visible) },
        )
    }

    @Test
    fun `DRAFT로 전이하려 하면 CourseInvalidStatusTransitionException이 발생한다`() {
        val product = product()
        val course = courseOf(product.id, CourseStatus.LISTED)
        every { courseAdaptor.findById(course.id) } returns course
        every { productAdaptor.findById(product.id) } returns product

        assertThatThrownBy {
            useCase.execute(course.id, CourseStatus.DRAFT)
        }.isInstanceOf(CourseInvalidStatusTransitionException::class.java)
    }

    @Test
    fun `이미 LISTED 인 코스에 LISTED 전이를 요청해도 멱등하게 성공한다`() {
        val product = product().also { it.show() }
        val course = courseOf(product.id, CourseStatus.LISTED)
        every { courseAdaptor.findById(course.id) } returns course
        every { productAdaptor.findById(product.id) } returns product
        every { courseAdaptor.save(any()) } answers { firstArg() }
        every { productAdaptor.save(any()) } answers { firstArg() }

        useCase.execute(course.id, CourseStatus.LISTED)

        assertAll(
            { assertEquals(CourseStatus.LISTED, course.status) },
            { assertTrue(product.visible) },
        )
    }

    @Test
    fun `코스와 상품을 모두 save 한다`() {
        val product = product()
        val course = courseOf(product.id, CourseStatus.DRAFT)
        val courseSlot = slot<Course>()
        val productSlot = slot<CourseProduct>()
        every { courseAdaptor.findById(course.id) } returns course
        every { productAdaptor.findById(product.id) } returns product
        every { courseAdaptor.save(capture(courseSlot)) } answers { courseSlot.captured }
        every { productAdaptor.save(capture(productSlot)) } answers { productSlot.captured }

        useCase.execute(course.id, CourseStatus.LISTED)

        verify(exactly = 1) { courseAdaptor.save(any()) }
        verify(exactly = 1) { productAdaptor.save(any()) }
        assertAll(
            { assertEquals(CourseStatus.LISTED, courseSlot.captured.status) },
            { assertTrue(productSlot.captured.visible) },
        )
    }
}
