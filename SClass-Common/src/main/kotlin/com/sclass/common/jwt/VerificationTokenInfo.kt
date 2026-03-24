package com.sclass.common.jwt

data class VerificationTokenInfo(
    val channel: String,
    val target: String,
)
