package com.sclass.domain.domains.verification.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.verification.domain.Verification
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.repository.VerificationRepository
import java.time.LocalDateTime

@Adaptor
class VerificationAdaptor(
    private val verificationRepository: VerificationRepository,
) {
    fun save(verification: Verification): Verification = verificationRepository.save(verification)

    fun findLatestOrNull(
        channel: VerificationChannel,
        target: String,
    ): Verification? = verificationRepository.findFirstByChannelAndTargetOrderByCreatedAtDesc(channel, target)

    fun countRecent(
        channel: VerificationChannel,
        target: String,
        after: LocalDateTime,
    ): Long = verificationRepository.countByChannelAndTargetAndCreatedAtAfter(channel, target, after)
}
