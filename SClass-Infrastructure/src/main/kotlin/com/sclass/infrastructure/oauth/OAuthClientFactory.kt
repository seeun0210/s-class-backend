package com.sclass.infrastructure.oauth

import com.sclass.infrastructure.oauth.client.OAuthClient
import org.springframework.stereotype.Component

@Component
class OAuthClientFactory(
    oAuthClients: List<OAuthClient>,
) {
    private val clientMap: Map<String, OAuthClient> =
        oAuthClients.associateBy { it.provider.uppercase() }

    fun getClient(provider: String): OAuthClient =
        clientMap[provider.uppercase()]
            ?: throw IllegalArgumentException("지원하지 않는 OAuth 프로바이더: $provider")
}
