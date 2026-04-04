package com.sclass.infrastructure.email

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnMissingBean(EmailSender::class)
class NoOpEmailSender : EmailSender {
    override fun sendVerificationCode(
        email: String,
        code: String,
        serviceName: String,
    ) = Unit
}
