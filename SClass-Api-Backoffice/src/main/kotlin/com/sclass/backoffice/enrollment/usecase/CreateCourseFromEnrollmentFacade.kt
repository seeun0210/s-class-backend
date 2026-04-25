package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.EnrollmentResponse
import com.sclass.common.annotation.UseCase
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey

@UseCase
class CreateCourseFromEnrollmentFacade(
    private val createCourseFromEnrollmentUseCase: CreateCourseFromEnrollmentUseCase,
) {
    @DistributedLock(prefix = "enrollment")
    fun execute(
        @LockKey enrollmentId: Long,
        teacherUserId: String,
    ): EnrollmentResponse = createCourseFromEnrollmentUseCase.execute(enrollmentId, teacherUserId)
}
