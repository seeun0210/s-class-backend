package com.sclass.infrastructure.oauth.client

import com.sclass.common.exception.GoogleOAuthProviderUnavailableException
import com.sclass.common.exception.GoogleTokenExchangeFailedException
import com.sclass.common.exception.GoogleTokenRefreshFailedException
import com.sclass.infrastructure.oauth.config.OAuthProperties
import com.sclass.infrastructure.oauth.config.ProviderConfig
import com.sclass.infrastructure.oauth.dto.GoogleTokenExchangeResponse
import com.sclass.infrastructure.oauth.dto.GoogleUserInfoResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

class GoogleAuthorizationCodeClientTest {
    private lateinit var webClient: WebClient
    private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec
    private lateinit var requestBodySpec: WebClient.RequestBodySpec
    private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>
    private lateinit var requestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*>
    private lateinit var responseSpec: WebClient.ResponseSpec
    private lateinit var client: GoogleAuthorizationCodeClient

    private val properties =
        OAuthProperties().apply {
            providers["google"] =
                ProviderConfig(
                    clientId = "test-client-id",
                    clientSecret = "test-client-secret",
                )
        }

    @BeforeEach
    fun setUp() {
        webClient = mockk()
        requestBodyUriSpec = mockk()
        requestBodySpec = mockk()
        requestHeadersSpec = mockk()
        requestHeadersUriSpec = mockk()
        responseSpec = mockk()
        client = GoogleAuthorizationCodeClient(properties, GoogleOAuthTokenClient(webClient))
    }

