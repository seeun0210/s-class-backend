package com.sclass.backoffice.enrollment.dto

import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.EnrollmentType
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.toProductType
import java.time.LocalDateTime

data class EnrollmentDetailResponse(
    val id: Long,
    val courseId: Long?,
    val productId: String?,
    val productName: String?,
    val productType: ProductType?,
    val studentUserId: String,
    val enrollmentType: EnrollmentType,
    val tuitionAmountWon: Int,
    val status: EnrollmentStatus,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(
            enrollment: Enrollment,
            product: Product?,
        ) = EnrollmentDetailResponse(
            id = enrollment.id,
            courseId = enrollment.courseId,
            productId = enrollment.productId,
            productName = product?.name,
            productType = product?.toProductType(),
            studentUserId = enrollment.studentUserId,
            enrollmentType = enrollment.enrollmentType,
            tuitionAmountWon = enrollment.tuitionAmountWon,
            status = enrollment.status,
            startAt = enrollment.startAt,
            endAt = enrollment.endAt,
            createdAt = enrollment.createdAt,
            updatedAt = enrollment.updatedAt,
        )
    }
}
