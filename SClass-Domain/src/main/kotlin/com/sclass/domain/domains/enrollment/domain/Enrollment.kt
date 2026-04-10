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

    @Column(name = "course_id", nullable = false)
    val courseId: Long,

    @Column(name = "student_user_id", nullable = false, length = 26)
    val studentUserId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_type", nullable = false, length = 20)
    val enrollmentType: EnrollmentType,

    // ===== 가격 스냅샷 (등록 시점 복사) =====
    @Column(name = "tuition_amount_won", nullable = false)
    val tuitionAmountWon: Int,

    @Column(name = "teacher_payout_per_session_won", nullable = false)
    val teacherPayoutPerSessionWon: Int,

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

    @Column(name = "cancelled_at")
    var cancelledAt: LocalDateTime? = null,

    @Column(name = "cancel_reason", length = 500)
    var cancelReason: String? = null,
) : BaseTimeEntity() {
    fun markPaid() {
        require(enrollmentType == EnrollmentType.PURCHASE) {
            "markPaid is only valid for PURCHASE enrollments"
        }
        require(paymentId != null) { "paymentId must be set before markPaid" }
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
         *   → PG 콜백 수신 시 payment.markCompleted() → enrollment.markPaid()
         */
        fun createForPurchase(
            courseId: Long,
            studentUserId: String,
            tuitionAmountWon: Int,
            teacherPayoutPerSessionWon: Int,
            paymentId: String,
        ): Enrollment =
            Enrollment(
                courseId = courseId,
                studentUserId = studentUserId,
                enrollmentType = EnrollmentType.PURCHASE,
                tuitionAmountWon = tuitionAmountWon,
                teacherPayoutPerSessionWon = teacherPayoutPerSessionWon,
                paymentId = paymentId,
                status = EnrollmentStatus.PENDING_PAYMENT,
            )

        /**
         * 백오피스/LMS 관리자가 직접 등록.
         * 결제 절차 없이 바로 ACTIVE 상태로 생성.
         */
        fun createByGrant(
            courseId: Long,
            studentUserId: String,
            grantedByUserId: String,
            grantReason: String,
            teacherPayoutPerSessionWon: Int,
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
                teacherPayoutPerSessionWon = teacherPayoutPerSessionWon,
                status = EnrollmentStatus.ACTIVE,
                grantedByUserId = grantedByUserId,
                grantReason = grantReason,
            )
        }
    }
}
