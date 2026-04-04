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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun incrementAttemptCount(verification: Verification) {
        verification.incrementAttemptCount()
        verificationAdaptor.save(verification)
    }
}
