package com.sclass.infrastructure.oauth.client

import com.sclass.common.exception.OAuthTokenAudienceMismatchException
import com.sclass.common.exception.OAuthTokenValidationFailedException
import com.sclass.infrastructure.oauth.config.OAuthProperties
import com.sclass.infrastructure.oauth.config.ProviderConfig
import com.sclass.infrastructure.oauth.dto.KakaoAccount
import com.sclass.infrastructure.oauth.dto.KakaoProfile
import com.sclass.infrastructure.oauth.dto.KakaoTokenInfoResponse
import com.sclass.infrastructure.oauth.dto.KakaoUserInfoResponse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

class KakaoOAuthClientTest {
    private lateinit var webClient: WebClient
    private lateinit var requestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*>
    private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>
    private lateinit var responseSpec: WebClient.ResponseSpec
    private lateinit var client: KakaoOAuthClient

    private val appId = 12345L

    @BeforeEach
    fun setUp() {
        webClient = mockk()
        requestHeadersUriSpec = mockk()
        requestHeadersSpec = mockk()
        responseSpec = mockk()

        val properties =
            OAuthProperties().apply {
                providers["kakao"] = ProviderConfig(appId = appId)
            }
        client = KakaoOAuthClient(webClient, properties)

        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.headers(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
    }

    @Test
    fun `provider가 KAKAO이다`() {
        assertEquals("KAKAO", client.provider)
    }

    @Test
    fun `유효한 토큰이면 OAuthUserInfo를 반환한다`() {
        val tokenInfo = KakaoTokenInfoResponse(appId = appId)
        val userInfo =
            KakaoUserInfoResponse(
                id = 9999L,
                kakaoAccount =
                    KakaoAccount(
                        email = "user@kakao.com",
                        profile = KakaoProfile(nickname = "카카오유저"),
                    ),
            )

        every {
            responseSpec.bodyToMono(KakaoTokenInfoResponse::class.java)
        } returns Mono.just(tokenInfo)

        every {
            responseSpec.bodyToMono(KakaoUserInfoResponse::class.java)
        } returns Mono.just(userInfo)

        val result = client.fetchUserInfo("valid-access-token")

        assertEquals("9999", result.id)
        assertEquals("user@kakao.com", result.email)
        assertEquals("카카오유저", result.name)
    }

    @Test
    fun `tokeninfo의 appId가 불일치하면 OAuthTokenAudienceMismatchException이 발생한다`() {
        val tokenInfo = KakaoTokenInfoResponse(appId = 99999L)

        every {
            responseSpec.bodyToMono(KakaoTokenInfoResponse::class.java)
        } returns Mono.just(tokenInfo)

        assertThrows<OAuthTokenAudienceMismatchException> {
            client.fetchUserInfo("valid-access-token")
        }
    }

    @Test
    fun `tokeninfo API가 WebClientResponseException을 던지면 OAuthTokenValidationFailedException이 발생한다`() {
        every {
            responseSpec.bodyToMono(KakaoTokenInfoResponse::class.java)
        } returns
            Mono.error(
                WebClientResponseException.create(401, "Unauthorized", HttpHeaders.EMPTY, ByteArray(0), null),
            )

        assertThrows<OAuthTokenValidationFailedException> {
            client.fetchUserInfo("invalid-token")
        }
    }

    @Test
    fun `userinfo API가 WebClientResponseException을 던지면 IllegalStateException이 발생한다`() {
        val tokenInfo = KakaoTokenInfoResponse(appId = appId)

        every {
            responseSpec.bodyToMono(KakaoTokenInfoResponse::class.java)
        } returns Mono.just(tokenInfo)

        every {
            responseSpec.bodyToMono(KakaoUserInfoResponse::class.java)
        } returns
            Mono.error(
                WebClientResponseException.create(500, "Server Error", HttpHeaders.EMPTY, ByteArray(0), null),
            )

        assertThrows<IllegalStateException> {
            client.fetchUserInfo("valid-access-token")
        }
    }
}
