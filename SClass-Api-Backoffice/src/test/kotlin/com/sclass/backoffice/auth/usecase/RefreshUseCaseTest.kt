package com.sclass.backoffice.auth.usecase

import com.sclass.backoffice.auth.dto.RefreshRequest
import com.sclass.domain.domains.token.dto.ResolvedRefreshToken
import com.sclass.domain.domains.token.dto.TokenResult
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RefreshUseCaseTest {
    private lateinit var userAdaptor: UserAdaptor
    private lateinit var tokenDomainService: TokenDomainService
    private lateinit var refreshUseCase: RefreshUseCase

    @BeforeEach
    fun setUp() {
        userAdaptor = mockk()
        tokenDomainService = mockk()
        refreshUseCase = RefreshUseCase(userAdaptor, tokenDomainService)
    }

    @Test
    fun `유효한 refresh token이면 기존 jti를 폐기하고 새 토큰을 발급한다`() {
        every { tokenDomainService.consumeRefreshToken("encrypted-refresh") } returns
            ResolvedRefreshToken(userId = "admin-id", tokenId = "refresh-token-id", role = Role.ADMIN)
        every { userAdaptor.findById("admin-id") } returns mockk<User>()
        every { tokenDomainService.issueTokens("admin-id", Role.ADMIN) } returns
            TokenResult(accessToken = "new-access", refreshToken = "new-refresh")

        val result = refreshUseCase.execute(RefreshRequest(refreshToken = "encrypted-refresh"))

        assertEquals("new-access", result.accessToken)
        assertEquals("new-refresh", result.refreshToken)
        verify { tokenDomainService.consumeRefreshToken("encrypted-refresh") }
        verify { tokenDomainService.issueTokens("admin-id", Role.ADMIN) }
    }
}
