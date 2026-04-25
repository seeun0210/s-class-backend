package com.sclass.domain.domains.token.repository

import com.sclass.domain.domains.token.domain.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface RefreshTokenRepository :
    JpaRepository<RefreshToken, String>,
    RefreshTokenCustomRepository {
    fun findAllByUserId(userId: String): List<RefreshToken>

    fun existsByTokenIdAndUserIdAndExpiresAtAfter(
        tokenId: String,
        userId: String,
        now: LocalDateTime,
    ): Boolean

    fun deleteAllByUserId(userId: String)
}
