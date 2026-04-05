package com.sclass.domain.domains.payment.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false, length = 26)
    val userId: String,

    @Column(nullable = false, length = 26)
    val productId: String,

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

    @Column(columnDefinition = "TEXT")
    var metadata: String? = null,
) : BaseTimeEntity() {
    fun markPgApproved(pgTid: String) {
        check(status == PaymentStatus.PENDING) { "PENDING 상태에서만 PG 승인 가능합니다" }
        this.pgTid = pgTid
        this.status = PaymentStatus.PG_APPROVED
    }

    fun markCompleted() {
        check(status == PaymentStatus.PG_APPROVED) { "PG_APPROVED 상태에서만 완료 처리 가능합니다" }
        this.status = PaymentStatus.COMPLETED
    }

    fun markPgApproveFailed() {
        check(status == PaymentStatus.PENDING) { "PENDING 상태에서만 PG 승인 실패 처리 가능합니다" }
        this.status = PaymentStatus.PG_APPROVE_FAILED
    }

    fun markIssueCoinFailed() {
        check(status == PaymentStatus.PG_APPROVED) { "PG_APPROVED 상태에서만 코인 발급 실패 처리 가능합니다" }
        this.status = PaymentStatus.ISSUE_COIN_FAILED
    }

    fun markCompensationNeeded() {
        this.status = PaymentStatus.COMPENSATION_NEEDED
    }

    fun markCancelled() {
        check(status == PaymentStatus.COMPLETED) { "COMPLETED 상태에서만 취소 가능합니다" }
        this.status = PaymentStatus.CANCELLED
    }

    fun markPgCancelFailed() {
        this.status = PaymentStatus.PG_CANCEL_FAILED
    }

    fun markCoinRefundFailed() {
        this.status = PaymentStatus.COIN_REFUND_FAILED
    }
}
