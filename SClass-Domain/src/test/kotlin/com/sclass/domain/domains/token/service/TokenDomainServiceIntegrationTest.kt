package com.sclass.domain.domains.token.service

import com.sclass.domain.config.DomainTestConfig
import com.sclass.domain.domains.token.repository.RefreshTokenRepository
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(DomainTestConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = ["com.sclass.domain"])
class TokenDomainServiceIntegrationTest {
    @Autowired
    private lateinit var tokenDomainService: TokenDomainService

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Test
    fun `issueTokens로 토큰을 발급하면 RefreshToken이 DB에 저장된다`() {
        // given
        val userId = "test-user-id-0000000000001"

        // when
        val result = tokenDomainService.issueTokens(userId, Role.STUDENT, Platform.SUPPORTERS)

        // then
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()

        val refreshTokens = refreshTokenRepository.findAllByUserId(userId)
        assertThat(refreshTokens).hasSize(1)
        assertThat(refreshTokens[0].userId).isEqualTo(userId)
    }

    @Test
    fun `issueSignupToken으로 발급한 토큰을 resolveSignupToken으로 복원할 수 있다`() {
        // given
        val encryptedToken =
            tokenDomainService.issueSignupToken(
                oauthId = "oauth-signup-123",
                provider = AuthProvider.GOOGLE,
                email = "signup@example.com",
                name = "가입유저",
                role = Role.STUDENT,
                platform = Platform.SUPPORTERS,
            )

        // when
        val info = tokenDomainService.resolveSignupToken(encryptedToken)

        // then
        assertThat(info.oauthId).isEqualTo("oauth-signup-123")
        assertThat(info.provider).isEqualTo("GOOGLE")
        assertThat(info.email).isEqualTo("signup@example.com")
        assertThat(info.name).isEqualTo("가입유저")
        assertThat(info.role).isEqualTo("STUDENT")
        assertThat(info.platform).isEqualTo("SUPPORTERS")
    }

    @Test
    fun `revokeAllByUserId로 해당 유저의 모든 RefreshToken이 삭제된다`() {
        // given
        val userId = "test-user-id-0000000000002"
        tokenDomainService.issueTokens(userId, Role.STUDENT, Platform.SUPPORTERS)
        tokenDomainService.issueTokens(userId, Role.STUDENT, Platform.SUPPORTERS)
        assertThat(refreshTokenRepository.findAllByUserId(userId)).hasSize(2)

        // when
        tokenDomainService.revokeAllByUserId(userId)

        // then
        assertThat(refreshTokenRepository.findAllByUserId(userId)).isEmpty()
    }
}
