package com.sclass.backoffice.oauth.usecase

import com.sclass.backoffice.oauth.dto.CentralGoogleConnectionStatusResponse
import com.sclass.backoffice.oauth.dto.ConnectCentralGoogleRequest
import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.GoogleCalendarScopeMissingException
import com.sclass.common.exception.GoogleIdentityScopeMissingException
import com.sclass.common.exception.GoogleOAuthStateInvalidException
import com.sclass.common.exception.GoogleRefreshTokenMissingException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import com.sclass.domain.domains.oauth.exception.CentralGoogleAccountEmailNotAllowedException
import com.sclass.infrastructure.calendar.GoogleCentralCalendarProperties
import com.sclass.infrastructure.oauth.client.GoogleAuthorizationCodeClient
import com.sclass.infrastructure.oauth.state.GoogleOAuthStateStore
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@UseCase
class ConnectCentralGoogleUseCase(
    private val googleClient: GoogleAuthorizationCodeClient,
    private val centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor,
    private val encryptor: AesTokenEncryptor,
    private val stateStore: GoogleOAuthStateStore,
    private val properties: GoogleCentralCalendarProperties,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    @Transactional
    fun execute(
        adminUserId: String,
        request: ConnectCentralGoogleRequest,
    ): CentralGoogleConnectionStatusResponse {
        if (!stateStore.consume(adminUserId, request.state)) throw GoogleOAuthStateInvalidException()

        val allowedEmail = properties.requireAllowedEmail()
        val tokens = googleClient.exchangeCodeForTokens(request.code, request.redirectUri)
        val refreshToken = tokens.refreshToken ?: throw GoogleRefreshTokenMissingException()
        val grantedScopes = tokens.scope.toScopeSet()
        if (!grantedScopes.hasGoogleEmailScope()) throw GoogleIdentityScopeMissingException()
        if (GOOGLE_CALENDAR_EVENTS_SCOPE !in grantedScopes) throw GoogleCalendarScopeMissingException()

        val userInfo = googleClient.fetchUserInfo(tokens.accessToken)
        if (!userInfo.email.equals(allowedEmail, ignoreCase = true)) {
            throw CentralGoogleAccountEmailNotAllowedException()
        }

        val now = LocalDateTime.now(clock)
        val encryptedRefreshToken = encryptor.encrypt(refreshToken)
        val account =
            centralGoogleAccountAdaptor
                .findGoogleOrNull()
                ?.apply {
                    reconnect(
                        newGoogleEmail = userInfo.email,
                        newEncryptedRefreshToken = encryptedRefreshToken,
                        newScope = tokens.scope,
                        adminUserId = adminUserId,
                        at = now,
                    )
                }
                ?: CentralGoogleAccount(
                    googleEmail = userInfo.email,
                    encryptedRefreshToken = encryptedRefreshToken,
                    scope = tokens.scope,
                    connectedByAdminUserId = adminUserId,
                    connectedAt = now,
                )

        return CentralGoogleConnectionStatusResponse.connected(
            account = centralGoogleAccountAdaptor.save(account),
            allowedEmail = allowedEmail,
        )
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
