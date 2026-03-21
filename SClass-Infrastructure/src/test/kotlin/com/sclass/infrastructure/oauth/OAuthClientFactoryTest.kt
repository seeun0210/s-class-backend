package com.sclass.infrastructure.oauth

import com.sclass.infrastructure.oauth.client.OAuthClient
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OAuthClientFactoryTest {
    private lateinit var googleClient: OAuthClient
    private lateinit var kakaoClient: OAuthClient
    private lateinit var factory: OAuthClientFactory

    @BeforeEach
    fun setUp() {
        googleClient = mockk { every { provider } returns "GOOGLE" }
        kakaoClient = mockk { every { provider } returns "KAKAO" }
        factory = OAuthClientFactory(listOf(googleClient, kakaoClient))
    }

    @Test
    fun `GOOGLE 프로바이더를 요청하면 해당 클라이언트를 반환한다`() {
        val client = factory.getClient("GOOGLE")

        assertEquals(googleClient, client)
    }

    @Test
    fun `소문자 프로바이더도 올바르게 매칭된다`() {
        val client = factory.getClient("google")

        assertEquals(googleClient, client)
    }

    @Test
    fun `지원하지 않는 프로바이더를 요청하면 예외가 발생한다`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                factory.getClient("NAVER")
            }

        assertEquals("지원하지 않는 OAuth 프로바이더: NAVER", exception.message)
    }
}
