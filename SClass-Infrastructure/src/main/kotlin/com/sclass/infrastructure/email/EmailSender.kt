package com.sclass.infrastructure.email

interface EmailSender {
    fun sendVerificationCode(
        email: String,
        code: String,
        serviceName: String,
    )
}
