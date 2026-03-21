package com.sclass.infrastructure.oauth

import com.sclass.infrastructure.oauth.client.OAuthClient
import org.springframework.stereotype.Component

@Component
class OAuthClientFactory(
    private val oAuthClients: List<OAuthClient>,
) {
    fun getClient(provider: String): OAuthClient =
        oAuthClients.firstOrNull {
            it.provider == provider
        } ?: throw IllegalArgumentException("지원하지 않는 OAuth 프로바이더: $provider")
}
