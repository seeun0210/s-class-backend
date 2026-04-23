package com.sclass.backoffice.product.course.usecase

import com.sclass.backoffice.product.course.dto.CourseProductResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCourseProductDetailUseCase(
    private val productAdaptor: ProductAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(productId: String): CourseProductResponse {
        val product =
            productAdaptor.findById(productId) as? CourseProduct
                ?: throw ProductTypeMismatchException()

        return CourseProductResponse.from(product, thumbnailUrlResolver.resolve(product.thumbnailFileId))
    }
}
