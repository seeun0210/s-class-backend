package com.sclass.backoffice.oauth.usecase

import com.sclass.backoffice.oauth.dto.CentralGoogleConnectionStatusResponse
import com.sclass.backoffice.oauth.dto.ConnectCentralGoogleRequest
import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.GoogleCalendarCentralDisabledException
import com.sclass.common.exception.GoogleCalendarScopeMissingException
import com.sclass.common.exception.GoogleDriveScopeMissingException
import com.sclass.common.exception.GoogleIdentityScopeMissingException
import com.sclass.common.exception.GoogleOAuthStateInvalidException
import com.sclass.common.exception.GoogleRefreshTokenMissingException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.exception.CentralGoogleAccountEmailNotAllowedException
import com.sclass.infrastructure.calendar.GoogleCentralCalendarProperties
import com.sclass.infrastructure.oauth.client.CentralGoogleAuthorizationCodeClient
import com.sclass.infrastructure.oauth.state.GoogleOAuthStateStore
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey

@UseCase
class ConnectCentralGoogleLockedUseCase(
    private val googleClient: CentralGoogleAuthorizationCodeClient,
    private val connectCentralGoogleAccountLockedUseCase: ConnectCentralGoogleAccountLockedUseCase,
    private val encryptor: AesTokenEncryptor,
    private val stateStore: GoogleOAuthStateStore,
    private val properties: GoogleCentralCalendarProperties,
) {
    @DistributedLock(prefix = "central-google-account", waitTime = 30)
    fun execute(
        @LockKey provider: String,
        adminUserId: String,
        request: ConnectCentralGoogleRequest,
    ): CentralGoogleConnectionStatusResponse {
        if (!properties.enabled) throw GoogleCalendarCentralDisabledException()
        if (!stateStore.consume(adminUserId, request.state)) throw GoogleOAuthStateInvalidException()

        val allowedEmail = properties.requireAllowedEmail()
        val tokens = googleClient.exchangeCodeForTokens(request.code, request.redirectUri)
        val refreshToken = tokens.refreshToken ?: throw GoogleRefreshTokenMissingException()
        val grantedScopes = tokens.scope.toScopeSet()
        if (!grantedScopes.hasGoogleEmailScope()) throw GoogleIdentityScopeMissingException()
        if (CentralGoogleOAuthScopes.GOOGLE_CALENDAR_EVENTS_SCOPE !in grantedScopes) throw GoogleCalendarScopeMissingException()
        if (CentralGoogleOAuthScopes.GOOGLE_DRIVE_MEET_READONLY_SCOPE !in grantedScopes) throw GoogleDriveScopeMissingException()

        val userInfo = googleClient.fetchUserInfo(tokens.accessToken)
        if (!userInfo.email.equals(allowedEmail, ignoreCase = true)) {
            throw CentralGoogleAccountEmailNotAllowedException()
        }

        val encryptedRefreshToken = encryptor.encrypt(refreshToken)
        val account =
            connectCentralGoogleAccountLockedUseCase.execute(
                provider = provider,
                googleEmail = userInfo.email,
                encryptedRefreshToken = encryptedRefreshToken,
                scope = tokens.scope,
                adminUserId = adminUserId,
            )

        return CentralGoogleConnectionStatusResponse.connected(
            account = account,
            allowedEmail = allowedEmail,
        )
    }

    private fun String.toScopeSet(): Set<String> =
        split(" ")
            .filter { it.isNotBlank() }
            .toSet()

    private fun Set<String>.hasGoogleEmailScope(): Boolean = any { it in CentralGoogleOAuthScopes.googleEmailScopes }
}
