package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class ActivateCourseUseCase(
    private val courseAdaptor: CourseAdaptor,
) {
    @Transactional
    fun execute(courseId: Long): CourseResponse {
        val course = courseAdaptor.findById(courseId)
        course.activate()
        return CourseResponse.from(courseAdaptor.save(course))
    }
}
