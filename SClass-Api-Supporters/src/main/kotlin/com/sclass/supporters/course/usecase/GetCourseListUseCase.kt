package com.sclass.supporters.course.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.supporters.course.dto.CourseResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCourseListUseCase(
    private val courseAdaptor: CourseAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(): List<CourseResponse> = courseAdaptor.findAllActiveWithTeacher().map { CourseResponse.from(it) }
}
