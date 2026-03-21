package com.sclass.domain.domains.token.service

import com.sclass.common.annotation.DomainService
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.domain.domains.token.adaptor.RefreshTokenAdaptor
import com.sclass.domain.domains.token.domain.RefreshToken
import com.sclass.domain.domains.token.dto.TokenResult
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@DomainService
class TokenDomainService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val aesTokenEncryptor: AesTokenEncryptor,
    private val refreshTokenAdaptor: RefreshTokenAdaptor,
) {
    @Transactional
    fun issueTokens(
        userId: String,
        role: String,
    ): TokenResult {
        val accessToken = jwtTokenProvider.generateAccessToken(userId, role)
        val refreshJwt = jwtTokenProvider.generateRefreshToken(userId)

        refreshTokenAdaptor.save(
            RefreshToken(
                userId = userId,
                expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenTtlSecond()),
            ),
        )

        return TokenResult(
            accessToken = aesTokenEncryptor.encrypt(accessToken),
            refreshToken = aesTokenEncryptor.encrypt(refreshJwt),
        )
    }

    @Transactional
    fun revokeAllByUserId(userId: String) {
        refreshTokenAdaptor.deleteAllByUserId(userId)
    }

    fun resolveUserId(encryptedRefreshToken: String): String {
        val refreshJwt = aesTokenEncryptor.decrypt(encryptedRefreshToken)
        return jwtTokenProvider.parseRefreshToken(refreshJwt)
    }
}
