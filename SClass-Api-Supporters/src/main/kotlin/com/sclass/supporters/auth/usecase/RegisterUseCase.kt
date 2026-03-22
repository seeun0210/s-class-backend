package com.sclass.supporters.auth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.service.UserDomainService
import com.sclass.supporters.auth.dto.RegisterRequest
import com.sclass.supporters.auth.dto.TokenResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class RegisterUseCase(
    private val userService: UserDomainService,
    private val tokenService: TokenDomainService,
) {
    @Transactional
    fun execute(request: RegisterRequest): TokenResponse {
        val user =
            userService.register(
                user =
                    User(
                        email = request.email,
                        name = request.name,
                        authProvider = AuthProvider.EMAIL,
                        phoneNumber = request.phoneNumber,
                    ),
                rawPassword = request.password,
                platform = Platform.SUPPORTERS,
                role = request.role,
            )

        val tokens = tokenService.issueTokens(user.id, request.role, Platform.SUPPORTERS)
        return TokenResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }
}
