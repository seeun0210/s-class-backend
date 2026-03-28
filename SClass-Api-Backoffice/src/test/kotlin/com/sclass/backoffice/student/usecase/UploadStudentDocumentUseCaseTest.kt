package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.UploadStudentDocumentRequest
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.adaptor.StudentDocumentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.domain.StudentDocumentType
import com.sclass.domain.domains.student.exception.StudentNotFoundException
import com.sclass.domain.domains.user.domain.User
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UploadStudentDocumentUseCaseTest {
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var studentDocumentAdaptor: StudentDocumentAdaptor
    private lateinit var fileAdaptor: FileAdaptor
    private lateinit var useCase: UploadStudentDocumentUseCase

    private val userId = Ulid.generate()
    private val studentId = Ulid.generate()
    private val fileId = Ulid.generate()

    private lateinit var student: Student
    private lateinit var file: File

    @BeforeEach
    fun setUp() {
        studentAdaptor = mockk()
        studentDocumentAdaptor = mockk()
        fileAdaptor = mockk()
        useCase = UploadStudentDocumentUseCase(studentAdaptor, studentDocumentAdaptor, fileAdaptor)

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

    @Test
    fun `학생 문서를 정상 등���한다`() {
        val request = UploadStudentDocumentRequest(fileId = fileId, documentType = StudentDocumentType.TRANSCRIPT)
        val docSlot = slot<StudentDocument>()

        every { studentAdaptor.findByUserId(userId) } returns student
        every { fileAdaptor.findById(fileId) } returns file
        every { studentDocumentAdaptor.findByStudentIdAndDocumentType(studentId, StudentDocumentType.TRANSCRIPT) } returns null
        every { studentDocumentAdaptor.save(capture(docSlot)) } answers { docSlot.captured }

        val result = useCase.execute(userId, request)

        assertEquals(fileId, result.fileId)
        assertEquals(StudentDocumentType.TRANSCRIPT, result.documentType)
    }

    @Test
    fun `같은 타입의 기존 문서가 있으면 교체한다`() {
        val request = UploadStudentDocumentRequest(fileId = fileId, documentType = StudentDocumentType.TRANSCRIPT)
        val existingDoc = mockk<StudentDocument>(relaxed = true)
        val docSlot = slot<StudentDocument>()

        every { studentAdaptor.findByUserId(userId) } returns student
        every { fileAdaptor.findById(fileId) } returns file
        every { studentDocumentAdaptor.findByStudentIdAndDocumentType(studentId, StudentDocumentType.TRANSCRIPT) } returns existingDoc
        every { studentDocumentAdaptor.delete(existingDoc) } just runs
        every { studentDocumentAdaptor.save(capture(docSlot)) } answers { docSlot.captured }

        useCase.execute(userId, request)

        verify { studentDocumentAdaptor.delete(existingDoc) }
        verify { studentDocumentAdaptor.save(any()) }
    }

    @Test
    fun `존재하지 않는 학생이면 StudentNotFoundException 발생`() {
        val request = UploadStudentDocumentRequest(fileId = fileId, documentType = StudentDocumentType.TRANSCRIPT)

        every { studentAdaptor.findByUserId(userId) } throws StudentNotFoundException()

        assertThrows<StudentNotFoundException> {
            useCase.execute(userId, request)
        }
    }
}
