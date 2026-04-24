package com.sclass.backoffice.product.course.usecase

import com.sclass.backoffice.product.course.dto.CourseProductResponse
import com.sclass.backoffice.product.course.dto.CreateCourseProductRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCourseProductUseCase(
    private val productAdaptor: ProductAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional
    fun execute(request: CreateCourseProductRequest): CourseProductResponse {
        val product =
            CourseProduct(
                name = request.name,
                priceWon = request.priceWon,
                description = request.description,
                thumbnailFileId = request.thumbnailFileId,
                totalLessons = request.totalLessons,
                curriculum = request.curriculum,
                requiresMatching = request.requiresMatching,
            ).also {
                if (request.visible) it.show() else it.hide()
            }

        val saved = productAdaptor.save(product) as CourseProduct
        return CourseProductResponse.from(saved, thumbnailUrlResolver.resolve(saved.thumbnailFileId))
    }
}
