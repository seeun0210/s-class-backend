package com.sclass.backoffice.student.usecase

import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.adaptor.StudentDocumentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.domain.StudentDocumentType
import com.sclass.domain.domains.student.exception.StudentDocumentNotFoundException
import com.sclass.domain.domains.student.exception.StudentNotFoundException
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

class DeleteStudentDocumentUseCaseTest {
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var studentDocumentAdaptor: StudentDocumentAdaptor
    private lateinit var fileAdaptor: FileAdaptor
    private lateinit var s3Service: S3Service
    private lateinit var useCase: DeleteStudentDocumentUseCase

    private val userId = Ulid.generate()
    private val studentId = Ulid.generate()
    private val documentId = Ulid.generate()
    private val fileId = Ulid.generate()

    private lateinit var student: Student
    private lateinit var file: File

    @BeforeEach
    fun setUp() {
        studentAdaptor = mockk()
        studentDocumentAdaptor = mockk()
        fileAdaptor = mockk()
        s3Service = mockk()
        useCase = DeleteStudentDocumentUseCase(studentAdaptor, studentDocumentAdaptor, fileAdaptor, s3Service)

        student = Student(id = studentId, user = mockk<User>(relaxed = true))
        file =
            File.create(
                id = fileId,
                originalFilename = "transcript.pdf",
                storedFilename = "backoffice/document/2026/03/${fileId}_transcript.pdf",
                mimeType = "application/pdf",
                fileSize = 1024L,
                fileType = FileType.PLAN,
                uploadedBy = "admin-1",
            )
    }

    private fun createDocument(): StudentDocument =
        StudentDocument(
            id = documentId,
            student = student,
            file = file,
            documentType = StudentDocumentType.TRANSCRIPT,
        )

    @Test
    fun `학생 문서를 ��상 삭제한다`() {
        val document = createDocument()
        every { studentAdaptor.findByUserId(userId) } returns student
        every { studentDocumentAdaptor.findById(documentId) } returns document
        every { studentDocumentAdaptor.delete(document) } just runs
        every { fileAdaptor.delete(fileId) } just runs
        every { s3Service.deleteObject(file.storedFilename) } just runs

        useCase.execute(userId, documentId)

        verifyOrder {
            studentDocumentAdaptor.delete(document)
            fileAdaptor.delete(fileId)
            s3Service.deleteObject(file.storedFilename)
        }
    }

    @Test
    fun `다�� 학생의 문서를 삭제하면 예외 ��생`() {
        val otherStudent = Student(id = Ulid.generate(), user = mockk<User>(relaxed = true))
        val document =
            StudentDocument(
                id = documentId,
                student = otherStudent,
                file = file,
                documentType = StudentDocumentType.TRANSCRIPT,
            )

        every { studentAdaptor.findByUserId(userId) } returns student
        every { studentDocumentAdaptor.findById(documentId) } returns document

        assertThrows<IllegalArgumentException> {
            useCase.execute(userId, documentId)
        }

        verify(exactly = 0) { studentDocumentAdaptor.delete(any()) }
        verify(exactly = 0) { s3Service.deleteObject(any()) }
    }

    @Test
    fun `존재하지 않는 학생이면 StudentNotFoundException 발생`() {
        every { studentAdaptor.findByUserId(userId) } throws StudentNotFoundException()

        assertThrows<StudentNotFoundException> {
            useCase.execute(userId, documentId)
        }
    }

    @Test
    fun `존재하지 않는 문서면 StudentDocumentNotFoundException 발생`() {
        every { studentAdaptor.findByUserId(userId) } returns student
        every { studentDocumentAdaptor.findById(documentId) } throws StudentDocumentNotFoundException()

        assertThrows<StudentDocumentNotFoundException> {
            useCase.execute(userId, documentId)
        }
    }
}
