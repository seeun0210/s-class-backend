package com.sclass.infrastructure.email

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["email.smtp.enabled"], havingValue = "false", matchIfMissing = true)
class NoOpEmailSender : EmailSender {
    override fun sendVerificationCode(
        email: String,
        code: String,
        serviceName: String,
    ) = Unit
}
