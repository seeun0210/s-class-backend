package com.sclass.backoffice.auth.usecase

import com.sclass.backoffice.auth.dto.LoginRequest
import com.sclass.domain.domains.token.dto.TokenResult
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.service.UserDomainService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class LoginUseCaseTest {
    private lateinit var userDomainService: UserDomainService
    private lateinit var tokenDomainService: TokenDomainService
    private lateinit var loginUseCase: LoginUseCase

    @BeforeEach
    fun setUp() {
        userDomainService = mockk()
        tokenDomainService = mockk()
        loginUseCase = LoginUseCase(userDomainService, tokenDomainService)
    }

    @Test
    fun `백오피스 로그인 시 토큰이 발급된다`() {
        val request = LoginRequest(email = "admin@sclass.com", password = "password123")
        val user = mockk<User>()
        every { user.id } returns "user-id-123"

        every {
            userDomainService.authenticate(
                email = "admin@sclass.com",
                rawPassword = "password123",
                platform = Platform.BACKOFFICE,
                role = Role.ADMIN,
            )
        } returns user

        every {
            tokenDomainService.issueTokens("user-id-123", Role.ADMIN, Platform.BACKOFFICE)
        } returns
            TokenResult(
                accessToken = "encrypted-access-token",
                refreshToken = "encrypted-refresh-token",
            )

        val result = loginUseCase.execute(request)

        assertAll(
            { assertEquals("encrypted-access-token", result.accessToken) },
            { assertEquals("encrypted-refresh-token", result.refreshToken) },
        )
    }
}
