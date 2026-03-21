package com.sclass.supporters.auth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.service.UserDomainService
import com.sclass.infrastructure.oauth.OAuthClientFactory
import com.sclass.supporters.auth.dto.OAuthCompleteSignupRequest
import com.sclass.supporters.auth.dto.OAuthLoginRequest
import com.sclass.supporters.auth.dto.OAuthLoginResponse
import com.sclass.supporters.auth.dto.TokenResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class OAuthLoginUseCase(
    private val oAuthClientFactory: OAuthClientFactory,
    private val userService: UserDomainService,
    private val tokenService: TokenDomainService,
) {
    @Transactional
    fun login(request: OAuthLoginRequest): OAuthLoginResponse {
        val client = oAuthClientFactory.getClient(request.provider)
        val userInfo = client.fetchUserInfo(request.accessToken)

        val authProvider = AuthProvider.valueOf(request.provider.uppercase())

        // 1. oauthId로 기존 유저 검색
        val existingUser = userService.findByOAuthOrNull(userInfo.id, authProvider)
        if (existingUser != null) {
            userService.ensureUserRole(existingUser.id, request.platform, request.role)
            val tokens = tokenService.issueTokens(existingUser.id, request.role)
            return OAuthLoginResponse(
                isNewUser = false,
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
            )
        }

        // 2. email로 기존 유저 검색 → OAuth 연결
        val linkedUser =
            userService.linkOAuthAndEnsureRole(
                email = userInfo.email,
                oauthId = userInfo.id,
                platform = request.platform,
                role = request.role,
            )
        if (linkedUser != null) {
            val tokens = tokenService.issueTokens(linkedUser.id, request.role)
            return OAuthLoginResponse(
                isNewUser = false,
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
            )
        }

        // 3. 신규 유저 → signupToken 발급
        val signupToken =
            tokenService.issueSignupToken(
                oauthId = userInfo.id,
                provider = request.provider.uppercase(),
                email = userInfo.email,
                name = userInfo.name,
                role = request.role,
                platform = request.platform,
            )
        return OAuthLoginResponse(
            isNewUser = true,
            signupToken = signupToken,
        )
    }

    @Transactional
    fun completeSignup(request: OAuthCompleteSignupRequest): TokenResponse {
        val signupInfo = tokenService.resolveSignupToken(request.signupToken)

        val user =
            userService.registerWithOAuth(
                oauthId = signupInfo.oauthId,
                authProvider = AuthProvider.valueOf(signupInfo.provider),
                email = signupInfo.email,
                name = signupInfo.name,
                phoneNumber = request.phoneNumber,
                profileImageUrl = null,
                platform = Platform.valueOf(signupInfo.platform),
                role = Role.valueOf(signupInfo.role),
            )

        val tokens = tokenService.issueTokens(user.id, Role.valueOf(signupInfo.role))
        return TokenResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }
}
