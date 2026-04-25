package com.sclass.backoffice.auth.usecase

import com.sclass.backoffice.auth.dto.RefreshRequest
import com.sclass.domain.domains.token.dto.ResolvedRefreshToken
import com.sclass.domain.domains.token.dto.TokenResult
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.service.UserDomainService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RefreshUseCaseTest {
    private lateinit var userDomainService: UserDomainService
    private lateinit var tokenDomainService: TokenDomainService
    private lateinit var refreshUseCase: RefreshUseCase

    @BeforeEach
    fun setUp() {
        userDomainService = mockk()
        tokenDomainService = mockk()
        refreshUseCase = RefreshUseCase(userDomainService, tokenDomainService)
    }

    @Test
    fun `유효한 refresh token이면 기존 jti를 폐기하고 새 토큰을 발급한다`() {
        every { tokenDomainService.consumeRefreshToken("encrypted-refresh") } returns
            ResolvedRefreshToken(userId = "admin-id", tokenId = "refresh-token-id", role = Role.ADMIN)
        every { userDomainService.validateUserHasRole("admin-id", Role.ADMIN) } just runs
        every { tokenDomainService.issueTokens("admin-id", Role.ADMIN) } returns
            TokenResult(accessToken = "new-access", refreshToken = "new-refresh")

        val result = refreshUseCase.execute(RefreshRequest(refreshToken = "encrypted-refresh"))

        assertEquals("new-access", result.accessToken)
        assertEquals("new-refresh", result.refreshToken)
        verify { tokenDomainService.consumeRefreshToken("encrypted-refresh") }
        verify { userDomainService.validateUserHasRole("admin-id", Role.ADMIN) }
        verify { tokenDomainService.issueTokens("admin-id", Role.ADMIN) }
    }
}
