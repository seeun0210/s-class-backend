package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.backoffice.course.dto.CreateCourseRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.exception.CourseMatchingProductNotCreatableException
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCourseUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(request: CreateCourseRequest): CourseResponse {
        val product =
            productAdaptor.findById(request.productId) as? CourseProduct
                ?: throw ProductTypeMismatchException()
        if (product.requiresMatching) throw CourseMatchingProductNotCreatableException()
        val course =
            courseAdaptor.save(
                Course(
                    productId = product.id,
                    teacherUserId = request.teacherUserId,
                    organizationId = request.organizationId,
                    maxEnrollments = request.maxEnrollments,
                    enrollmentStartAt = request.enrollmentStartAt,
                    enrollmentDeadLine = request.enrollmentDeadLine,
                    totalLessons = product.totalLessons,
                    curriculum = product.curriculum,
                    startAt = request.startAt,
                    endAt = request.endAt,
                ),
            )
        return CourseResponse.from(course, product, thumbnailUrlResolver.resolve(product.thumbnailFileId))
    }
}
