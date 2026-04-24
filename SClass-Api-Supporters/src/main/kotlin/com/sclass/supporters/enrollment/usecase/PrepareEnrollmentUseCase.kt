package com.sclass.supporters.enrollment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.enrollment.exception.EnrollmentInvalidPurchaseTargetException
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import com.sclass.supporters.enrollment.dto.PrepareEnrollmentResponse

@UseCase
class PrepareEnrollmentUseCase(
    private val productAdaptor: ProductAdaptor,
    private val prepareRegularEnrollmentUseCase: PrepareRegularEnrollmentUseCase,
    private val prepareMatchingEnrollmentUseCase: PrepareMatchingEnrollmentUseCase,
) {
    @DistributedLock(prefix = "course-product", waitTime = 30)
    fun execute(
        studentUserId: String,
        @LockKey productId: String,
        courseId: Long?,
        pgType: PgType,
    ): PrepareEnrollmentResponse {
        val product =
            productAdaptor.findById(productId) as? CourseProduct
                ?: throw ProductTypeMismatchException()

        return if (product.requiresMatching) {
            prepareMatchingEnrollmentUseCase.execute(
                studentUserId = studentUserId,
                productId = productId,
                courseId = courseId,
                pgType = pgType,
            )
        } else {
            prepareRegularEnrollmentUseCase.execute(
                studentUserId = studentUserId,
                productId = productId,
                product = product,
                courseId = courseId ?: throw EnrollmentInvalidPurchaseTargetException(),
                pgType = pgType,
            )
        }
    }
}
