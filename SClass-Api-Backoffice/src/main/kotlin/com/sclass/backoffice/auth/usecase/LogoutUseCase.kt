package com.sclass.backoffice.auth.usecase

import com.sclass.backoffice.auth.dto.RefreshRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.token.service.TokenDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class LogoutUseCase(
    private val tokenDomainService: TokenDomainService,
) {
    @Transactional
    fun execute(request: RefreshRequest) {
        val refreshToken = tokenDomainService.resolveRefreshToken(request.refreshToken)
        tokenDomainService.revokeAllByUserId(refreshToken.userId)
    }
}
