package com.sclass.backoffice.teacher.usecase

import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.domain.TeacherDocumentType
import com.sclass.domain.domains.teacher.exception.TeacherDocumentNotFoundException
import com.sclass.domain.domains.teacher.exception.TeacherNotFoundException
import com.sclass.domain.domains.user.domain.User
import com.sclass.infrastructure.s3.S3Service
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteTeacherDocumentUseCaseTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var teacherDocumentAdaptor: TeacherDocumentAdaptor
    private lateinit var fileAdaptor: FileAdaptor
    private lateinit var s3Service: S3Service
    private lateinit var useCase: DeleteTeacherDocumentUseCase

    private val userId = Ulid.generate()
    private val teacherId = Ulid.generate()
    private val documentId = Ulid.generate()
    private val fileId = Ulid.generate()

    private lateinit var teacher: Teacher
    private lateinit var file: File

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        teacherDocumentAdaptor = mockk()
        fileAdaptor = mockk()
        s3Service = mockk()
        useCase = DeleteTeacherDocumentUseCase(teacherAdaptor, teacherDocumentAdaptor, fileAdaptor, s3Service)

        teacher = Teacher(id = teacherId, user = mockk<User>(relaxed = true))
        file =
            File.create(
                id = fileId,
                originalFilename = "certificate.pdf",
                storedFilename = "backoffice/document/2026/03/${fileId}_certificate.pdf",
                mimeType = "application/pdf",
                fileSize = 2048L,
                fileType = FileType.PLAN,
                uploadedBy = "admin-1",
            )
    }

    private fun createDocument(): TeacherDocument =
        TeacherDocument(
            id = documentId,
            teacher = teacher,
            file = file,
            documentType = TeacherDocumentType.COMPLETION_CERTIFICATE,
        )

    @Test
    fun `선생님 문서를 정상 삭제한다`() {
        val document = createDocument()
        every { teacherAdaptor.findByUserId(userId) } returns teacher
        every { teacherDocumentAdaptor.findById(documentId) } returns document
        every { teacherDocumentAdaptor.delete(document) } just runs
        every { fileAdaptor.delete(fileId) } just runs
        every { s3Service.deleteObject(file.storedFilename) } just runs

        useCase.execute(userId, documentId)

        verifyOrder {
            teacherDocumentAdaptor.delete(document)
            fileAdaptor.delete(fileId)
            s3Service.deleteObject(file.storedFilename)
        }
    }

    @Test
    fun `다른 선생님의 문서를 삭제하��� 예외 발생`() {
        val otherTeacher = Teacher(id = Ulid.generate(), user = mockk<User>(relaxed = true))
        val document =
            TeacherDocument(
                id = documentId,
                teacher = otherTeacher,
                file = file,
                documentType = TeacherDocumentType.COMPLETION_CERTIFICATE,
            )

        every { teacherAdaptor.findByUserId(userId) } returns teacher
        every { teacherDocumentAdaptor.findById(documentId) } returns document

        assertThrows<IllegalArgumentException> {
            useCase.execute(userId, documentId)
        }

        verify(exactly = 0) { teacherDocumentAdaptor.delete(any()) }
        verify(exactly = 0) { s3Service.deleteObject(any()) }
    }

    @Test
    fun `존재하지 않는 선생님이면 TeacherNotFoundException 발생`() {
        every { teacherAdaptor.findByUserId(userId) } throws TeacherNotFoundException()

        assertThrows<TeacherNotFoundException> {
            useCase.execute(userId, documentId)
        }
    }

    @Test
    fun `존재하지 않는 문서면 TeacherDocumentNotFoundException 발생`() {
        every { teacherAdaptor.findByUserId(userId) } returns teacher
        every { teacherDocumentAdaptor.findById(documentId) } throws TeacherDocumentNotFoundException()

        assertThrows<TeacherDocumentNotFoundException> {
            useCase.execute(userId, documentId)
        }
    }
}
