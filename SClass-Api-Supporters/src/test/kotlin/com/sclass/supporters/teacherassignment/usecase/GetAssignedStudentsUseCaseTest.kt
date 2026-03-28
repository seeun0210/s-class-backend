package com.sclass.supporters.teacherassignment.usecase

import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.exception.TeacherNotFoundException
import com.sclass.domain.domains.teacherassignment.adaptor.TeacherAssignmentAdaptor
import com.sclass.domain.domains.teacherassignment.dto.AssignedStudentInfo
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class GetAssignedStudentsUseCaseTest {
    private lateinit var teacherAssignmentAdaptor: TeacherAssignmentAdaptor
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var useCase: GetAssignedStudentsUseCase

    @BeforeEach
    fun setUp() {
        teacherAssignmentAdaptor = mockk()
        studentAdaptor = mockk()
        teacherAdaptor = mockk()
        useCase = GetAssignedStudentsUseCase(teacherAssignmentAdaptor, studentAdaptor, teacherAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `담당학생 목록과 문서를 함께 조회한다`() {
            val teacherUserId = "teacher-1"
            val studentUserId = "student-1"
            val assignedAt = LocalDateTime.of(2026, 3, 1, 10, 0)

            val assignedStudents =
                listOf(
                    AssignedStudentInfo(
                        assignmentId = 1L,
                        studentUserId = studentUserId,
                        studentName = "김학생",
                        grade = Grade.HIGH_1,
                        school = "테스트고",
                        platform = Platform.SUPPORTERS,
                        organizationId = null,
                        organizationName = null,
                        assignedAt = assignedAt,
                    ),
                )

            val document = mockk<StudentDocument>(relaxed = true)

            every { teacherAdaptor.findByUserId(teacherUserId) } returns mockk<Teacher>()
            every {
                teacherAssignmentAdaptor.findActiveAssignedStudentsByTeacherUserId(teacherUserId, Platform.SUPPORTERS)
            } returns assignedStudents
            every {
                studentAdaptor.findAcademicDocumentsWithFileByUserIds(listOf(studentUserId))
            } returns mapOf(studentUserId to listOf(document))

            val result = useCase.execute(teacherUserId)

            assertEquals(1, result.size)
            val studentResponse = result[0]
            assertAll(
                { assertEquals(1L, studentResponse.assignmentId) },
                { assertEquals(studentUserId, studentResponse.studentUserId) },
                { assertEquals("김학생", studentResponse.studentName) },
                { assertEquals(Grade.HIGH_1, studentResponse.grade) },
                { assertEquals("테스트고", studentResponse.school) },
                { assertEquals(Platform.SUPPORTERS, studentResponse.platform) },
                { assertEquals(null, studentResponse.organizationId) },
                { assertEquals(null, studentResponse.organizationName) },
                { assertEquals(assignedAt, studentResponse.assignedAt) },
                { assertEquals(1, studentResponse.documents.size) },
            )
        }

        @Test
        fun `담당학생이 없으면 빈 리스트를 반환한다`() {
            val teacherUserId = "teacher-1"

            every { teacherAdaptor.findByUserId(teacherUserId) } returns mockk<Teacher>()
            every {
                teacherAssignmentAdaptor.findActiveAssignedStudentsByTeacherUserId(teacherUserId, Platform.SUPPORTERS)
            } returns emptyList()

            val result = useCase.execute(teacherUserId)

            assertEquals(0, result.size)
            verify(exactly = 0) { studentAdaptor.findAcademicDocumentsWithFileByUserIds(any()) }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `선생님이 아니면 예외가 발생한다`() {
            every { teacherAdaptor.findByUserId("not-teacher") } throws TeacherNotFoundException()

            assertThrows<TeacherNotFoundException> {
                useCase.execute("not-teacher")
            }
        }
    }
}
