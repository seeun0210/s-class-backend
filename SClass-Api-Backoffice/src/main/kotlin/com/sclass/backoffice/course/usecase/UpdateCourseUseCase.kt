package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.backoffice.course.dto.UpdateCourseRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@UseCase
class UpdateCourseUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(
        courseId: Long,
        request: UpdateCourseRequest,
    ): CourseResponse {
        val course = courseAdaptor.findById(courseId)
        val product =
            productAdaptor.findById(course.productId) as? CourseProduct
                ?: throw ProductTypeMismatchException()
        val now = LocalDateTime.now()

        product.updateCatalog(
            newName = request.name,
            newDescription = request.description,
            newThumbnailFileId = request.thumbnailFileId,
            newPriceWon = request.priceWon,
        )
        product.updateCurriculum(request.curriculum)

        if (request.hasEnrollmentConstraintChange()) {
            val liveCount = enrollmentAdaptor.countLiveEnrollments(courseId).toInt()
            course.updateEnrollmentConstraints(
                now = now,
                newMaxEnrollments = request.maxEnrollments,
                newEnrollmentStartAt = request.enrollmentStartAt,
                newEnrollmentDeadLine = request.enrollmentDeadLine,
                currentLiveCount = liveCount,
            )
        }
        if (request.hasScheduleChange()) {
            course.updateSchedule(now, request.startAt, request.endAt)
        }

        return CourseResponse.from(course, product, thumbnailUrlResolver.resolve(product.thumbnailFileId))
    }
}
