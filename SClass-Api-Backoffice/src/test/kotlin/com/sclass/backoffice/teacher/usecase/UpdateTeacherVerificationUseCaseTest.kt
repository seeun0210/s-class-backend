package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.UpdateVerificationStatusRequest
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherVerificationStatus
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateTeacherVerificationUseCaseTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var teacherDomainService: TeacherDomainService
    private lateinit var useCase: UpdateTeacherVerificationUseCase

    private val teacherId = "teacher-id"
    private val userId = "admin-user-id"

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        teacherDomainService = mockk(relaxed = true)
        useCase = UpdateTeacherVerificationUseCase(teacherAdaptor, teacherDomainService)
    }

    @Nested
    inner class Approve {
        @Test
        fun `APPROVED 요청 시 teacherDomainService의 approve를 호출한다`() {
            val teacher = mockk<Teacher>()
            every { teacherAdaptor.findById(teacherId) } returns teacher

            val request =
                UpdateVerificationStatusRequest(
                    status = TeacherVerificationStatus.APPROVED,
                )

            useCase.execute(teacherId, request, userId)

            verify { teacherDomainService.approve(teacher, userId) }
        }
    }

    @Nested
    inner class Reject {
        @Test
        fun `REJECTED 요청 시 teacherDomainService의 reject를 호출한다`() {
            val teacher = mockk<Teacher>()
            every { teacherAdaptor.findById(teacherId) } returns teacher

            val request =
                UpdateVerificationStatusRequest(
                    status = TeacherVerificationStatus.REJECTED,
                    reason = "서류 미비",
                )

            useCase.execute(teacherId, request, userId)

            verify { teacherDomainService.reject(teacher, "서류 미비") }
        }
    }
}
