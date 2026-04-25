package com.sclass.backoffice.enrollment.usecase

import com.sclass.backoffice.enrollment.dto.EnrollmentDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetEnrollmentDetailUseCase(
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(enrollmentId: Long): EnrollmentDetailResponse {
        val enrollment = enrollmentAdaptor.findById(enrollmentId)
        val product = enrollment.productId?.let { productAdaptor.findByIdOrNull(it) }

        return EnrollmentDetailResponse.from(enrollment, product)
    }
}
