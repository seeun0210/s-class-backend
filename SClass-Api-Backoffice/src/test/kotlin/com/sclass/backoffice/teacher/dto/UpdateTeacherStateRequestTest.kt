package com.sclass.backoffice.teacher.dto

import com.sclass.domain.domains.teacher.exception.TeacherInvalidVerificationStatusException
import com.sclass.domain.domains.teacher.exception.TeacherRejectReasonRequiredException
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRoleState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateTeacherStateRequestTest {
    @Nested
    inner class StateValidation {
        @Test
        fun `APPROVED 상태로 생성할 수 있다`() {
            val request =
                UpdateTeacherStateRequest(
                    state = UserRoleState.APPROVED,
                    platform = Platform.SUPPORTERS,
                )

            assertTrue(request.isApproved)
        }

        @Test
        fun `REJECTED 상태로 생성할 수 있다`() {
            val request =
                UpdateTeacherStateRequest(
                    state = UserRoleState.REJECTED,
                    platform = Platform.SUPPORTERS,
                    reason = "서류 미비",
                )

            assertFalse(request.isApproved)
        }

        @Test
        fun `DRAFT 상태는 허용되지 않는다`() {
            assertThrows<TeacherInvalidVerificationStatusException> {
                UpdateTeacherStateRequest(
                    state = UserRoleState.DRAFT,
                    platform = Platform.SUPPORTERS,
                )
            }
        }

        @Test
        fun `PENDING 상태는 허용되지 않는다`() {
            assertThrows<TeacherInvalidVerificationStatusException> {
                UpdateTeacherStateRequest(
                    state = UserRoleState.PENDING,
                    platform = Platform.SUPPORTERS,
                )
            }
        }
    }

    @Nested
    inner class ReasonValidation {
        @Test
        fun `REJECTED 상태에서 reason이 없으면 예외가 발생한다`() {
            assertThrows<TeacherRejectReasonRequiredException> {
                UpdateTeacherStateRequest(
                    state = UserRoleState.REJECTED,
                    platform = Platform.SUPPORTERS,
                    reason = null,
                )
            }
        }

        @Test
        fun `REJECTED 상태에서 reason이 있으면 requireReason으로 접근할 수 있다`() {
            val request =
                UpdateTeacherStateRequest(
                    state = UserRoleState.REJECTED,
                    platform = Platform.SUPPORTERS,
                    reason = "서류 미비",
                )

            assertEquals("서류 미비", request.requireReason)
        }
    }
}
