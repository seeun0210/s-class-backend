package com.sclass.infrastructure.message

interface MessageSender {
    fun sendVerificationCode(
        phoneNumber: String,
        code: String,
    )
}
