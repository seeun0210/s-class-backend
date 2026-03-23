package com.sclass.domain.domains.verification.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VerificationTest {
    @Nested
    inner class Create {
        @Test
        fun `6자리 숫자 코드를 생성한다`() {
            val verification = Verification.create(VerificationChannel.PHONE, "010-1234-5678")

            assertEquals(6, verification.code.length)
            assertTrue(verification.code.all { it.isDigit() })
        }

        @Test
        fun `초기 상태는 미인증이고 시도횟수 0이다`() {
            val verification = Verification.create(VerificationChannel.PHONE, "010-1234-5678")

            assertFalse(verification.verified)
            assertEquals(0, verification.attemptCount)
        }

        @Test
        fun `만료시간이 현재로부터 5분 후이다`() {
            val before = LocalDateTime.now().plusMinutes(4)
            val verification = Verification.create(VerificationChannel.PHONE, "010-1234-5678")
            val after = LocalDateTime.now().plusMinutes(6)

            assertTrue(verification.expiresAt.isAfter(before))
            assertTrue(verification.expiresAt.isBefore(after))
        }

        @Test
        fun `채널과 대상이 올바르게 설정된다`() {
            val verification = Verification.create(VerificationChannel.PHONE, "010-1234-5678")

            assertEquals(VerificationChannel.PHONE, verification.channel)
            assertEquals("010-1234-5678", verification.target)
        }

        @Test
        fun `ULID 형식의 id가 생성된다`() {
            val verification = Verification.create(VerificationChannel.PHONE, "010-1234-5678")

            assertNotNull(verification.id)
            assertEquals(26, verification.id.length)
        }
    }

    @Nested
    inner class IsExpired {
        @Test
        fun `만료시간 이전이면 false를 반환한다`() {
            val verification =
                Verification(
                    channel = VerificationChannel.PHONE,
                    target = "010-1234-5678",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(5),
                )

            assertFalse(verification.isExpired())
        }

        @Test
        fun `만료시간 이후이면 true를 반환한다`() {
            val verification =
                Verification(
                    channel = VerificationChannel.PHONE,
                    target = "010-1234-5678",
                    code = "123456",
                    expiresAt = LocalDateTime.now().minusMinutes(1),
                )

            assertTrue(verification.isExpired())
        }
    }

    @Nested
    inner class IncrementAttemptCount {
        @Test
        fun `시도 횟수가 1 증가한다`() {
            val verification =
                Verification(
                    channel = VerificationChannel.PHONE,
                    target = "010-1234-5678",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(5),
                )

            verification.incrementAttemptCount()

            assertEquals(1, verification.attemptCount)
        }

        @Test
        fun `여러 번 호출하면 누적된다`() {
            val verification =
                Verification(
                    channel = VerificationChannel.PHONE,
                    target = "010-1234-5678",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(5),
                )

            repeat(3) { verification.incrementAttemptCount() }

            assertEquals(3, verification.attemptCount)
        }
    }

    @Nested
    inner class Verify {
        @Test
        fun `verified가 true로 변경된다`() {
            val verification =
                Verification(
                    channel = VerificationChannel.PHONE,
                    target = "010-1234-5678",
                    code = "123456",
                    expiresAt = LocalDateTime.now().plusMinutes(5),
                )

            assertFalse(verification.verified)

            verification.verify()

            assertTrue(verification.verified)
        }
    }
}
