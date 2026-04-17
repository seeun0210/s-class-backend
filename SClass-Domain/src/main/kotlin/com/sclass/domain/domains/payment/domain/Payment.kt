package com.sclass.domain.domains.payment.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.payment.exception.InvalidPaymentStatusException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false, length = 26)
    val userId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    val targetType: PaymentTargetType,

    @Column(name = "target_id", nullable = false, length = 26)
    val targetId: String,

    @Column(nullable = false)
    val amount: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val pgType: PgType,

    @Column(nullable = false, unique = true)
    val pgOrderId: String,

    @Column
    var pgTid: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(name = "receipt_url", length = 500)
    var receiptUrl: String? = null,

    @Column(columnDefinition = "TEXT")
    var metadata: String? = null,

    @Version
    var version: Long = 0,
) : BaseTimeEntity() {
    fun markPgApproved(
        pgTid: String,
        receiptUrl: String? = null,
    ) {
        if (status != PaymentStatus.PENDING) {
            throw InvalidPaymentStatusException()
        }
        this.pgTid = pgTid
        this.receiptUrl = receiptUrl
        this.status = PaymentStatus.PG_APPROVED
    }

    fun markCompleted() {
        if (status != PaymentStatus.PG_APPROVED) {
            throw InvalidPaymentStatusException()
        }
        this.status = PaymentStatus.COMPLETED
    }

    fun markPgApproveFailed() {
        if (status != PaymentStatus.PENDING) {
            throw InvalidPaymentStatusException()
        }
        this.status = PaymentStatus.PG_APPROVE_FAILED
    }

    fun markIssueCoinFailed() {
        if (status != PaymentStatus.PG_APPROVED) {
            throw InvalidPaymentStatusException()
        }
        this.status = PaymentStatus.ISSUE_COIN_FAILED
    }

    fun markCompensationNeeded() {
        this.status = PaymentStatus.COMPENSATION_NEEDED
    }

    fun markCancelled() {
        if (status != PaymentStatus.COMPLETED) {
            throw InvalidPaymentStatusException()
        }
        this.status = PaymentStatus.CANCELLED
    }

    fun markPgCancelFailed() {
        this.status = PaymentStatus.PG_CANCEL_FAILED
    }

    fun markCoinRefundFailed() {
        this.status = PaymentStatus.COIN_REFUND_FAILED
    }
}
