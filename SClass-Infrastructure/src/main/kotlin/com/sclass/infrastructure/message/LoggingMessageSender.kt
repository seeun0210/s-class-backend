package com.sclass.infrastructure.message

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local", "test")
class LoggingMessageSender : MessageSender {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendVerificationCode(
        phoneNumber: String,
        code: String,
    ) {
        log.info("[SMS 인증] phoneNumber = $phoneNumber, code = $code")
    }
}
