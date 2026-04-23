package com.sclass.backoffice.product.course.usecase

import com.sclass.backoffice.product.course.dto.CourseProductResponse
import com.sclass.backoffice.product.course.dto.UpdateCourseProductRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.exception.CourseMatchingProductHasPendingMatchEnrollmentException
import com.sclass.domain.domains.course.exception.CourseMatchingProductNotConvertibleException
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateCourseProductUseCase(
    private val productAdaptor: ProductAdaptor,
    private val courseAdaptor: CourseAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(
        productId: String,
        request: UpdateCourseProductRequest,
    ): CourseProductResponse {
        val product = productAdaptor.findCourseProductById(productId)
        if (product.requiresMatching && request.requiresMatching == false) {
            if (enrollmentAdaptor.hasPendingUnassignedMatchingEnrollment(product.id)) {
                throw CourseMatchingProductHasPendingMatchEnrollmentException()
            }
            val linkedCourses = courseAdaptor.findAllByProductId(product.id)
            if (linkedCourses.size > 1) {
                throw CourseMatchingProductNotConvertibleException()
            }
        }

        product.updateCatalog(
            newName = request.name,
            newDescription = request.description,
            newThumbnailFileId = request.thumbnailFileId,
            newPriceWon = request.priceWon,
        )
        product.updateFulfillmentInfo(
            newCurriculum = request.curriculum,
            newTotalLessons = request.totalLessons,
            newRequiresMatching = request.requiresMatching,
        )
        request.visible?.let { visible ->
            if (visible) product.show() else product.hide()
        }

        return CourseProductResponse.from(product, thumbnailUrlResolver.resolve(product.thumbnailFileId))
    }
}
