package com.sclass.infrastructure.message

interface VerificationCodeSender {
    fun sendVerificationCode(
        phoneNumber: String,
        code: String,
    )
}
