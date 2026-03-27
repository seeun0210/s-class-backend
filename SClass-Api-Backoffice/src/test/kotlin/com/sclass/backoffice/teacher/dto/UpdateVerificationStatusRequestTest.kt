package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.teacher.domain.TeacherVerificationStatus
import com.sclass.domain.domains.teacher.exception.TeacherInvalidVerificationStatusException
import com.sclass.domain.domains.teacher.exception.TeacherRejectReasonRequiredException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateVerificationStatusRequestTest {
    @Nested
    inner class StatusValidation {
        @Test
        fun `APPROVED 상태로 생성할 수 있다`() {
            val request =
                UpdateVerificationStatusRequest(
                    status = TeacherVerificationStatus.APPROVED,
                )

            assertTrue(request.isApproved)
        }

        @Test
        fun `REJECTED 상태로 생성할 수 있다`() {
            val request =
                UpdateVerificationStatusRequest(
                    status = TeacherVerificationStatus.REJECTED,
                    reason = "서류 미비",
                )

            assertFalse(request.isApproved)
        }

        @Test
        fun `DRAFT 상태는 허용되지 않는다`() {
            assertThrows<TeacherInvalidVerificationStatusException> {
                UpdateVerificationStatusRequest(status = TeacherVerificationStatus.DRAFT)
            }
        }

        @Test
        fun `PENDING 상태는 허용되지 않는다`() {
            assertThrows<TeacherInvalidVerificationStatusException> {
                UpdateVerificationStatusRequest(status = TeacherVerificationStatus.PENDING)
            }
        }
    }

    @Nested
    inner class ReasonValidation {
        @Test
        fun `REJECTED 상태에서 reason이 없으면 예외가 발생한다`() {
            assertThrows<TeacherRejectReasonRequiredException> {
                UpdateVerificationStatusRequest(
                    status = TeacherVerificationStatus.REJECTED,
                    reason = null,
                )
            }
        }

        @Test
        fun `REJECTED 상태에서 reason이 있으면 requireReason으로 접근할 수 있다`() {
            val request =
                UpdateVerificationStatusRequest(
                    status = TeacherVerificationStatus.REJECTED,
                    reason = "서류 미비",
                )

            assertEquals("서류 미비", request.requireReason)
        }
    }
}
