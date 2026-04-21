package com.sclass.supporters.lesson.usecase

import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportAdaptor
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportFileAdaptor
import com.sclass.domain.domains.lessonReport.domain.LessonReport
import com.sclass.domain.domains.lessonReport.domain.LessonReportFile
import com.sclass.domain.domains.lessonReport.domain.LessonReportStatus
import com.sclass.domain.domains.lessonReport.exception.LessonReportNotRejectedException
import com.sclass.infrastructure.s3.S3Service
import com.sclass.supporters.lesson.dto.UpdateLessonReportRequest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateLessonReportUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var lessonReportAdaptor: LessonReportAdaptor
    private lateinit var lessonReportFileAdaptor: LessonReportFileAdaptor
    private lateinit var fileAdaptor: FileAdaptor
    private lateinit var s3Service: S3Service
    private lateinit var useCase: UpdateLessonReportUseCase

    private val student = "student-user-id-0000000001"
    private val assignedTeacher = "assigned-teacher-id-0000001"

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        lessonReportAdaptor = mockk()
        lessonReportFileAdaptor = mockk()
        fileAdaptor = mockk()
        s3Service = mockk()
        useCase = UpdateLessonReportUseCase(lessonAdaptor, lessonReportAdaptor, lessonReportFileAdaptor, fileAdaptor, s3Service)
    }

    private fun newLesson() =
        Lesson(
            id = 1L,
            lessonType = LessonType.COURSE,
            studentUserId = student,
            assignedTeacherUserId = assignedTeacher,
            name = "lesson",
            status = LessonStatus.COMPLETED,
        )

    private fun newRejectedReport() =
        LessonReport(
            id = 10L,
            lessonId = 1L,
            submittedByUserId = assignedTeacher,
            content = "old",
            status = LessonReportStatus.REJECTED,
            reviewedByUserId = "reviewer",
            rejectReason = "부족",
        )

    private fun fakeFile(id: String) =
        File(
            id = id,
            originalFilename = "f.pdf",
            storedFilename = "k/$id",
            mimeType = "application/pdf",
            fileSize = 1L,
            fileType = FileType.REPORT,
            uploadedBy = assignedTeacher,
        )

    @Test
    fun `REJECTED 리포트 수정 시 PENDING_REVIEW로 돌아가고 첨부가 교체된다`() {
        val lesson = newLesson()
        val report = newRejectedReport()
        val existingFileIds = listOf("file-id-1", "file-id-2")
        val fileIds = listOf("file-id-2", "file-id-3")
        val existingReportFiles =
            existingFileIds.map { fileId ->
                LessonReportFile(lessonReport = report, file = fakeFile(fileId))
            }

        every { lessonAdaptor.findById(1L) } returns lesson
        every { lessonReportAdaptor.findByLesson(1L) } returns report
        every { lessonReportFileAdaptor.findByLessonReportId(10L) } returns existingReportFiles
        every { lessonReportFileAdaptor.deleteAllByLessonReportId(10L) } just Runs
        every { fileAdaptor.findAllByIds(fileIds) } returns fileIds.map(::fakeFile)
        every { fileAdaptor.delete("file-id-1") } just Runs
        every { s3Service.deleteObject("k/file-id-1") } just Runs
        every { lessonReportFileAdaptor.saveAll(any()) } answers { firstArg<List<LessonReportFile>>() }

        val result = useCase.execute(assignedTeacher, 1L, UpdateLessonReportRequest(content = "new", fileIds = fileIds))

        assertAll(
            { assertEquals(LessonReportStatus.PENDING_REVIEW, report.status) },
            { assertEquals("new", report.content) },
            { assertEquals(null, report.reviewedByUserId) },
            { assertEquals(null, report.rejectReason) },
            { assertEquals(fileIds, result.fileIds) },
        )
        verify { lessonReportFileAdaptor.deleteAllByLessonReportId(10L) }
        verify { lessonReportFileAdaptor.saveAll(match { it.size == 2 }) }
        verify { fileAdaptor.delete("file-id-1") }
        verify { s3Service.deleteObject("k/file-id-1") }
    }

    @Test
    fun `REJECTED가 아닌 리포트는 수정 불가`() {
        val lesson = newLesson()
        val report =
            LessonReport(
                id = 10L,
                lessonId = 1L,
                submittedByUserId = assignedTeacher,
                content = "old",
                status = LessonReportStatus.PENDING_REVIEW,
            )

        every { lessonAdaptor.findById(1L) } returns lesson
        every { lessonReportAdaptor.findByLesson(1L) } returns report
        every { lessonReportFileAdaptor.findByLessonReportId(10L) } returns emptyList()

        assertThrows<LessonReportNotRejectedException> {
            useCase.execute(assignedTeacher, 1L, UpdateLessonReportRequest(content = "new"))
        }
    }

    @Test
    fun `권한 없는 유저는 수정 불가`() {
        every { lessonAdaptor.findById(1L) } returns newLesson()

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute("other-user-id", 1L, UpdateLessonReportRequest(content = "new"))
        }
    }

    @Test
    fun `첨부를 비우면 기존 첨부가 파일 레코드와 S3에서 삭제된다`() {
        val lesson = newLesson()
        val report = newRejectedReport()
        val existingReportFiles =
            listOf(
                LessonReportFile(lessonReport = report, file = fakeFile("file-id-1")),
                LessonReportFile(lessonReport = report, file = fakeFile("file-id-2")),
            )

        every { lessonAdaptor.findById(1L) } returns lesson
        every { lessonReportAdaptor.findByLesson(1L) } returns report
        every { lessonReportFileAdaptor.findByLessonReportId(10L) } returns existingReportFiles
        every { lessonReportFileAdaptor.deleteAllByLessonReportId(10L) } just Runs
        every { fileAdaptor.delete("file-id-1") } just Runs
        every { fileAdaptor.delete("file-id-2") } just Runs
        every { s3Service.deleteObject("k/file-id-1") } just Runs
        every { s3Service.deleteObject("k/file-id-2") } just Runs

        val result = useCase.execute(assignedTeacher, 1L, UpdateLessonReportRequest(content = "new", fileIds = emptyList()))

        assertEquals(emptyList<String>(), result.fileIds)
        verify { fileAdaptor.delete("file-id-1") }
        verify { fileAdaptor.delete("file-id-2") }
        verify { s3Service.deleteObject("k/file-id-1") }
        verify { s3Service.deleteObject("k/file-id-2") }
    }
}
