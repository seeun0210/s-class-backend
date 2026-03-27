package com.sclass.supporters.auth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.service.UserDomainService
import com.sclass.supporters.auth.dto.LoginRequest
import com.sclass.supporters.auth.dto.TokenResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class LoginUseCase(
    private val userService: UserDomainService,
    private val tokenService: TokenDomainService,
) {
    @Transactional
    fun execute(request: LoginRequest): TokenResponse {
        val user =
            userService.authenticate(
                request.email,
                request.password,
                Platform.SUPPORTERS,
                request.role,
            )

        userService.activateIfApproved(user.id, Platform.SUPPORTERS, request.role)

        val tokens = tokenService.issueTokens(user.id, request.role, Platform.SUPPORTERS)
        return TokenResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }
}
