package com.sclass.supporters.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.ForbiddenException
import com.sclass.common.exception.GoogleIdentityScopeMissingException
import com.sclass.common.exception.GoogleRefreshTokenMissingException
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.user.domain.Role
import com.sclass.infrastructure.oauth.client.GoogleAuthorizationCodeClient
import com.sclass.supporters.oauth.dto.ConnectGoogleRequest
import com.sclass.supporters.oauth.dto.GoogleConnectionStatusResponse

@UseCase
class ConnectGoogleUseCase(
    private val googleClient: GoogleAuthorizationCodeClient,
    private val connectGoogleAccountLockedUseCase: ConnectGoogleAccountLockedUseCase,
    private val encryptor: AesTokenEncryptor,
) {
    fun execute(
        userId: String,
        role: Role,
        request: ConnectGoogleRequest,
    ): GoogleConnectionStatusResponse {
        if (role != Role.TEACHER) throw ForbiddenException()

        val tokens = googleClient.exchangeCodeForTokens(request.code, request.redirectUri)
        val refreshToken = tokens.refreshToken ?: throw GoogleRefreshTokenMissingException()
        if (!tokens.scope.hasGoogleEmailScope()) throw GoogleIdentityScopeMissingException()

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

    private fun String.hasGoogleEmailScope(): Boolean =
        split(" ")
            .any { it in GOOGLE_EMAIL_SCOPES }

    companion object {
        private val GOOGLE_EMAIL_SCOPES =
            setOf(
                "email",
                "https://www.googleapis.com/auth/userinfo.email",
            )
    }
}
