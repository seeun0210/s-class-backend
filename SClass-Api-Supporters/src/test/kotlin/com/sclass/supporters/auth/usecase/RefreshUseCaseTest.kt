package com.sclass.supporters.auth.usecase

import com.sclass.domain.domains.token.dto.ResolvedRefreshToken
import com.sclass.domain.domains.token.dto.TokenResult
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.supporters.auth.dto.RefreshRequest
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
            ResolvedRefreshToken(userId = "user-id", tokenId = "refresh-token-id", role = Role.STUDENT)
        every { userAdaptor.findById("user-id") } returns mockk<User>()
        every { tokenDomainService.issueTokens("user-id", Role.STUDENT) } returns
            TokenResult(accessToken = "new-access", refreshToken = "new-refresh")

        val result = refreshUseCase.execute(RefreshRequest(refreshToken = "encrypted-refresh"))

        assertEquals("new-access", result.accessToken)
        assertEquals("new-refresh", result.refreshToken)
        verify { tokenDomainService.consumeRefreshToken("encrypted-refresh") }
        verify { tokenDomainService.issueTokens("user-id", Role.STUDENT) }
    }
}
