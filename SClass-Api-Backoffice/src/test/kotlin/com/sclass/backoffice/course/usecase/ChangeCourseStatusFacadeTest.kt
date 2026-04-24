package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChangeCourseStatusFacadeTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var changeCourseStatusLockedUseCase: ChangeCourseStatusLockedUseCase
    private lateinit var facade: ChangeCourseStatusFacade

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        changeCourseStatusLockedUseCase = mockk()
        facade = ChangeCourseStatusFacade(courseAdaptor, changeCourseStatusLockedUseCase)
    }

    @Test
    fun `courseId로 productId를 해석한 뒤 locked use case에 위임한다`() {
        val course =
            Course(
                id = 1L,
                productId = "product-id-00000000001",
                teacherUserId = "teacher-id-00000000001",
                status = CourseStatus.DRAFT,
            )
        val response = mockk<CourseResponse>()
        every { courseAdaptor.findById(course.id) } returns course
        every {
            changeCourseStatusLockedUseCase.execute(
                courseId = course.id,
                productId = course.productId,
                targetStatus = CourseStatus.LISTED,
            )
        } returns response

        facade.execute(course.id, CourseStatus.LISTED)

        verify(exactly = 1) { courseAdaptor.findById(course.id) }
        verify(exactly = 1) {
            changeCourseStatusLockedUseCase.execute(
                courseId = course.id,
                productId = course.productId,
                targetStatus = CourseStatus.LISTED,
            )
        }
    }
}
