package com.sclass.supporters.enrollment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import com.sclass.supporters.enrollment.dto.MyEnrollmentResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMyEnrollmentsUseCase(
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(studentUserId: String): List<MyEnrollmentResponse> =
        enrollmentAdaptor.findAllByStudentWithCourse(studentUserId).map {
            MyEnrollmentResponse.from(
                dto = it,
                courseThumbnailUrl = thumbnailUrlResolver.resolve(it.courseProduct?.thumbnailFileId),
                membershipThumbnailUrl = thumbnailUrlResolver.resolve(it.membershipProduct?.thumbnailFileId),
            )
        }
}
