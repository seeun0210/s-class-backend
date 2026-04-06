package com.sclass.domain.domains.token.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.token.domain.RefreshToken
import com.sclass.domain.domains.token.repository.RefreshTokenRepository
import java.time.LocalDateTime

@Adaptor
class RefreshTokenAdaptor(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun findAllByUserId(userId: String): List<RefreshToken> = refreshTokenRepository.findAllByUserId(userId)

    fun existsValidByUserId(userId: String): Boolean = refreshTokenRepository.existsByUserIdAndExpiresAtAfter(userId, LocalDateTime.now())

    fun save(refreshToken: RefreshToken): RefreshToken = refreshTokenRepository.save(refreshToken)

    fun deleteAllByUserId(userId: String) = refreshTokenRepository.deleteAllByUserId(userId)

    fun deleteById(id: String) = refreshTokenRepository.deleteById(id)
}
