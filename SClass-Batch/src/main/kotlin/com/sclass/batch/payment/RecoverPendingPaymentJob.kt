package com.sclass.batch.payment

import com.sclass.common.annotation.BatchJob
import com.sclass.domain.domains.payment.adaptor.PaymentAdaptor
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime

@BatchJob
class RecoverPendingPaymentJob(
    private val paymentAdaptor: PaymentAdaptor,
    private val processor: PendingPaymentProcessor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 600_000) // 10분마다
    fun execute() {
        val threshold = LocalDateTime.now().minusMinutes(10)
        val pendingPayments =
            paymentAdaptor.findPendingOlderThan(threshold)

        if (pendingPayments.isEmpty()) return

        log.info(
            "PENDING 결제 복구 시작 - {}건",
            pendingPayments.size,
        )

        if (pendingPayments.size > MAX_BATCH_SIZE) {
            log.warn("PENDING 결제 {}건 초과 - 전체 COMPENSATION_NEEDED 처리", pendingPayments.size)
            pendingPayments.forEach { payment ->
                payment.markCompensationNeeded()
                paymentAdaptor.save(payment)
            }
            return
        }

        pendingPayments.forEach { payment ->
            processor.process(payment)
            Thread.sleep(200) // rate limit 방지
        }

        log.info(
            "PENDING 결제 복구 완료 - {}건",
            pendingPayments.size,
        )
    }

    companion object {
        private const val MAX_BATCH_SIZE = 50
    }
}