    private fun mockPostFormData() {
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.contentType(any()) } returns requestBodySpec
        every { requestBodySpec.body(any<BodyInserter<*, ClientHttpRequest>>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
    }

    private fun mockGet() {
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.header(any(), any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
    }

    @Nested
    inner class ExchangeCodeForTokens {
        @Test
        fun `성공 시 GoogleTokenExchangeResponse 반환`() {
            mockPostFormData()
            every {
                responseSpec.bodyToMono(GoogleTokenExchangeResponse::class.java)
            } returns
                Mono.just(
                    GoogleTokenExchangeResponse(
                        accessToken = "access-token-abc",
                        expiresIn = 3600,
                        refreshToken = "refresh-token-xyz",
                        scope = "calendar.events",
                        tokenType = "Bearer",
                    ),
                )

            val result = client.exchangeCodeForTokens("code-1", "http://localhost:3000/callback")

            assertEquals("access-token-abc", result.accessToken)
            assertEquals("refresh-token-xyz", result.refreshToken)
        }

        @Test
        fun `Google 4xx 응답 시 GoogleTokenExchangeFailedException`() {
            mockPostFormData()
            every {
                responseSpec.bodyToMono(GoogleTokenExchangeResponse::class.java)
            } returns
                Mono.error(
                    WebClientResponseException.create(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        HttpHeaders.EMPTY,
                        """{"error":"invalid_grant"}""".toByteArray(),
                        null,
                    ),
                )

            assertThrows<GoogleTokenExchangeFailedException> {
                client.exchangeCodeForTokens("code-bad", "http://localhost:3000/callback")
            }
        }

        @Test
        fun `Google 5xx 응답 시 GoogleOAuthProviderUnavailableException`() {
            mockPostFormData()
            every {
                responseSpec.bodyToMono(GoogleTokenExchangeResponse::class.java)
            } returns
                Mono.error(
                    WebClientResponseException.create(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Service Unavailable",
                        HttpHeaders.EMPTY,
                        """{"error":"temporarily_unavailable"}""".toByteArray(),
                        null,
                    ),
                )

            assertThrows<GoogleOAuthProviderUnavailableException> {
                client.exchangeCodeForTokens("code-1", "http://localhost:3000/callback")
            }
        }

        @Test
        fun `네트워크 예외 시 GoogleOAuthProviderUnavailableException`() {
            mockPostFormData()
            every {
                responseSpec.bodyToMono(GoogleTokenExchangeResponse::class.java)
            } returns Mono.error(RuntimeException("network down"))

            assertThrows<GoogleOAuthProviderUnavailableException> {
                client.exchangeCodeForTokens("code-1", "http://localhost:3000/callback")
            }
        }
    }

    @Nested
    inner class RefreshAccessToken {
        @Test
        fun `성공 시 새 access_token 반환`() {
            mockPostFormData()
            every {
                responseSpec.bodyToMono(GoogleTokenExchangeResponse::class.java)
            } returns
                Mono.just(
                    GoogleTokenExchangeResponse(
                        accessToken = "new-access-token",
                        expiresIn = 3600,
                        refreshToken = null,
                        scope = "calendar.events",
                        tokenType = "Bearer",
                    ),
                )

            val result = client.refreshAccessToken("refresh-token-xyz")

            assertEquals("new-access-token", result)
        }

        @Test
        fun `Google 4xx 응답 시 GoogleTokenRefreshFailedException`() {
            mockPostFormData()
            every {
                responseSpec.bodyToMono(GoogleTokenExchangeResponse::class.java)
            } returns
                Mono.error(
                    WebClientResponseException.create(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        HttpHeaders.EMPTY,
                        """{"error":"invalid_grant"}""".toByteArray(),
                        null,
                    ),
                )

            assertThrows<GoogleTokenRefreshFailedException> {
                client.refreshAccessToken("expired-or-revoked-refresh-token")
            }
        }

        @Test
        fun `Google 5xx 응답 시 GoogleOAuthProviderUnavailableException`() {
            mockPostFormData()
            every {
                responseSpec.bodyToMono(GoogleTokenExchangeResponse::class.java)
            } returns
                Mono.error(
                    WebClientResponseException.create(
                        HttpStatus.BAD_GATEWAY.value(),
                        "Bad Gateway",
                        HttpHeaders.EMPTY,
                        """{"error":"bad_gateway"}""".toByteArray(),
                        null,
                    ),
                )

            assertThrows<GoogleOAuthProviderUnavailableException> {
                client.refreshAccessToken("refresh-token-xyz")
            }
        }

        @Test
        fun `네트워크 예외 시 GoogleOAuthProviderUnavailableException`() {
            mockPostFormData()
            every {
                responseSpec.bodyToMono(GoogleTokenExchangeResponse::class.java)
            } returns Mono.error(RuntimeException("network down"))

            assertThrows<GoogleOAuthProviderUnavailableException> {
                client.refreshAccessToken("refresh-token-xyz")
            }
        }
    }

    @Nested
    inner class FetchUserInfo {
        @Test
        fun `성공 시 GoogleUserInfoResponse 반환`() {
            mockGet()
            every {
                responseSpec.bodyToMono(GoogleUserInfoResponse::class.java)
            } returns
                Mono.just(
                    GoogleUserInfoResponse(
                        id = "google-user-id",
                        email = "teacher@gmail.com",
                        verifiedEmail = true,
                        name = "Teacher",
                    ),
                )

            val result = client.fetchUserInfo("access-token-abc")

            assertEquals("teacher@gmail.com", result.email)
            assertEquals(true, result.verifiedEmail)
        }

        @Test
        fun `Google 4xx 응답 시 GoogleTokenExchangeFailedException`() {
            mockGet()
            every {
                responseSpec.bodyToMono(GoogleUserInfoResponse::class.java)
            } returns
                Mono.error(
                    WebClientResponseException.create(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        HttpHeaders.EMPTY,
                        """{"error":"invalid_token"}""".toByteArray(),
                        null,
                    ),
                )

            assertThrows<GoogleTokenExchangeFailedException> {
                client.fetchUserInfo("invalid-access-token")
            }
        }

        @Test
        fun `응답 바디가 없으면 GoogleOAuthProviderUnavailableException`() {
            mockGet()
            every {
                responseSpec.bodyToMono(GoogleUserInfoResponse::class.java)
            } returns Mono.empty()

            assertThrows<GoogleOAuthProviderUnavailableException> {
                client.fetchUserInfo("access-token-abc")
            }
        }
    }

    @Nested
    inner class RevokeRefreshToken {
        @Test
        fun `refresh token은 form body로 revoke 요청한다`() {
            mockPostFormData()
            every { responseSpec.toBodilessEntity() } returns Mono.empty()

            client.revokeRefreshToken("refresh-token-with-special?chars&")

            verify { requestBodyUriSpec.uri("https://oauth2.googleapis.com/revoke") }
            verify { requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED) }
            verify { requestBodySpec.body(any<BodyInserter<*, ClientHttpRequest>>()) }
        }

        @Test
        fun `revoke 실패는 전파하지 않는다`() {
            mockPostFormData()
            every { responseSpec.toBodilessEntity() } throws RuntimeException("network down")

            client.revokeRefreshToken("refresh-token-xyz")
        }
    }
}
