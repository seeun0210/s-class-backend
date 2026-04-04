package com.sclass.domain.domains.verification.service

import com.sclass.domain.domains.verification.adaptor.VerificationAdaptor
import com.sclass.domain.domains.verification.domain.Verification
import com.sclass.domain.domains.verification.domain.VerificationChannel
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VerificationAttemptServiceTest {
    private val verificationAdaptor = mockk<VerificationAdaptor>()
    private val service = VerificationAttemptService(verificationAdaptor)

    @Test
    fun `attemptCount를 증가시키고 저장한다`() {
        val verification =
            Verification(
                channel = VerificationChannel.PHONE,
                target = "010-1234-5678",
                code = "123456",
                expiresAt = LocalDateTime.now().plusMinutes(5),
                attemptCount = 0,
            )
        val slot = slot<Verification>()
        every { verificationAdaptor.save(capture(slot)) } answers { slot.captured }

        service.incrementAttemptCount(verification)

        assertEquals(1, slot.captured.attemptCount)
        verify { verificationAdaptor.save(verification) }
    }
}
