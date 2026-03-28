package com.sclass.backoffice.teacherassignment.usecase

import com.sclass.backoffice.teacherassignment.dto.AssignTeacherRequest
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.exception.StudentNotFoundException
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.exception.TeacherNotFoundException
import com.sclass.domain.domains.teacherassignment.domain.TeacherAssignment
import com.sclass.domain.domains.teacherassignment.service.TeacherAssignmentDomainService
import com.sclass.domain.domains.user.domain.Platform
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AssignTeacherUseCaseTest {
    private lateinit var teacherAssignmentDomainService: TeacherAssignmentDomainService
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var useCase: AssignTeacherUseCase

    @BeforeEach
    fun setUp() {
        teacherAssignmentDomainService = mockk()
        studentAdaptor = mockk()
        teacherAdaptor = mockk()
        useCase = AssignTeacherUseCase(teacherAssignmentDomainService, studentAdaptor, teacherAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `학생과 선생님을 검증한 뒤 배정한다`() {
            val request =
                AssignTeacherRequest(
                    studentId = "student-1",
                    teacherId = "teacher-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                )

            every { studentAdaptor.findByUserId("student-1") } returns mockk<Student>()
            every { teacherAdaptor.findByUserId("teacher-1") } returns mockk()
            every {
                teacherAssignmentDomainService.assign(
                    studentId = "student-1",
                    teacherId = "teacher-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                    assignedBy = "admin-1",
                )
            } returns mockk<TeacherAssignment>()

            useCase.execute(request, "admin-1")

            verify(exactly = 1) { studentAdaptor.findByUserId("student-1") }
            verify(exactly = 1) { teacherAdaptor.findByUserId("teacher-1") }
            verify(exactly = 1) {
                teacherAssignmentDomainService.assign(
                    studentId = "student-1",
                    teacherId = "teacher-1",
                    platform = Platform.LMS,
                    organizationId = 1L,
                    assignedBy = "admin-1",
                )
            }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `존재하지 않는 학생이면 예외가 발생한다`() {
            val request =
                AssignTeacherRequest(
                    studentId = "invalid",
                    teacherId = "teacher-1",
                    platform = Platform.SUPPORTERS,
                )

            every { studentAdaptor.findByUserId("invalid") } throws StudentNotFoundException()

            assertThrows<StudentNotFoundException> {
                useCase.execute(request, "admin-1")
            }
        }

        @Test
        fun `존재하지 않는 선생님이면 예외가 발생한다`() {
            val request =
                AssignTeacherRequest(
                    studentId = "student-1",
                    teacherId = "invalid",
                    platform = Platform.SUPPORTERS,
                )

            every { studentAdaptor.findByUserId("student-1") } returns mockk<Student>()
            every { teacherAdaptor.findByUserId("invalid") } throws TeacherNotFoundException()

            assertThrows<TeacherNotFoundException> {
                useCase.execute(request, "admin-1")
            }
        }
    }
}
