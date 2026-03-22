package com.sclass.supporters.auth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.supporters.auth.dto.RefreshRequest
import com.sclass.supporters.auth.dto.TokenResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class RefreshUseCase(
    private val userAdaptor: UserAdaptor,
    private val tokenDomainService: TokenDomainService,
) {
    @Transactional
    fun execute(
        request: RefreshRequest,
        role: Role,
    ): TokenResponse {
        val userId = tokenDomainService.resolveUserId(request.refreshToken)
        userAdaptor.findById(userId)
        tokenDomainService.revokeAllByUserId(userId)
        val tokens = tokenDomainService.issueTokens(userId, role, Platform.SUPPORTERS)
        return TokenResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }
}
