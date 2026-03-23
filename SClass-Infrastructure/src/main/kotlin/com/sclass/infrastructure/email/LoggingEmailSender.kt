package com.sclass.infrastructure.email

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnMissingBean(SmtpEmailSender::class)
class LoggingEmailSender : EmailSender {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendVerificationCode(
        email: String,
        code: String,
        serviceName: String,
    ) {
        log.info("[이메일 인증] [{}] {} → {}", serviceName, email, code)
    }
}
