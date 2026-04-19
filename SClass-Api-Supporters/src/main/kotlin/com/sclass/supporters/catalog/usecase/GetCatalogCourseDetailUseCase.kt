package com.sclass.supporters.catalog.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import com.sclass.supporters.catalog.dto.CatalogCourseDetailResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCatalogCourseDetailUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(courseId: Long): CatalogCourseDetailResponse {
        val dto = courseAdaptor.findCatalogCourseById(courseId)
        val liveCount = enrollmentAdaptor.countLiveEnrollments(courseId)
        val thumbnailUrl = thumbnailUrlResolver.resolve(dto.courseProduct?.thumbnailFileId)
        return CatalogCourseDetailResponse.from(dto, liveCount, thumbnailUrl)
    }
}
