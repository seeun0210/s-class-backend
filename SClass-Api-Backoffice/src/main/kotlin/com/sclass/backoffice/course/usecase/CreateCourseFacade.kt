package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.backoffice.course.dto.CreateCourseRequest
import com.sclass.common.annotation.UseCase
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey

@UseCase
class CreateCourseFacade(
    private val createCourseUseCase: CreateCourseUseCase,
) {
    @DistributedLock(prefix = "course-product")
    fun execute(
        @LockKey productId: String,
        request: CreateCourseRequest,
    ): CourseResponse = createCourseUseCase.execute(request)
}
