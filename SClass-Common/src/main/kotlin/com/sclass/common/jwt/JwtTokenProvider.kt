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
        private const val ROLES = "roles"
        private const val TOKEN_ISSUER = "sclass"
        private const val MILLI_TO_SECOND = 1000L
    }

    private fun getSecretKey(): SecretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8))

    private fun getJws(token: String): Jws<Claims> =
        try {
            Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
        } catch (e: ExpiredJwtException) {
            throw TokenExpiredException.EXCEPTION
        } catch (e: Exception) {
            throw InvalidTokenException.EXCEPTION
        }

    fun generateAccessToken(
        userId: String,
        roles: List<String>,
    ): String {
        val issuedAt = Date()
        val expiration = Date(issuedAt.time + jwtProperties.accessExp * MILLI_TO_SECOND)
        return Jwts
            .builder()
            .issuer(TOKEN_ISSUER)
            .issuedAt(issuedAt)
            .subject(userId)
            .claim(TOKEN_TYPE, ACCESS_TOKEN)
            .claim(ROLES, roles)
            .expiration(expiration)
            .signWith(getSecretKey())
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
            .signWith(getSecretKey())
            .compact()
    }

    fun parseAccessToken(token: String): AccessTokenInfo {
        val claims = getJws(token).payload
        if (claims.get(TOKEN_TYPE) != ACCESS_TOKEN) {
            throw InvalidTokenException.EXCEPTION
        }
        @Suppress("UNCHECKED_CAST")
        val roles = claims.get(ROLES, List::class.java) as List<String>
        return AccessTokenInfo(
            userId = claims.subject,
            roles = roles,
        )
    }

    fun parseRefreshToken(token: String): String {
        val claims =
            try {
                getJws(token).payload
            } catch (e: TokenExpiredException) {
                throw RefreshTokenExpiredException.EXCEPTION
            }
        if (claims.get(TOKEN_TYPE) != REFRESH_TOKEN) {
            throw InvalidTokenException.EXCEPTION
        }
        return claims.subject
    }

    fun getRefreshTokenTtlSecond(): Long = jwtProperties.refreshExp

    fun getAccessTokenTtlSecond(): Long = jwtProperties.accessExp
}
