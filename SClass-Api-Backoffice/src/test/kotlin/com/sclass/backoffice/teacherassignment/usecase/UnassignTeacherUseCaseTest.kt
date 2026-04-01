package com.sclass.backoffice.teacherassignment.usecase

import com.sclass.backoffice.teacherassignment.dto.UnassignTeacherRequest
import com.sclass.domain.domains.teacherassignment.exception.TeacherAssignmentNotFoundException
import com.sclass.domain.domains.teacherassignment.service.TeacherAssignmentDomainService
import com.sclass.domain.domains.user.domain.Platform
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UnassignTeacherUseCaseTest {
    private lateinit var teacherAssignmentDomainService: TeacherAssignmentDomainService
    private lateinit var useCase: UnassignTeacherUseCase

    @BeforeEach
    fun setUp() {
        teacherAssignmentDomainService = mockk()
        useCase = UnassignTeacherUseCase(teacherAssignmentDomainService)
    }

    @Nested
    inner class Success {
        @Test
        fun `배정을 해제한다`() {
            val request =
                UnassignTeacherRequest(
                    studentUserId = "student-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                )

            every {
                teacherAssignmentDomainService.unassign(
                    studentUserId = "student-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                )
            } just runs

            useCase.execute(request)

            verify(exactly = 1) {
                teacherAssignmentDomainService.unassign(
                    studentUserId = "student-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                )
            }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `활성 배정이 없으면 예외가 발생한다`() {
            val request =
                UnassignTeacherRequest(
                    studentUserId = "student-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                )

            every {
                teacherAssignmentDomainService.unassign(
                    studentUserId = "student-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                )
            } throws TeacherAssignmentNotFoundException()

            assertThrows<TeacherAssignmentNotFoundException> {
                useCase.execute(request)
            }
        }
    }
}
