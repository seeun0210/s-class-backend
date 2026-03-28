package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.UploadTeacherDocumentRequest
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.domain.TeacherDocumentType
import com.sclass.domain.domains.teacher.exception.TeacherNotFoundException
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

class UploadTeacherDocumentUseCaseTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var teacherDocumentAdaptor: TeacherDocumentAdaptor
    private lateinit var fileAdaptor: FileAdaptor
    private lateinit var useCase: UploadTeacherDocumentUseCase

    private val userId = Ulid.generate()
    private val teacherId = Ulid.generate()
    private val fileId = Ulid.generate()

    private lateinit var teacher: Teacher
    private lateinit var file: File

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        teacherDocumentAdaptor = mockk()
        fileAdaptor = mockk()
        useCase = UploadTeacherDocumentUseCase(teacherAdaptor, teacherDocumentAdaptor, fileAdaptor)

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

    @Test
    fun `선생님 문서를 정상 등록한다`() {
        val request =
            UploadTeacherDocumentRequest(fileId = fileId, documentType = TeacherDocumentType.COMPLETION_CERTIFICATE)
        val docSlot = slot<TeacherDocument>()

        every { teacherAdaptor.findByUserId(userId) } returns teacher
        every { fileAdaptor.findById(fileId) } returns file
        every {
            teacherDocumentAdaptor.findByTeacherIdAndDocumentType(teacherId, TeacherDocumentType.COMPLETION_CERTIFICATE)
        } returns null
        every { teacherDocumentAdaptor.save(capture(docSlot)) } answers { docSlot.captured }

        val result = useCase.execute(userId, request)

        assertEquals(fileId, result.fileId)
        assertEquals(TeacherDocumentType.COMPLETION_CERTIFICATE, result.documentType)
    }

    @Test
    fun `같은 타입의 기존 문서가 있으면 교체한다`() {
        val request =
            UploadTeacherDocumentRequest(fileId = fileId, documentType = TeacherDocumentType.COMPLETION_CERTIFICATE)
        val existingDoc = mockk<TeacherDocument>(relaxed = true)
        val docSlot = slot<TeacherDocument>()

        every { teacherAdaptor.findByUserId(userId) } returns teacher
        every { fileAdaptor.findById(fileId) } returns file
        every {
            teacherDocumentAdaptor.findByTeacherIdAndDocumentType(teacherId, TeacherDocumentType.COMPLETION_CERTIFICATE)
        } returns existingDoc
        every { teacherDocumentAdaptor.delete(existingDoc) } just runs
        every { teacherDocumentAdaptor.save(capture(docSlot)) } answers { docSlot.captured }

        useCase.execute(userId, request)

        verify { teacherDocumentAdaptor.delete(existingDoc) }
        verify { teacherDocumentAdaptor.save(any()) }
    }

    @Test
    fun `존재하지 ���는 선생님이면 TeacherNotFoundException 발생`() {
        val request =
            UploadTeacherDocumentRequest(fileId = fileId, documentType = TeacherDocumentType.COMPLETION_CERTIFICATE)

        every { teacherAdaptor.findByUserId(userId) } throws TeacherNotFoundException()

        assertThrows<TeacherNotFoundException> {
            useCase.execute(userId, request)
        }
    }
}
