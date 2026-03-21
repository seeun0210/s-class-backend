package com.sclass.domain.domains.token.repository

import com.sclass.domain.domains.token.domain.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, String> {
    fun findAllByUserId(userId: String): List<RefreshToken>

    fun deleteAllByUserId(userId: String)
}
