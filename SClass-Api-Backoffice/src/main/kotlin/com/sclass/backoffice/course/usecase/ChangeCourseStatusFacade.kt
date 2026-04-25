package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.CourseStatus

@UseCase
class ChangeCourseStatusFacade(
    private val courseAdaptor: CourseAdaptor,
    private val changeCourseStatusLockedUseCase: ChangeCourseStatusLockedUseCase,
) {
    fun execute(
        courseId: Long,
        targetStatus: CourseStatus,
    ): CourseResponse {
        val course = courseAdaptor.findById(courseId)
        return changeCourseStatusLockedUseCase.execute(courseId, course.productId, targetStatus)
    }
}
