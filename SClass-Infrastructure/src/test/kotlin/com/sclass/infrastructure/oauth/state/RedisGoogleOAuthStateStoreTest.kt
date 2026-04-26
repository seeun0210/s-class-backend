package com.sclass.infrastructure.oauth.state

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.redisson.api.RBucket
import org.redisson.api.RedissonClient
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.Base64

class RedisGoogleOAuthStateStoreTest {
    private lateinit var redissonClient: RedissonClient
    private lateinit var bucket: RBucket<String>
    private lateinit var store: RedisGoogleOAuthStateStore

    @BeforeEach
    fun setUp() {
        redissonClient = mockk()
        bucket = mockk(relaxed = true)
        store = RedisGoogleOAuthStateStore(redissonClient)
    }

    @Test
    fun `state를 생성하고 TTL과 함께 Redis에 저장한다`() {
        val keySlot = slot<String>()
        every { redissonClient.getBucket<String>(capture(keySlot)) } returns bucket

        val state = store.issue("teacher-user-id", Duration.ofMinutes(5))

        assertAll(
            { assertTrue(state.isNotBlank()) },
            { assertTrue(keySlot.captured.startsWith("oauth:google:state:${encoded("teacher-user-id")}:")) },
            { assertTrue(keySlot.captured.endsWith(state)) },
        )
        verify { bucket.set("1", Duration.ofMinutes(5)) }
    }

    @Test
    fun `저장된 state를 원자적으로 consume하면 true를 반환한다`() {
        every { redissonClient.getBucket<String>("oauth:google:state:${encoded("user-1")}:state-1") } returns bucket
        every { bucket.getAndDelete() } returns "1"

        val consumed = store.consume("user-1", "state-1")

        assertTrue(consumed)
    }

    @Test
    fun `저장된 state가 없으면 consume은 false를 반환한다`() {
        every { redissonClient.getBucket<String>("oauth:google:state:${encoded("user-1")}:missing-state") } returns bucket
        every { bucket.getAndDelete() } returns null

        val consumed = store.consume("user-1", "missing-state")

        assertFalse(consumed)
    }

    @Test
    fun `userId에 구분자가 포함되어도 인코딩된 Redis key를 사용한다`() {
        every { redissonClient.getBucket<String>("oauth:google:state:${encoded("user:1")}:state-1") } returns bucket
        every { bucket.getAndDelete() } returns "1"

        val consumed = store.consume("user:1", "state-1")

        assertTrue(consumed)
    }

    private fun encoded(value: String): String =
        Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.toByteArray(StandardCharsets.UTF_8))
}
