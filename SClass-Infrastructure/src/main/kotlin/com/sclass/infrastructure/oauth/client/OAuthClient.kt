package com.sclass.infrastructure.oauth.client

import com.sclass.infrastructure.oauth.dto.OAuthUserInfo

interface OAuthClient {
    val provider: String

    fun fetchUserInfo(accessToken: String): OAuthUserInfo
}
