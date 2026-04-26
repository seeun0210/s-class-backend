package com.sclass.supporters.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.ForbiddenException
import com.sclass.common.exception.GoogleCalendarScopeMissingException
import com.sclass.common.exception.GoogleIdentityScopeMissingException
import com.sclass.common.exception.GoogleOAuthStateInvalidException
import com.sclass.common.exception.GoogleRefreshTokenMissingException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.user.domain.Role
import com.sclass.infrastructure.oauth.client.GoogleAuthorizationCodeClient
import com.sclass.infrastructure.oauth.state.GoogleOAuthStateStore
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import com.sclass.supporters.oauth.dto.ConnectGoogleRequest
import com.sclass.supporters.oauth.dto.GoogleConnectionStatusResponse

@UseCase
class ConnectGoogleUseCase(
    private val googleClient: GoogleAuthorizationCodeClient,
    private val connectGoogleAccountLockedUseCase: ConnectGoogleAccountLockedUseCase,
    private val encryptor: AesTokenEncryptor,
    private val stateStore: GoogleOAuthStateStore,
) {
    @DistributedLock(prefix = "teacher-google-account", waitTime = 30)
    fun execute(
        @LockKey userId: String,
        role: Role,
        request: ConnectGoogleRequest,
    ): GoogleConnectionStatusResponse {
        if (role != Role.TEACHER) throw ForbiddenException()
        if (!stateStore.consume(userId, request.state)) throw GoogleOAuthStateInvalidException()

        val tokens = googleClient.exchangeCodeForTokens(request.code, request.redirectUri)
        val refreshToken = tokens.refreshToken ?: throw GoogleRefreshTokenMissingException()
        val grantedScopes = tokens.scope.toScopeSet()
        if (!grantedScopes.hasGoogleEmailScope()) throw GoogleIdentityScopeMissingException()
        if (GOOGLE_CALENDAR_EVENTS_SCOPE !in grantedScopes) throw GoogleCalendarScopeMissingException()

        val userInfo = googleClient.fetchUserInfo(tokens.accessToken)
        val encryptedRefreshToken = encryptor.encrypt(refreshToken)

        val account =
            connectGoogleAccountLockedUseCase.execute(
                userId = userId,
                googleEmail = userInfo.email,
                encryptedRefreshToken = encryptedRefreshToken,
                scope = tokens.scope,
            )

        return GoogleConnectionStatusResponse.connected(account)
    }

    private fun String.toScopeSet(): Set<String> =
        split(" ")
            .filter { it.isNotBlank() }
            .toSet()

    private fun Set<String>.hasGoogleEmailScope(): Boolean = any { it in GOOGLE_EMAIL_SCOPES }

    companion object {
        private const val GOOGLE_CALENDAR_EVENTS_SCOPE = "https://www.googleapis.com/auth/calendar.events"
        private val GOOGLE_EMAIL_SCOPES =
            setOf(
                "email",
                "https://www.googleapis.com/auth/userinfo.email",
            )
    }
}
