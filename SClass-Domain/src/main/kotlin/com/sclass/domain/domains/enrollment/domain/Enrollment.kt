package com.sclass.domain.domains.enrollment.domain

import com.sclass.domain.common.model.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "enrollments",
    indexes = [
        Index(name = "idx_enrollments_student", columnList = "student_user_id"),
        Index(name = "idx_enrollments_course", columnList = "course_id"),
        Index(name = "idx_enrollments_status", columnList = "status"),
        Index(name = "idx_enrollments_type", columnList = "enrollment_type"),
        Index(name = "idx_enrollments_payment", columnList = "payment_id"),
        Index(name = "idx_enrollments_granted_by", columnList = "granted_by_user_id"),
    ],
)
class Enrollment private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "course_id")
    val courseId: Long? = null,

    // purchase 대상 product 스냅샷
    @Column(name = "product_id", length = 26)
    val productId: String? = null,

    @Column(name = "student_user_id", nullable = false, length = 26)
    val studentUserId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_type", nullable = false, length = 20)
    val enrollmentType: EnrollmentType,

    // ===== 가격 스냅샷 (등록 시점 복사) =====
    @Column(name = "tuition_amount_won", nullable = false)
    val tuitionAmountWon: Int,

    // ===== 결제 FK (PURCHASE일 때만 채워짐) =====
    // payment 도메인의 Payment.id (ULID) 참조
    @Column(name = "payment_id", length = 26)
    var paymentId: String? = null,

    // ===== 관리자 부여 감사 =====
    @Column(name = "granted_by_user_id", length = 26)
    val grantedByUserId: String? = null,

    @Column(name = "grant_reason", length = 500)
    val grantReason: String? = null,

    // ===== 상태 =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: EnrollmentStatus,

    // ===== 유효기간 (MEMBERSHIP 구매 시에만 채워짐) =====
    @Column(name = "start_at")
    var startAt: LocalDateTime? = null,

    @Column(name = "end_at")
    var endAt: LocalDateTime? = null,

    @Column(name = "cancelled_at")
    var cancelledAt: LocalDateTime? = null,

    @Column(name = "cancel_reason", length = 500)
    var cancelReason: String? = null,
) : BaseTimeEntity() {
    fun markCoursePaid() {
        require(enrollmentType == EnrollmentType.PURCHASE)
        require(paymentId != null)
        validateTransition(EnrollmentStatus.ACTIVE)
        this.status = EnrollmentStatus.ACTIVE
    }

    fun markMembershipPaid(
        startAt: LocalDateTime,
        endAt: LocalDateTime,
    ) {
        require(enrollmentType == EnrollmentType.PURCHASE)
        require(paymentId != null)
        this.startAt = startAt
        this.endAt = endAt
        validateTransition(EnrollmentStatus.ACTIVE)
        this.status = EnrollmentStatus.ACTIVE
    }

    fun complete() {
        validateTransition(EnrollmentStatus.COMPLETED)
        this.status = EnrollmentStatus.COMPLETED
    }

    fun cancel(
        reason: String,
        at: LocalDateTime = LocalDateTime.now(),
    ) {
        validateTransition(EnrollmentStatus.CANCELLED)
        this.status = EnrollmentStatus.CANCELLED
        this.cancelledAt = at
        this.cancelReason = reason
    }

    fun refund(
        reason: String,
        refundedAt: LocalDateTime = LocalDateTime.now(),
    ) {
        require(enrollmentType == EnrollmentType.PURCHASE) {
            "refund is only valid for PURCHASE enrollments"
        }
        validateTransition(EnrollmentStatus.REFUNDED)
        this.status = EnrollmentStatus.REFUNDED
        this.cancelledAt = refundedAt
        this.cancelReason = reason
    }

    private fun validateTransition(target: EnrollmentStatus) {
        val allowed =
            when (target) {
                EnrollmentStatus.PENDING_PAYMENT -> emptySet()
                EnrollmentStatus.ACTIVE -> setOf(EnrollmentStatus.PENDING_PAYMENT)
                EnrollmentStatus.COMPLETED -> setOf(EnrollmentStatus.ACTIVE)
                EnrollmentStatus.CANCELLED -> setOf(EnrollmentStatus.PENDING_PAYMENT, EnrollmentStatus.ACTIVE)
                EnrollmentStatus.REFUNDED -> setOf(EnrollmentStatus.ACTIVE, EnrollmentStatus.CANCELLED)
            }
        require(status in allowed) { "Cannot transition from $status to $target" }
    }

    companion object {
        /**
         * 학생이 PG 결제로 등록.
         * 호출 순서: Payment 먼저 생성 → Enrollment.createForPurchase(..., paymentId = payment.id)
         *   → PG 콜백 수신 시 payment.markCompleted() → enrollment.markCoursePaid()
         */
        fun createForPurchase(
            productId: String,
            studentUserId: String,
            tuitionAmountWon: Int,
            paymentId: String,
            courseId: Long,
        ): Enrollment =
            Enrollment(
                courseId = courseId,
                productId = productId,
                studentUserId = studentUserId,
                enrollmentType = EnrollmentType.PURCHASE,
                tuitionAmountWon = tuitionAmountWon,
                paymentId = paymentId,
                status = EnrollmentStatus.PENDING_PAYMENT,
            )

        fun createForMembershipPurchase(
            productId: String,
            studentUserId: String,
            tuitionAmountWon: Int,
            paymentId: String,
        ): Enrollment =
            Enrollment(
                courseId = null,
                productId = productId,
                studentUserId = studentUserId,
                enrollmentType = EnrollmentType.PURCHASE,
                tuitionAmountWon = tuitionAmountWon,
                paymentId = paymentId,
                status = EnrollmentStatus.PENDING_PAYMENT,
            )

        fun createMembershipByGrant(
            productId: String,
            studentUserId: String,
            grantedByUserId: String,
            grantReason: String,
            startAt: LocalDateTime,
            endAt: LocalDateTime,
        ): Enrollment =
            Enrollment(
                courseId = null,
                productId = productId,
                studentUserId = studentUserId,
                enrollmentType = EnrollmentType.ADMIN_GRANT,
                tuitionAmountWon = 0,
                status = EnrollmentStatus.ACTIVE,
                grantedByUserId = grantedByUserId,
                grantReason = grantReason,
                startAt = startAt,
                endAt = endAt,
            )

        /**
         * 백오피스/LMS 관리자가 직접 등록.
         * 결제 절차 없이 바로 ACTIVE 상태로 생성.
         */
        fun createByGrant(
            courseId: Long?,
            studentUserId: String,
            grantedByUserId: String,
            grantReason: String,
            tuitionAmountWon: Int = 0,
            type: EnrollmentType = EnrollmentType.ADMIN_GRANT,
        ): Enrollment {
            require(type != EnrollmentType.PURCHASE) {
                "createByGrant cannot be used for PURCHASE type"
            }
            return Enrollment(
                courseId = courseId,
                studentUserId = studentUserId,
                enrollmentType = type,
                tuitionAmountWon = tuitionAmountWon,
                status = EnrollmentStatus.ACTIVE,
                grantedByUserId = grantedByUserId,
                grantReason = grantReason,
            )
        }
    }
}
