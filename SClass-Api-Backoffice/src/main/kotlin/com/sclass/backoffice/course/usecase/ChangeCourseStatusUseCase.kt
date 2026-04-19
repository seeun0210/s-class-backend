package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.exception.CourseInvalidStatusTransitionException
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class ChangeCourseStatusUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val productAdaptor: ProductAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(
        courseId: Long,
        targetStatus: CourseStatus,
    ): CourseResponse {
        val course = courseAdaptor.findById(courseId)
        val product =
            productAdaptor.findById(course.productId) as? CourseProduct
                ?: throw ProductTypeMismatchException()

        when (targetStatus) {
            CourseStatus.LISTED -> {
                course.list()
                product.show()
            }
            CourseStatus.UNLISTED -> {
                course.unlist()
                product.hide()
            }
            CourseStatus.ARCHIVED -> {
                course.archive()
                product.hide()
            }
            CourseStatus.DRAFT -> throw CourseInvalidStatusTransitionException()
        }

        val saved = courseAdaptor.save(course)
        productAdaptor.save(product)
        return CourseResponse.from(saved, product, thumbnailUrlResolver.resolve(product.thumbnailFileId))
    }
}
