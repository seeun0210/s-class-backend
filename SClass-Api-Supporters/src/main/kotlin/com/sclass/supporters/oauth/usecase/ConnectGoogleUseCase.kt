package com.sclass.supporters.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.ForbiddenException
import com.sclass.common.exception.GoogleIdentityScopeMissingException
import com.sclass.common.exception.GoogleRefreshTokenMissingException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.adaptor.TeacherGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.TeacherGoogleAccount
import com.sclass.domain.domains.user.domain.Role
import com.sclass.infrastructure.oauth.client.GoogleAuthorizationCodeClient
import com.sclass.supporters.oauth.dto.ConnectGoogleRequest
import com.sclass.supporters.oauth.dto.GoogleConnectionStatusResponse
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@UseCase
class ConnectGoogleUseCase(
    private val googleClient: GoogleAuthorizationCodeClient,
    private val accountAdaptor: TeacherGoogleAccountAdaptor,
    private val encryptor: AesTokenEncryptor,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    @Transactional
    fun execute(
        userId: String,
        role: Role,
        request: ConnectGoogleRequest,
    ): GoogleConnectionStatusResponse {
        if (role != Role.TEACHER) throw ForbiddenException()

        val tokens = googleClient.exchangeCodeForTokens(request.code, request.redirectUri)
        val refreshToken = tokens.refreshToken ?: throw GoogleRefreshTokenMissingException()
        if (!tokens.scope.hasGoogleIdentityScope()) throw GoogleIdentityScopeMissingException()

        val userInfo = googleClient.fetchUserInfo(tokens.accessToken)
        val encryptedRefreshToken = encryptor.encrypt(refreshToken)

        val existing = accountAdaptor.findByUserIdOrNull(userId)
        val account =
            if (existing != null) {
                existing.reconnect(userInfo.email, encryptedRefreshToken, tokens.scope)
                accountAdaptor.save(existing)
            } else {
                accountAdaptor.save(
                    TeacherGoogleAccount(
                        userId = userId,
                        googleEmail = userInfo.email,
                        encryptedRefreshToken = encryptedRefreshToken,
                        scope = tokens.scope,
                        connectedAt = LocalDateTime.now(clock),
                    ),
                )
            }

        return GoogleConnectionStatusResponse.connected(account)
    }

    private fun String.hasGoogleIdentityScope(): Boolean =
        split(" ")
            .any { it in GOOGLE_IDENTITY_SCOPES }

    companion object {
        private val GOOGLE_IDENTITY_SCOPES =
            setOf(
                "openid",
                "email",
                "https://www.googleapis.com/auth/userinfo.email",
            )
    }
}
