package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.EnrollmentDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.exception.ProductNotFoundException
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetEnrollmentDetailUseCase(
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(enrollmentId: Long): EnrollmentDetailResponse {
        val enrollment = enrollmentAdaptor.findById(enrollmentId)
        val product = enrollment.productId?.let { findProductOrNull(it) }

        return EnrollmentDetailResponse.from(enrollment, product)
    }

    private fun findProductOrNull(productId: String): Product? =
        try {
            productAdaptor.findById(productId)
        } catch (_: ProductNotFoundException) {
            null
        }
}
