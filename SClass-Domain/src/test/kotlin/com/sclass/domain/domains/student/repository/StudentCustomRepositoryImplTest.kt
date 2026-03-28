package com.sclass.domain.domains.student.repository

import com.sclass.domain.config.DomainTestConfig
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.domain.StudentDocumentType
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(DomainTestConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = ["com.sclass.domain"])
class StudentCustomRepositoryImplTest {
    @Autowired
    private lateinit var studentRepository: StudentRepository

    @Autowired
    private lateinit var em: EntityManager

    private lateinit var user: User
    private lateinit var student: Student

    @BeforeEach
    fun setUp() {
        user = User(email = "student@test.com", name = "김학생", authProvider = AuthProvider.EMAIL)
        em.persist(user)

        student = Student(user = user)
        em.persist(student)

        em.flush()
        em.clear()
    }

    private fun createFile(fileType: FileType): File {
        val file =
            File.create(
                originalFilename = "test.pdf",
                storedFilename = "stored.pdf",
                mimeType = "application/pdf",
                fileSize = 1024,
                fileType = fileType,
                uploadedBy = user.id,
            )
        em.persist(file)
        return file
    }

    private fun createDocument(
        documentType: StudentDocumentType,
        fileType: FileType,
    ): StudentDocument {
        val file = createFile(fileType)
        val document = StudentDocument(student = student, file = file, documentType = documentType)
        em.persist(document)
        return document
    }

    @Nested
    inner class FindAcademicDocumentsWithFileByUserIds {
        @Test
        fun `REGISTRATION_RECEIPT를 제외한 문서만 반환한다`() {
            createDocument(StudentDocumentType.TRANSCRIPT, FileType.STUDENT_TRANSCRIPT)
            createDocument(StudentDocumentType.APPLICATION, FileType.STUDENT_APPLICATION)
            createDocument(StudentDocumentType.REGISTRATION_RECEIPT, FileType.STUDENT_REGISTRATION_RECEIPT)
            em.flush()
            em.clear()

            val result = studentRepository.findAcademicDocumentsWithFileByUserIds(listOf(user.id))

            val documents = result[user.id] ?: emptyList()
            assertAll(
                { assertEquals(2, documents.size) },
                {
                    assertTrue(
                        documents.none { it.documentType == StudentDocumentType.REGISTRATION_RECEIPT },
                    )
                },
            )
        }

        @Test
        fun `등록영수증만 있는 학생은 빈 리스트로 반환된다`() {
            createDocument(StudentDocumentType.REGISTRATION_RECEIPT, FileType.STUDENT_REGISTRATION_RECEIPT)
            em.flush()
            em.clear()

            val result = studentRepository.findAcademicDocumentsWithFileByUserIds(listOf(user.id))

            val documents = result[user.id] ?: emptyList()
            assertTrue(documents.isEmpty())
        }

        @Test
        fun `빈 userIds를 전달하면 빈 맵을 반환한다`() {
            val result = studentRepository.findAcademicDocumentsWithFileByUserIds(emptyList())

            assertTrue(result.isEmpty())
        }
    }
}
