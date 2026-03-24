package com.sclass.supporters.file.usecase

import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.file.repository.FileRepository
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.domain.StudentDocumentType
import com.sclass.domain.domains.student.repository.StudentDocumentRepository
import com.sclass.domain.domains.student.repository.StudentRepository
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.domain.TeacherDocumentType
import com.sclass.domain.domains.teacher.repository.TeacherDocumentRepository
import com.sclass.domain.domains.teacher.repository.TeacherRepository
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.repository.UserRepository
import com.sclass.supporters.config.ApiIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ApiIntegrationTest
class DeleteFileIntegrationTest {
    @Autowired
    private lateinit var fileRepository: FileRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var teacherRepository: TeacherRepository

    @Autowired
    private lateinit var teacherDocumentRepository: TeacherDocumentRepository

    @Autowired
    private lateinit var studentRepository: StudentRepository

    @Autowired
    private lateinit var studentDocumentRepository: StudentDocumentRepository

    @BeforeEach
    fun setUp() {
        teacherDocumentRepository.deleteAll()
        studentDocumentRepository.deleteAll()
        teacherRepository.deleteAll()
        studentRepository.deleteAll()
        fileRepository.deleteAll()
        userRepository.deleteAll()
    }

    private fun createFile(suffix: String = "report.pdf"): File =
        fileRepository.save(
            File.create(
                originalFilename = suffix,
                storedFilename = "supporters/plan/2026/03/$suffix",
                mimeType = "application/pdf",
                fileSize = 1024L,
                fileType = FileType.PLAN,
                uploadedBy = "user-123",
            ),
        )

    private fun createUser(email: String = "test@example.com"): User =
        userRepository.save(
            User(
                email = email,
                name = "테스트유저",
                authProvider = AuthProvider.EMAIL,
                hashedPassword = "hashed",
            ),
        )

    @Test
    fun `Document가 연결된 파일 삭제 시 TeacherDocument도 CASCADE 삭제된다`() {
        val file = createFile("teacher-doc.pdf")
        val user = createUser("teacher@test.com")
        val teacher = teacherRepository.save(Teacher(user = user))
        teacherDocumentRepository.save(
            TeacherDocument(
                teacher = teacher,
                file = file,
                documentType = TeacherDocumentType.APPLICATION,
            ),
        )

        assertTrue(teacherDocumentRepository.findAllByTeacherId(teacher.id).isNotEmpty())

        fileRepository.deleteById(file.id)
        fileRepository.flush()

        assertFalse(fileRepository.findById(file.id).isPresent)
        assertTrue(teacherDocumentRepository.findAllByTeacherId(teacher.id).isEmpty())
    }

    @Test
    fun `Document가 연결된 파일 삭제 시 StudentDocument도 CASCADE 삭제된다`() {
        val file = createFile("student-doc.pdf")
        val user = createUser("student@test.com")
        val student = studentRepository.save(Student(user = user))
        studentDocumentRepository.save(
            StudentDocument(
                student = student,
                file = file,
                documentType = StudentDocumentType.APPLICATION,
            ),
        )

        assertTrue(studentDocumentRepository.findAllByStudentId(student.id).isNotEmpty())

        fileRepository.deleteById(file.id)
        fileRepository.flush()

        assertFalse(fileRepository.findById(file.id).isPresent)
        assertTrue(studentDocumentRepository.findAllByStudentId(student.id).isEmpty())
    }

    @Test
    fun `Document가 없는 파일도 정상 삭제된다`() {
        val file = createFile("orphan.pdf")

        assertTrue(fileRepository.findById(file.id).isPresent)

        fileRepository.deleteById(file.id)
        fileRepository.flush()

        assertFalse(fileRepository.findById(file.id).isPresent)
    }

    @Test
    fun `파일 삭제 시 다른 파일의 Document는 영향받지 않는다`() {
        val fileToDelete = createFile("delete-me.pdf")
        val fileToKeep = createFile("keep-me.pdf")
        val user = createUser("multi@test.com")
        val teacher = teacherRepository.save(Teacher(user = user))

        teacherDocumentRepository.save(
            TeacherDocument(
                teacher = teacher,
                file = fileToDelete,
                documentType = TeacherDocumentType.APPLICATION,
            ),
        )
        teacherDocumentRepository.save(
            TeacherDocument(
                teacher = teacher,
                file = fileToKeep,
                documentType = TeacherDocumentType.COMPLETION_CERTIFICATE,
            ),
        )

        assertEquals(2, teacherDocumentRepository.findAllByTeacherId(teacher.id).size)

        fileRepository.deleteById(fileToDelete.id)
        fileRepository.flush()

        val remaining = teacherDocumentRepository.findAllByTeacherId(teacher.id)
        assertEquals(1, remaining.size)
        assertEquals(fileToKeep.id, remaining[0].file.id)
    }
}
