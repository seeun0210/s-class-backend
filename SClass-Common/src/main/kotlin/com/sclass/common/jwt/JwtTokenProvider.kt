package com.sclass.common.jwt

import com.sclass.common.jwt.exception.InvalidTokenException
import com.sclass.common.jwt.exception.RefreshTokenExpiredException
import com.sclass.common.jwt.exception.TokenExpiredException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    companion object {
        private const val TOKEN_TYPE = "type"
        private const val ACCESS_TOKEN = "ACCESS"
        private const val REFRESH_TOKEN = "REFRESH"
        private const val ROLE = "role"
        private const val TOKEN_ISSUER = "sclass"
        private const val MILLI_TO_SECOND = 1000L

        private const val SIGNUP_TOKEN = "SIGNUP"
        private const val OAUTH_ID = "oauthId"
        private const val PROVIDER = "provider"
        private const val EMAIL = "email"
        private const val NAME = "name"
        private const val PLATFORM = "platform"
        private const val SIGNUP_TOKEN_TTL_SECONDS = 300L
    }

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    private fun getJws(token: String): Jws<Claims> =
        try {
            Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
        } catch (e: ExpiredJwtException) {
            throw TokenExpiredException()
        } catch (e: SecurityException) {
            throw InvalidTokenException()
        } catch (e: io.jsonwebtoken.MalformedJwtException) {
            throw InvalidTokenException()
        } catch (e: io.jsonwebtoken.UnsupportedJwtException) {
            throw InvalidTokenException()
        } catch (e: IllegalArgumentException) {
            throw InvalidTokenException()
        }

    fun generateAccessToken(
        userId: String,
        role: String,
        platform: String,
    ): String {
        val issuedAt = Date()
        val expiration = Date(issuedAt.time + jwtProperties.accessExp * MILLI_TO_SECOND)
        return Jwts
            .builder()
            .issuer(TOKEN_ISSUER)
            .issuedAt(issuedAt)
            .subject(userId)
            .claim(TOKEN_TYPE, ACCESS_TOKEN)
            .claim(ROLE, role)
            .claim(PLATFORM, platform)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(userId: String): String {
        val issuedAt = Date()
        val expiration = Date(issuedAt.time + jwtProperties.refreshExp * MILLI_TO_SECOND)
        return Jwts
            .builder()
            .issuer(TOKEN_ISSUER)
            .issuedAt(issuedAt)
            .subject(userId)
            .claim(TOKEN_TYPE, REFRESH_TOKEN)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun parseAccessToken(token: String): AccessTokenInfo {
        val claims = getJws(token).payload
        if (claims.get(TOKEN_TYPE) != ACCESS_TOKEN) {
            throw InvalidTokenException()
        }
        val role = claims.get(ROLE, String::class.java)
        val platform = claims.get(PLATFORM, String::class.java)
        return AccessTokenInfo(
            userId = claims.subject,
            role = role,
            platform = platform,
        )
    }

    fun parseRefreshToken(token: String): String {
        val claims =
            try {
                getJws(token).payload
            } catch (e: TokenExpiredException) {
                throw RefreshTokenExpiredException()
            }
        if (claims.get(TOKEN_TYPE) != REFRESH_TOKEN) {
            throw InvalidTokenException()
        }
        return claims.subject
    }

    fun getRefreshTokenTtlSecond(): Long = jwtProperties.refreshExp

    fun getAccessTokenTtlSecond(): Long = jwtProperties.accessExp

    fun generateSignupToken(
        oauthId: String,
        provider: String,
        email: String,
        name: String,
        role: String,
        platform: String,
    ): String {
        val issuedAt = Date()
        val expiration = Date(issuedAt.time + SIGNUP_TOKEN_TTL_SECONDS * MILLI_TO_SECOND)
        return Jwts
            .builder()
            .issuer(TOKEN_ISSUER)
            .issuedAt(issuedAt)
            .claim(TOKEN_TYPE, SIGNUP_TOKEN)
            .claim(OAUTH_ID, oauthId)
            .claim(PROVIDER, provider)
            .claim(EMAIL, email)
            .claim(NAME, name)
            .claim(ROLE, role)
            .claim(PLATFORM, platform)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun parseSignupToken(token: String): SignupTokenInfo {
        val claims = getJws(token).payload
        if (claims.get(TOKEN_TYPE) != SIGNUP_TOKEN) {
            throw InvalidTokenException()
        }
        return SignupTokenInfo(
            oauthId = claims.get(OAUTH_ID, String::class.java),
            provider = claims.get(PROVIDER, String::class.java),
            email = claims.get(EMAIL, String::class.java),
            name = claims.get(NAME, String::class.java),
            role = claims.get(ROLE, String::class.java),
            platform = claims.get(PLATFORM, String::class.java),
        )
    }
}
