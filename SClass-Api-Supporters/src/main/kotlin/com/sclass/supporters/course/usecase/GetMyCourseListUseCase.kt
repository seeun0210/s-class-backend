package com.sclass.supporters.course.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.supporters.course.dto.MyCourseResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMyCourseListUseCase(
    private val courseAdaptor: CourseAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(teacherUserId: String): List<MyCourseResponse> =
        courseAdaptor
            .findAllByTeacherUserIdWithEnrollmentCount(teacherUserId)
            .map { MyCourseResponse.from(it) }
}
