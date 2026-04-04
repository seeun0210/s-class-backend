package com.sclass.domain.domains.verification.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.verification.adaptor.VerificationAdaptor
import com.sclass.domain.domains.verification.domain.Verification
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.exception.VerificationCodeMismatchException
import com.sclass.domain.domains.verification.exception.VerificationExpiredException
import com.sclass.domain.domains.verification.exception.VerificationMaxAttemptsException
import com.sclass.domain.domains.verification.exception.VerificationNotFoundException
import com.sclass.domain.domains.verification.exception.VerificationSendRateLimitException
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@DomainService
class VerificationDomainService(
    private val verificationAdaptor: VerificationAdaptor,
    private val verificationAttemptService: VerificationAttemptService,
) {
    companion object {
        private const val MAX_SEND_PER_HOURS = 5
        private const val MAX_ATTEMPTS_PER_CODE = 5
    }

    @Transactional
    fun createVerification(
        channel: VerificationChannel,
        target: String,
    ): Verification {
        val recentCount = verificationAdaptor.countRecent(channel, target, after = LocalDateTime.now().minusHours(1))

        if (recentCount >= MAX_SEND_PER_HOURS) {
            throw VerificationSendRateLimitException()
        }

        val verification = Verification.create(channel, target)
        return verificationAdaptor.save(verification)
    }

    @Transactional
    fun verifyCode(
        channel: VerificationChannel,
        target: String,
        code: String,
    ): Verification {
        val verification =
            verificationAdaptor.findLatestOrNull(channel, target)
                ?: throw VerificationNotFoundException()

        if (verification.isExpired()) {
            throw VerificationExpiredException()
        }

        if (verification.attemptCount >= MAX_ATTEMPTS_PER_CODE) {
            throw VerificationMaxAttemptsException()
        }

        // REQUIRES_NEW 트랜잭션으로 즉시 커밋 → 코드 불일치 롤백 시에도 시도 횟수 보존 (Brute-force 방지)
        verificationAttemptService.incrementAttemptCount(verification)

        if (verification.code != code) {
            throw VerificationCodeMismatchException()
        }

        verification.verify()
        return verification
    }
}
