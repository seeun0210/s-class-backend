package com.sclass.supporters.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sclass.domain.domains.token.service.TokenDomainService
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.service.UserDomainService
import com.sclass.supporters.auth.dto.RefreshRequest
import com.sclass.supporters.config.ApiIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@ApiIntegrationTest
class AuthControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userDomainService: UserDomainService

    @Autowired
    private lateinit var tokenDomainService: TokenDomainService

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @Test
    fun `refresh endpoint는 refresh token을 회전하고 이전 refresh token 재사용을 막는다`() {
        val user =
            userDomainService.register(
                user =
                    User(
                        email = "refresh-${UUID.randomUUID()}@example.com",
                        name = "테스트",
                        authProvider = AuthProvider.EMAIL,
                        phoneNumber = "010-1234-5678",
                    ),
                rawPassword = "password123",
                platform = Platform.SUPPORTERS,
                role = Role.STUDENT,
            )
        val tokens = tokenDomainService.issueTokens(user.id, Role.STUDENT)
        val request = RefreshRequest(refreshToken = tokens.refreshToken)

        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty)

        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("TOKEN_004"))
    }
}
