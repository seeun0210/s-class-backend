package com.sclass.infrastructure.oauth.client

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.sclass.common.exception.OAuthTokenValidationFailedException
import com.sclass.infrastructure.oauth.config.OAuthProperties
import com.sclass.infrastructure.oauth.config.ProviderConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GoogleOAuthClientTest {
    private lateinit var client: GoogleOAuthClient
    private lateinit var verifier: GoogleIdTokenVerifier

    @BeforeEach
    fun setUp() {
        val properties =
            OAuthProperties().apply {
                providers["google"] = ProviderConfig(clientId = "test-client-id")
            }
        client = GoogleOAuthClient(properties)

        verifier = mockk()
        val field = GoogleOAuthClient::class.java.getDeclaredField("verifier")
        field.isAccessible = true
        field.set(client, verifier)
    }

    @Test
    fun `provider가 GOOGLE이다`() {
        assertEquals("GOOGLE", client.provider)
    }

    @Test
    fun `유효한 ID 토큰이면 OAuthUserInfo를 반환한다`() {
        val payload =
            GoogleIdToken.Payload().apply {
                subject = "google-user-123"
                email = "user@gmail.com"
                set("name", "홍길동")
            }
        val idToken = mockk<GoogleIdToken> { every { getPayload() } returns payload }
        every { verifier.verify(any<String>()) } returns idToken

        val result = client.fetchUserInfo("valid-id-token")

        assertEquals("google-user-123", result.id)
        assertEquals("user@gmail.com", result.email)
        assertEquals("홍길동", result.name)
    }

    @Test
    fun `name 클레임이 없으면 빈 문자열로 반환한다`() {
        val payload =
            GoogleIdToken.Payload().apply {
                subject = "google-user-123"
                email = "user@gmail.com"
            }
        val idToken = mockk<GoogleIdToken> { every { getPayload() } returns payload }
        every { verifier.verify(any<String>()) } returns idToken

        val result = client.fetchUserInfo("valid-id-token")

        assertEquals("", result.name)
    }

    @Test
    fun `verifier가 null을 반환하면 OAuthTokenValidationFailedException이 발생한다`() {
        every { verifier.verify(any<String>()) } returns null

        assertThrows<OAuthTokenValidationFailedException> {
            client.fetchUserInfo("invalid-token")
        }
    }

    @Test
    fun `verifier가 예외를 던지면 OAuthTokenValidationFailedException이 발생한다`() {
        every { verifier.verify(any<String>()) } throws RuntimeException("network error")

        assertThrows<OAuthTokenValidationFailedException> {
            client.fetchUserInfo("bad-token")
        }
    }
}
