package com.sclass.lms.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.service.UserDomainService
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.infrastructure.oauth.OAuthClientFactory
import com.sclass.infrastructure.oauth.client.OAuthClient
import com.sclass.infrastructure.oauth.dto.OAuthUserInfo
import com.sclass.lms.auth.dto.OAuthCompleteSignupRequest
import com.sclass.lms.auth.dto.OAuthLoginRequest
import com.sclass.lms.config.ApiIntegrationTest
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ApiIntegrationTest
class OAuthControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var oAuthClientFactory: OAuthClientFactory

    @Autowired
    private lateinit var oAuthClient: OAuthClient

    @Autowired
    private lateinit var userDomainService: UserDomainService

    @Autowired
    private lateinit var tokenDomainService: TokenDomainService

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @Test
    fun `신규 유저 OAuth 로그인 시 signupToken이 반환된다`() {
        every { oAuthClientFactory.getClient("GOOGLE") } returns oAuthClient
        every { oAuthClient.fetchUserInfo("oauth-access-token") } returns
            OAuthUserInfo(id = "new-google-id", email = "new-mgmt@example.com", name = "신규관리자")

        val request =
            OAuthLoginRequest(
                provider = AuthProvider.GOOGLE,
                accessToken = "oauth-access-token",
                role = Role.ADMIN,
                platform = Platform.LMS,
            )

        mockMvc
            .perform(
                post("/api/v1/oauth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.newUser").value(true))
            .andExpect(jsonPath("$.data.signupToken").isNotEmpty)
            .andExpect(jsonPath("$.data.accessToken").value(org.hamcrest.Matchers.nullValue()))
            .andExpect(jsonPath("$.data.refreshToken").value(org.hamcrest.Matchers.nullValue()))
    }

    @Test
    fun `기존 유저 OAuth 로그인 시 accessToken과 refreshToken이 반환된다`() {
        userDomainService.registerWithOAuth(
            oauthId = "existing-mgmt-google-id",
            authProvider = AuthProvider.GOOGLE,
            email = "existing-mgmt@example.com",
            name = "기존관리자",
            phoneNumber = "01012345678",
            profileImageUrl = null,
            platform = Platform.LMS,
            role = Role.ADMIN,
        )

        every { oAuthClientFactory.getClient("GOOGLE") } returns oAuthClient
        every { oAuthClient.fetchUserInfo("existing-token") } returns
            OAuthUserInfo(id = "existing-mgmt-google-id", email = "existing-mgmt@example.com", name = "기존관리자")

        val request =
            OAuthLoginRequest(
                provider = AuthProvider.GOOGLE,
                accessToken = "existing-token",
                role = Role.ADMIN,
                platform = Platform.LMS,
            )

        mockMvc
            .perform(
                post("/api/v1/oauth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.newUser").value(false))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty)
    }

    @Test
    fun `signupToken으로 회원가입을 완료하면 토큰이 반환된다`() {
        val signupToken =
            tokenDomainService.issueSignupToken(
                oauthId = "signup-mgmt-google-id",
                provider = AuthProvider.GOOGLE,
                email = "signup-mgmt@example.com",
                name = "가입관리자",
                role = Role.ADMIN,
                platform = Platform.LMS,
            )

        val phoneVerificationToken =
            tokenDomainService.issueVerificationToken(
                channel = VerificationChannel.PHONE,
                target = "010-9999-8888",
            )

        val request =
            OAuthCompleteSignupRequest(
                signupToken = signupToken,
                phoneVerificationToken = phoneVerificationToken,
                profileImageUrl = "https://example.com/profile.jpg",
            )

        mockMvc
            .perform(
                post("/api/v1/oauth/complete-signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty)
    }

    @Test
    fun `잘못된 요청 바디로 로그인하면 400이 반환된다`() {
        val invalidBody = """{"provider": "GOOGLE"}"""

        mockMvc
            .perform(
                post("/api/v1/oauth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidBody),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }
}
