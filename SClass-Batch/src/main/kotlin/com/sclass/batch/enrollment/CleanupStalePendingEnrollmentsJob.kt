package com.sclass.batch.enrollment

import com.sclass.common.annotation.BatchJob
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import com.sclass.domain.domains.payment.domain.PaymentCancelSource
import com.sclass.domain.domains.payment.domain.PaymentStatus
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.LocalDateTime

@BatchJob
class CleanupStalePendingEnrollmentsJob(
    private val enrollmentAdaptor: EnrollmentAdaptor,
    private val paymentAdaptor: PaymentAdaptor,
    private val txTemplate: TransactionTemplate,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = INTERVAL_MS)
    fun execute() {
        val threshold = LocalDateTime.now(clock).minusMinutes(TTL_MINUTES)
        val staleEnrollments = enrollmentAdaptor.findPendingPaymentOlderThan(threshold)

        if (staleEnrollments.isEmpty()) return

        log.info("만료된 PENDING_PAYMENT enrollment 정리 시작 - {}건", staleEnrollments.size)

        var successCount = 0
        staleEnrollments.forEach { stale ->
            runCatching {
                txTemplate.execute {
                    val enrollment = enrollmentAdaptor.findById(stale.id)
                    if (enrollment.status != EnrollmentStatus.PENDING_PAYMENT) return@execute null

                    val paymentId = enrollment.paymentId ?: return@execute null
                    val payment = paymentAdaptor.findById(paymentId)
                    when (payment.status) {
                        PaymentStatus.PENDING -> {
                            payment.markCancelled(PaymentCancelSource.PAYMENT_TIMEOUT)
                            paymentAdaptor.save(payment)
                        }
                        PaymentStatus.CANCELLED -> {}
                        else -> {
                            // PG_APPROVED/COMPLETED 등 결제가 이미 진행 중 — 건너뜀
                            log.warn(
                                "PENDING_PAYMENT enrollment이지만 결제 상태가 취소 불가 enrollmentId={} paymentStatus={}",
                                enrollment.id,
                                payment.status,
                            )
                            return@execute null
                        }
                    }
                    enrollment.cancel("결제 시간 초과 (${TTL_MINUTES}분)")
                    enrollmentAdaptor.save(enrollment)
                    null
                }
                successCount++
            }.onFailure { e ->
                log.error("PENDING_PAYMENT enrollment 정리 실패 enrollmentId={}", stale.id, e)
            }
        }

        log.info("만료된 PENDING_PAYMENT enrollment 정리 완료 - 성공 {}건 / 전체 {}건", successCount, staleEnrollments.size)
    }

    companion object {
        private const val TTL_MINUTES = 30L
        private const val INTERVAL_MS = 5 * 60 * 1000L
    }
}
