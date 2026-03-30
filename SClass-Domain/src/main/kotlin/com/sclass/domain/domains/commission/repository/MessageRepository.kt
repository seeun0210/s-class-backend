package com.sclass.domain.domains.commission.repository

import com.sclass.domain.domains.commission.domain.Message
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository : JpaRepository<Message, Long> {
    fun findByCommissionIdOrderByCreatedAtAsc(commissionId: Long): List<Message>
}
