package com.sclass.domain.domains.payment.repository

import com.sclass.domain.domains.payment.domain.Payment
import com.sclass.domain.domains.payment.domain.PaymentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface PaymentRepository : JpaRepository<Payment, String> {
    fun findByPgOrderId(pgOrderId: String): Payment?

    fun findAllByUserId(
        userId: String,
        pageable: Pageable,
    ): Page<Payment>

    fun findAllByStatusAndCreatedAtBefore(
        status: PaymentStatus,
        createdAt: LocalDateTime,
    ): List<Payment>
}
