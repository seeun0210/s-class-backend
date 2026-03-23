package com.sclass.supporters.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sclass.domain.domains.verification.domain.VerificationChannel
import com.sclass.domain.domains.verification.service.VerificationDomainService
import com.sclass.supporters.auth.dto.SendEmailCodeRequest
import com.sclass.supporters.auth.dto.VerifyEmailCodeRequest
import com.sclass.supporters.config.ApiIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ApiIntegrationTest
class EmailVerificationControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var verificationDomainService: VerificationDomainService

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @Test
    fun `인증코드 발송 요청 시 200을 반환한다`() {
        val request = SendEmailCodeRequest(email = "test@example.com")

        mockMvc
            .perform(
                post("/api/v1/auth/email/send-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.expiresInSeconds").value(300))
    }

    @Test
    fun `email이 비어있으면 400을 반환한다`() {
        val invalidBody = """{"email": ""}"""

        mockMvc
            .perform(
                post("/api/v1/auth/email/send-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidBody),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `email 형식이 올바르지 않으면 400을 반환한다`() {
        val invalidBody = """{"email": "not-an-email"}"""

        mockMvc
            .perform(
                post("/api/v1/auth/email/send-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidBody),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `올바른 인증코드로 인증하면 emailVerificationToken을 반환한다`() {
        val verification =
            verificationDomainService.createVerification(
                channel = VerificationChannel.EMAIL,
                target = "verify@example.com",
            )

        val request = VerifyEmailCodeRequest(email = "verify@example.com", code = verification.code)

        mockMvc
            .perform(
                post("/api/v1/auth/email/verify-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.emailVerificationToken").isNotEmpty)
    }

    @Test
    fun `잘못된 인증코드로 인증하면 에러를 반환한다`() {
        verificationDomainService.createVerification(
            channel = VerificationChannel.EMAIL,
            target = "wrong@example.com",
        )

        val request = VerifyEmailCodeRequest(email = "wrong@example.com", code = "000000")

        mockMvc
            .perform(
                post("/api/v1/auth/email/verify-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `code가 6자리가 아니면 400을 반환한다`() {
        val invalidBody = """{"email": "test@example.com", "code": "123"}"""

        mockMvc
            .perform(
                post("/api/v1/auth/email/verify-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidBody),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }
}
