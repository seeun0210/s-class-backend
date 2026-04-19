package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCourseDetailUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(courseId: Long): CourseDetailResponse {
        val dto = courseAdaptor.findCourseDetailById(courseId)
        return CourseDetailResponse.from(dto, thumbnailUrlResolver.resolve(dto.courseProduct?.thumbnailFileId))
    }
}
