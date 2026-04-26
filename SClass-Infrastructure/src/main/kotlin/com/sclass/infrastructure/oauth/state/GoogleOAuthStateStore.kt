package com.sclass.infrastructure.oauth.state

import java.time.Duration

interface GoogleOAuthStateStore {
    fun issue(
        userId: String,
        ttl: Duration,
    ): String

    fun consume(
        userId: String,
        state: String,
    ): Boolean
}
