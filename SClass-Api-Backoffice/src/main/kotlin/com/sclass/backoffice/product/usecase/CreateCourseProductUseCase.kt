package com.sclass.backoffice.product.usecase

import com.sclass.backoffice.product.dto.CreateCourseProductRequest
import com.sclass.backoffice.product.dto.ProductResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCourseProductUseCase(
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(request: CreateCourseProductRequest): ProductResponse {
        val product =
            productAdaptor.save(
                CourseProduct(
                    name = request.name,
                    priceWon = request.priceWon,
                    totalLessons = request.totalLessons,
                ),
            )
        return ProductResponse.from(product)
    }
}
