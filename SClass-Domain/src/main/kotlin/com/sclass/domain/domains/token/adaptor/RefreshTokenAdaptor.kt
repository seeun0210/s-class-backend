package com.sclass.domain.domains.token.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.token.domain.RefreshToken
import com.sclass.domain.domains.token.repository.RefreshTokenRepository
import java.time.Clock
import java.time.LocalDateTime

@Adaptor
class RefreshTokenAdaptor(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun findAllByUserId(userId: String): List<RefreshToken> = refreshTokenRepository.findAllByUserId(userId)

    fun existsValidByTokenIdAndUserId(
        tokenId: String,
        userId: String,
    ): Boolean = refreshTokenRepository.existsByTokenIdAndUserIdAndExpiresAtAfter(tokenId, userId, LocalDateTime.now(clock))

    fun save(refreshToken: RefreshToken): RefreshToken = refreshTokenRepository.save(refreshToken)

    fun deleteAllByUserId(userId: String) = refreshTokenRepository.deleteAllByUserId(userId)

    fun deleteValidByTokenIdAndUserId(
        tokenId: String,
        userId: String,
    ): Long = refreshTokenRepository.deleteValidByTokenIdAndUserId(tokenId, userId, LocalDateTime.now(clock))

    fun deleteById(id: String) = refreshTokenRepository.deleteById(id)
}
