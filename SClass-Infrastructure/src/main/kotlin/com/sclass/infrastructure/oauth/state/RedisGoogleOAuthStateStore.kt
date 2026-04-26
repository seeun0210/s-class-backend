package com.sclass.infrastructure.oauth.state

import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Duration
import java.util.Base64

@Component
class RedisGoogleOAuthStateStore(
    private val redissonClient: RedissonClient,
) : GoogleOAuthStateStore {
    private val secureRandom = SecureRandom()

    override fun issue(
        userId: String,
        ttl: Duration,
    ): String {
        val state = generateState()
        redissonClient
            .getBucket<String>(key(userId, state))
            .set("1", ttl)
        return state
    }

    override fun consume(
        userId: String,
        state: String,
    ): Boolean {
        val bucket = redissonClient.getBucket<String>(key(userId, state))
        return bucket.getAndDelete() != null
    }

    private fun key(
        userId: String,
        state: String,
    ): String = "oauth:google:state:${encodeUserId(userId)}:$state"

    private fun encodeUserId(userId: String): String =
        Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(userId.toByteArray(StandardCharsets.UTF_8))

    private fun generateState(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
