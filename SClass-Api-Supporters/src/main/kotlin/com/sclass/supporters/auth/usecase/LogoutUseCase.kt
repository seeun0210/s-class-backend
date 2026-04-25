package com.sclass.supporters.auth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.supporters.auth.dto.RefreshRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class LogoutUseCase(
    private val tokenService: TokenDomainService,
) {
    @Transactional
    fun execute(request: RefreshRequest) {
        tokenService.revokeRefreshToken(request.refreshToken)
    }
}
