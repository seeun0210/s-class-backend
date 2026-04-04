package com.sclass.domain.domains.verification.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.verification.adaptor.VerificationAdaptor
import com.sclass.domain.domains.verification.domain.Verification
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DomainService
class VerificationAttemptService(
    private val verificationAdaptor: VerificationAdaptor,
) {
    // 외부 트랜잭션과 같은 엔티티 참조 → REQUIRES_NEW에서 증가 후 저장,
    // 외부 트랜잭션 플러시 시에도 attemptCount가 그대로 유지됨 (Brute-force 방지)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun incrementAttemptCount(verification: Verification) {
        verification.incrementAttemptCount()
        verificationAdaptor.save(verification)
    }
}
