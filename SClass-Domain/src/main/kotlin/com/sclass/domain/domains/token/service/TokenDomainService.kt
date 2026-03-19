package com.sclass.domain.domains.token.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.token.domain.RefreshToken
import com.sclass.domain.domains.token.repository.RefreshTokenRepository
import org.springframework.transaction.annotation.Transactional

@DomainService
@Transactional(readOnly = true)
class TokenDomainService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Transactional
    fun save(refreshToken: RefreshToken) = refreshTokenRepository.save(refreshToken)

    @Transactional
    fun deleteByUserId(userId: String) = refreshTokenRepository.deleteAllByUserId(userId)

    @Transactional
    fun deleteById(id: String) = refreshTokenRepository.deleteById(id)
}
