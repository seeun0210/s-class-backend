package com.sclass.backoffice.auth.usecase

import com.sclass.backoffice.auth.dto.RefreshRequest
import com.sclass.backoffice.auth.dto.TokenResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class RefreshUseCase(
    private val userAdaptor: UserAdaptor,
    private val tokenDomainService: TokenDomainService,
) {
    @Transactional
    fun execute(request: RefreshRequest): TokenResponse {
        val refreshToken = tokenDomainService.consumeRefreshToken(request.refreshToken)
        userAdaptor.findById(refreshToken.userId)
        val tokens = tokenDomainService.issueTokens(refreshToken.userId, refreshToken.role)
        return TokenResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }
}
