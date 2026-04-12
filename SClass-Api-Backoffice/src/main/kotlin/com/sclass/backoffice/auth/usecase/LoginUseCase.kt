package com.sclass.backoffice.auth.usecase

import com.sclass.backoffice.auth.dto.LoginRequest
import com.sclass.backoffice.auth.dto.TokenResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.service.UserDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class LoginUseCase(
    private val userDomainService: UserDomainService,
    private val tokenDomainService: TokenDomainService,
) {
    @Transactional
    fun execute(request: LoginRequest): TokenResponse {
        val user =
            userDomainService.authenticate(
                email = request.email,
                rawPassword = request.password,
                role = Role.ADMIN,
            )

        val tokens = tokenDomainService.issueTokens(user.id, Role.ADMIN)
        return TokenResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }
}
