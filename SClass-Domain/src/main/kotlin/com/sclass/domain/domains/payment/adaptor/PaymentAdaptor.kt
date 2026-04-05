package com.sclass.domain.domains.payment.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import com.sclass.domain.domains.payment.exception.PaymentNotFoundException
import com.sclass.domain.domains.payment.repository.PaymentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

@Adaptor
class PaymentAdaptor(
    private val paymentRepository: PaymentRepository,
) {
    fun findById(id: String): Payment = paymentRepository.findById(id).orElseThrow { PaymentNotFoundException() }

    fun findByPgOrderIdOrNull(pgOrderId: String): Payment? = paymentRepository.findByPgOrderId(pgOrderId)

    fun findAllByUserId(
        userId: String,
        pageable: Pageable,
    ): Page<Payment> = paymentRepository.findAllByUserId(userId, pageable)

    fun findPendingOlderThan(threshold: LocalDateTime): List<Payment> =
        paymentRepository.findAllByStatusAndCreatedAtBefore(PaymentStatus.PENDING, threshold)

    fun save(payment: Payment): Payment = paymentRepository.save(payment)
}
