package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey

@UseCase
class ChangeCourseStatusFacade(
    private val courseAdaptor: CourseAdaptor,
    private val changeCourseStatusUseCase: ChangeCourseStatusUseCase,
) {
    fun execute(
        courseId: Long,
        targetStatus: CourseStatus,
    ): CourseResponse {
        val course = courseAdaptor.findById(courseId)
        return executeLocked(courseId, course.productId, targetStatus)
    }

    @DistributedLock(prefix = "course-product")
    fun executeLocked(
        courseId: Long,
        @LockKey productId: String,
        targetStatus: CourseStatus,
    ): CourseResponse = changeCourseStatusUseCase.execute(courseId, targetStatus)
}
