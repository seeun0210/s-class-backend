package com.sclass.domain.domains.user.service

import com.sclass.common.annotation.DomainService
import org.springframework.security.crypto.password.PasswordEncoder

@DomainService
class PasswordService(
    private val encoder: PasswordEncoder,
) {
    fun hash(rawPassword: String): String? = encoder.encode(rawPassword)

    fun matches(
        rawPassword: String,
        hashedPassword: String,
    ): Boolean = encoder.matches(rawPassword, hashedPassword)
}
