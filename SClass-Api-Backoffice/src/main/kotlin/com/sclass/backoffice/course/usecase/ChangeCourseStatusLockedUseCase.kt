package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey

@UseCase
class ChangeCourseStatusLockedUseCase(
    private val changeCourseStatusUseCase: ChangeCourseStatusUseCase,
) {
    @DistributedLock(prefix = "course-product")
    fun execute(
        courseId: Long,
        @LockKey productId: String,
        targetStatus: CourseStatus,
    ): CourseResponse = changeCourseStatusUseCase.execute(courseId, targetStatus)
}
