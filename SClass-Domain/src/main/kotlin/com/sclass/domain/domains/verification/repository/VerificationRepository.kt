package com.sclass.domain.domains.verification.repository

import com.sclass.domain.domains.verification.domain.Verification
import com.sclass.domain.domains.verification.domain.VerificationChannel
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface VerificationRepository : JpaRepository<Verification, String> {
    fun findFirstByChannelAndTargetOrderByCreatedAtDesc(
        channel: VerificationChannel,
        target: String,
    ): Verification?

    fun countByChannelAndTargetAndCreatedAtAfter(
        channel: VerificationChannel,
        target: String,
        createdAt: LocalDateTime,
    ): Long
}
