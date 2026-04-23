package com.sclass.backoffice.product.course.usecase

import com.sclass.backoffice.product.course.dto.CourseProductPageResponse
import com.sclass.backoffice.product.course.dto.CourseProductResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.infrastructure.s3.ThumbnailUrlResolver
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCourseProductListUseCase(
    private val productAdaptor: ProductAdaptor,
    private val thumbnailUrlResolver: ThumbnailUrlResolver,
) {
    @Transactional(readOnly = true)
    fun execute(pageable: Pageable): CourseProductPageResponse =
        CourseProductPageResponse.from(
            productAdaptor.findCourseProducts(pageable).map { product ->
                CourseProductResponse.from(product, thumbnailUrlResolver.resolve(product.thumbnailFileId))
            },
        )
}
