package com.sclass.backoffice.auth.usecase

import com.sclass.backoffice.auth.dto.RefreshRequest
import com.sclass.domain.domains.token.service.TokenDomainService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LogoutUseCaseTest {
    private lateinit var tokenDomainService: TokenDomainService
    private lateinit var logoutUseCase: LogoutUseCase

    @BeforeEach
    fun setUp() {
        tokenDomainService = mockk()
        logoutUseCase = LogoutUseCase(tokenDomainService)
    }

    @Test
    fun `유효한 refresh token이면 해당 refresh token만 폐기한다`() {
        every { tokenDomainService.revokeRefreshToken("encrypted-refresh") } just runs

        logoutUseCase.execute(RefreshRequest(refreshToken = "encrypted-refresh"))

        verify { tokenDomainService.revokeRefreshToken("encrypted-refresh") }
    }
}
