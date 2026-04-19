package com.sclass.supporters.membership.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.exception.EnrollmentAlreadyExistsException
import com.sclass.domain.domains.enrollment.exception.MembershipCapacityExceededException
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentTargetType
import com.sclass.domain.domains.payment.domain.PgType
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.exception.ProductNotPurchasableException
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import com.sclass.supporters.membership.dto.PrepareMembershipPurchaseResponse
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@UseCase
class PrepareMembershipPurchaseUseCase(
    private val productAdaptor: ProductAdaptor,
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val paymentAdaptor: PaymentAdaptor,
) {
    @Transactional
    @DistributedLock(prefix = "membership-purchase")
    fun execute(
        studentUserId: String,
        @LockKey membershipProductId: String,
        pgType: PgType,
    ): PrepareMembershipPurchaseResponse {
        val product =
            productAdaptor.findById(membershipProductId) as? MembershipProduct
                ?: throw ProductTypeMismatchException()
        if (!product.visible) throw ProductNotPurchasableException()
        product.validateSaleable(LocalDateTime.now())

        product.maxEnrollments?.let { cap ->
            val live = enrollmentAdaptor.countLiveMembershipEnrollments(product.id)
            if (live >= cap) throw MembershipCapacityExceededException()
        }
        if (enrollmentAdaptor.findLiveMembershipEnrollment(product.id, studentUserId) != null) {
            throw EnrollmentAlreadyExistsException()
        }

        val payment =
            paymentAdaptor.save(
                Payment(
                    userId = studentUserId,
                    targetType = PaymentTargetType.MEMBERSHIP_PRODUCT,
                    targetId = product.id,
                    amount = product.priceWon,
                    pgType = pgType,
                    pgOrderId = Ulid.generate(),
                ),
            )
        enrollmentAdaptor.save(
            Enrollment.createForMembershipPurchase(
                productId = product.id,
                studentUserId = studentUserId,
                tuitionAmountWon = product.priceWon,
                paymentId = payment.id,
            ),
        )

        return PrepareMembershipPurchaseResponse(
            paymentId = payment.id,
            pgOrderId = payment.pgOrderId,
            amount = payment.amount,
            productId = product.id,
            productName = product.name,
        )
    }
}
