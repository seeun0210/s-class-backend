package com.sclass.domain.domains.token.service

import com.sclass.common.annotation.DomainService
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.common.jwt.SignupTokenInfo
import com.sclass.common.jwt.VerificationTokenInfo
import com.sclass.common.jwt.exception.InvalidTokenException
import com.sclass.common.jwt.exception.RefreshTokenRevokedException
import com.sclass.domain.domains.token.adaptor.RefreshTokenAdaptor
import com.sclass.domain.domains.token.domain.RefreshToken
import com.sclass.domain.domains.token.dto.ResolvedRefreshToken
import com.sclass.domain.domains.token.dto.TokenResult
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.verification.domain.VerificationChannel
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@DomainService
class TokenDomainService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val aesTokenEncryptor: AesTokenEncryptor,
    private val refreshTokenAdaptor: RefreshTokenAdaptor,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    @Transactional
    fun issueTokens(
        userId: String,
        role: Role,
    ): TokenResult {
        val accessToken = jwtTokenProvider.generateAccessToken(userId, role.name)
        val refreshJwt = jwtTokenProvider.generateRefreshToken(userId, role.name)

        refreshTokenAdaptor.save(
            RefreshToken(
                userId = userId,
                tokenId = refreshJwt.tokenId,
                expiresAt = LocalDateTime.now(clock).plusSeconds(jwtTokenProvider.getRefreshTokenTtlSecond()),
            ),
        )

        return TokenResult(
            accessToken = aesTokenEncryptor.encrypt(accessToken),
            refreshToken = aesTokenEncryptor.encrypt(refreshJwt.token),
        )
    }

    @Transactional
    fun revokeAllByUserId(userId: String) {
        refreshTokenAdaptor.deleteAllByUserId(userId)
    }

    @Transactional
    fun consumeRefreshToken(encryptedRefreshToken: String): ResolvedRefreshToken = deleteValidRefreshToken(encryptedRefreshToken)

    @Transactional
    fun revokeRefreshToken(encryptedRefreshToken: String) {
        deleteValidRefreshToken(encryptedRefreshToken)
    }

    private fun deleteValidRefreshToken(encryptedRefreshToken: String): ResolvedRefreshToken {
        val refreshToken = parseEncryptedRefreshToken(encryptedRefreshToken)
        val deletedCount =
            refreshTokenAdaptor.deleteValidByTokenIdAndUserId(
                tokenId = refreshToken.tokenId,
                userId = refreshToken.userId,
            )
        if (deletedCount == 0L) {
            throw RefreshTokenRevokedException()
        }
        return refreshToken
    }

    fun resolveRefreshToken(encryptedRefreshToken: String): ResolvedRefreshToken {
        val refreshToken = parseEncryptedRefreshToken(encryptedRefreshToken)

        if (!refreshTokenAdaptor.existsValidByTokenIdAndUserId(refreshToken.tokenId, refreshToken.userId)) {
            throw RefreshTokenRevokedException()
        }

        return refreshToken
    }

    fun issueSignupToken(
        oauthId: String,
        provider: AuthProvider,
        email: String,
        name: String,
        role: Role,
        platform: Platform,
    ): String {
        val signupJwt = jwtTokenProvider.generateSignupToken(oauthId, provider.name, email, name, role.name, platform.name)
        return aesTokenEncryptor.encrypt(signupJwt)
    }

    fun resolveSignupToken(encryptedSignupToken: String): SignupTokenInfo {
        val signupJwt = aesTokenEncryptor.decrypt(encryptedSignupToken)
        return jwtTokenProvider.parseSignupToken(signupJwt)
    }

    fun issueVerificationToken(
        channel: VerificationChannel,
        target: String,
    ): String {
        val jwt = jwtTokenProvider.generateVerificationToken(channel.name, target)
        return aesTokenEncryptor.encrypt(jwt)
    }

    fun resolveVerificationToken(encryptedToken: String): VerificationTokenInfo {
        val jwt = aesTokenEncryptor.decrypt(encryptedToken)
        return jwtTokenProvider.parseVerificationToken(jwt)
    }

    private fun decryptRefreshToken(encryptedRefreshToken: String): String =
        try {
            aesTokenEncryptor.decrypt(encryptedRefreshToken)
        } catch (e: Exception) {
            throw InvalidTokenException()
        }

    private fun parseEncryptedRefreshToken(encryptedRefreshToken: String): ResolvedRefreshToken {
        val refreshJwt = decryptRefreshToken(encryptedRefreshToken)
        val tokenInfo = jwtTokenProvider.parseRefreshToken(refreshJwt)
        return ResolvedRefreshToken(
            userId = tokenInfo.userId,
            tokenId = tokenInfo.tokenId,
            role = parseRole(tokenInfo.role),
        )
    }

    private fun parseRole(role: String): Role =
        try {
            Role.valueOf(role)
        } catch (e: IllegalArgumentException) {
            throw InvalidTokenException()
        }
}
