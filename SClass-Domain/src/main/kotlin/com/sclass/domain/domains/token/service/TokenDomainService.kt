package com.sclass.domain.domains.token.service

import com.sclass.common.annotation.DomainService
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.common.jwt.JwtTokenProvider
import com.sclass.common.jwt.SignupTokenInfo
import com.sclass.common.jwt.VerificationTokenInfo
import com.sclass.domain.domains.token.adaptor.RefreshTokenAdaptor
import com.sclass.domain.domains.token.domain.RefreshToken
import com.sclass.domain.domains.token.dto.TokenResult
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.verification.domain.VerificationChannel
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
        role: Role,
        platform: Platform,
    ): TokenResult {
        val accessToken = jwtTokenProvider.generateAccessToken(userId, role.name, platform.name)
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
}
