package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.UpdateTeacherStateRequest
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRoleState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateTeacherStateUseCaseTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var teacherDomainService: TeacherDomainService
    private lateinit var useCase: UpdateTeacherStateUseCase

    private val targetUserId = "target-user-id"
    private val userId = "admin-user-id"

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        teacherDomainService = mockk(relaxed = true)
        useCase = UpdateTeacherStateUseCase(teacherAdaptor, teacherDomainService)
    }

    @Nested
    inner class Approve {
        @Test
        fun `APPROVED 요청 시 teacherDomainService의 approve를 호출한다`() {
            val teacher = mockk<Teacher>()
            every { teacherAdaptor.findByUserId(targetUserId) } returns teacher

            val request =
                UpdateTeacherStateRequest(
                    state = UserRoleState.APPROVED,
                    platform = Platform.SUPPORTERS,
                )

            useCase.execute(targetUserId, request, userId)

            verify { teacherDomainService.approve(teacher, Platform.SUPPORTERS, userId) }
        }
    }

    @Nested
    inner class Reject {
        @Test
        fun `REJECTED 요청 시 teacherDomainService의 reject를 호출한다`() {
            val teacher = mockk<Teacher>()
            every { teacherAdaptor.findByUserId(targetUserId) } returns teacher

            val request =
                UpdateTeacherStateRequest(
                    state = UserRoleState.REJECTED,
                    platform = Platform.SUPPORTERS,
                    reason = "서류 미비",
                )

            useCase.execute(targetUserId, request, userId)

            verify { teacherDomainService.reject(teacher, Platform.SUPPORTERS, "서류 미비") }
        }
    }
}
