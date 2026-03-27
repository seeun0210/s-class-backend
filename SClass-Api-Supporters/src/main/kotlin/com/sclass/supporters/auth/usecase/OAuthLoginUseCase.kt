package com.sclass.supporters.auth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.service.StudentDomainService
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
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
    private val teacherDomainService: TeacherDomainService,
    private val studentDomainService: StudentDomainService,
) {
    @Transactional
    fun login(request: OAuthLoginRequest): OAuthLoginResponse {
        val client = oAuthClientFactory.getClient(request.provider.name)
        val userInfo = client.fetchUserInfo(request.accessToken)

        // 1. oauthId로 기존 유저 검색, 없으면 email로 검색 → OAuth 연결
        val user =
            userService.findByOAuthOrNull(userInfo.id, request.provider)?.also {
                userService.ensureUserRole(it.id, request.platform, request.role)
            } ?: userService.linkOAuthAndEnsureRole(
                email = userInfo.email,
                oauthId = userInfo.id,
                platform = request.platform,
                role = request.role,
            )

        if (user != null) {
            userService.activateIfApproved(user.id, request.platform, request.role)
            val tokens = tokenService.issueTokens(user.id, request.role, request.platform)
            return OAuthLoginResponse(
                isNewUser = false,
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
            )
        }

        // 2. 신규 유저 → signupToken 발급
        val signupToken =
            tokenService.issueSignupToken(
                oauthId = userInfo.id,
                provider = request.provider,
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
        val authProvider = AuthProvider.valueOf(signupInfo.provider)
        val platform = Platform.valueOf(signupInfo.platform)
        val role = Role.valueOf(signupInfo.role)

        val phoneInfo = tokenService.resolveVerificationToken(request.phoneVerificationToken)

        val user =
            userService.registerWithOAuth(
                oauthId = signupInfo.oauthId,
                authProvider = authProvider,
                email = signupInfo.email,
                name = signupInfo.name,
                phoneNumber = phoneInfo.target,
                profileImageUrl = request.profileImageUrl,
                platform = platform,
                role = role,
            )

        createRoleProfile(user, role)

        val tokens = tokenService.issueTokens(user.id, role, platform)
        return TokenResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }

    private fun createRoleProfile(
        user: User,
        role: Role,
    ) {
        when (role) {
            Role.TEACHER -> teacherDomainService.register(user)
            Role.STUDENT -> studentDomainService.register(user)
            else -> {}
        }
    }
}
