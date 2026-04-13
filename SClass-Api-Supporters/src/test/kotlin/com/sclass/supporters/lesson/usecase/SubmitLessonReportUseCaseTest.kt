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
import com.sclass.domain.domains.lessonReport.exception.LessonReportAlreadyReportedException
import com.sclass.supporters.lesson.dto.SubmitLessonReportRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SubmitLessonReportUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var lessonReportAdaptor: LessonReportAdaptor
    private lateinit var lessonReportFileAdaptor: LessonReportFileAdaptor
    private lateinit var fileAdaptor: FileAdaptor
    private lateinit var useCase: SubmitLessonReportUseCase

    private val student = "student-user-id-0000000001"
    private val assignedTeacher = "assigned-teacher-id-0000001"
    private val substitute = "substitute-teacher-id-00001"

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        lessonReportAdaptor = mockk()
        lessonReportFileAdaptor = mockk()
        fileAdaptor = mockk()
        useCase = SubmitLessonReportUseCase(lessonAdaptor, lessonReportAdaptor, lessonReportFileAdaptor, fileAdaptor)
    }

    private fun newLesson(
        status: LessonStatus = LessonStatus.SCHEDULED,
        substituteTeacherUserId: String? = null,
    ) = Lesson(
        id = 1L,
        lessonType = LessonType.COURSE,
        studentUserId = student,
        assignedTeacherUserId = assignedTeacher,
        substituteTeacherUserId = substituteTeacherUserId,
        name = "lesson",
        status = status,
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
    fun `assigned teacher가 제출하면 lesson이 COMPLETED로 바뀌고 report 저장`() {
        val lesson = newLesson()
        every { lessonAdaptor.findById(1L) } returns lesson
        every { lessonReportAdaptor.findLatestByLesson(1L) } returns null
        every { lessonReportAdaptor.nextVersion(1L) } returns 1
        val saved = slot<LessonReport>()
        every { lessonReportAdaptor.save(capture(saved)) } answers { saved.captured }

        val result = useCase.execute(assignedTeacher, 1L, SubmitLessonReportRequest(content = "ok"))

        assertAll(
            { assertEquals(LessonStatus.COMPLETED, lesson.status) },
            { assertEquals(assignedTeacher, lesson.actualTeacherUserId) },
            { assertEquals("ok", result.content) },
            { assertEquals(1, result.version) },
            { assertEquals(assignedTeacher, result.submittedByUserId) },
            { assertEquals(0, result.fileIds.size) },
        )
    }

    @Test
    fun `substitute 선생님도 제출 가능`() {
        val lesson = newLesson(substituteTeacherUserId = substitute)
        every { lessonAdaptor.findById(1L) } returns lesson
        every { lessonReportAdaptor.findLatestByLesson(1L) } returns null
        every { lessonReportAdaptor.nextVersion(1L) } returns 1
        every { lessonReportAdaptor.save(any()) } answers { firstArg() }

        val result = useCase.execute(substitute, 1L, SubmitLessonReportRequest(content = "c"))

        assertAll(
            { assertEquals(LessonStatus.COMPLETED, lesson.status) },
            { assertEquals(substitute, lesson.actualTeacherUserId) },
            { assertEquals(substitute, result.submittedByUserId) },
        )
    }

    @Test
    fun `권한 없는 유저가 제출 시 예외`() {
        val lesson = newLesson()
        every { lessonAdaptor.findById(1L) } returns lesson

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute("other-user-id-0000000000001", 1L, SubmitLessonReportRequest(content = "x"))
        }
    }

    @Test
    fun `이미 PENDING_REVIEW 리포트가 있으면 재제출 불가`() {
        val lesson = newLesson(status = LessonStatus.COMPLETED)
        every { lessonAdaptor.findById(1L) } returns lesson
        every { lessonReportAdaptor.findLatestByLesson(1L) } returns
            LessonReport(
                id = 99L,
                lessonId = 1L,
                version = 1,
                submittedByUserId = assignedTeacher,
                content = "prev",
                status = LessonReportStatus.PENDING_REVIEW,
            )

        assertThrows<LessonReportAlreadyReportedException> {
            useCase.execute(assignedTeacher, 1L, SubmitLessonReportRequest(content = "x"))
        }
    }

    @Test
    fun `REJECTED 리포트가 있으면 재제출 가능 (version 증가)`() {
        val lesson = newLesson(status = LessonStatus.COMPLETED)
        every { lessonAdaptor.findById(1L) } returns lesson
        every { lessonReportAdaptor.findLatestByLesson(1L) } returns
            LessonReport(
                id = 99L,
                lessonId = 1L,
                version = 1,
                submittedByUserId = assignedTeacher,
                content = "prev",
                status = LessonReportStatus.REJECTED,
            )
        every { lessonReportAdaptor.nextVersion(1L) } returns 2
        every { lessonReportAdaptor.save(any()) } answers { firstArg() }

        val result = useCase.execute(assignedTeacher, 1L, SubmitLessonReportRequest(content = "v2"))

        assertEquals(2, result.version)
    }

    @Test
    fun `파일이 있으면 LessonReportFile 저장됨`() {
        val lesson = newLesson()
        every { lessonAdaptor.findById(1L) } returns lesson
        every { lessonReportAdaptor.findLatestByLesson(1L) } returns null
        every { lessonReportAdaptor.nextVersion(1L) } returns 1
        every { lessonReportAdaptor.save(any()) } answers { firstArg() }
        val fileIds = listOf("file-id-000000000000000001", "file-id-000000000000000002")
        every { fileAdaptor.findAllByIds(fileIds) } returns fileIds.map(::fakeFile)
        val slot = slot<List<LessonReportFile>>()
        every { lessonReportFileAdaptor.saveAll(capture(slot)) } answers { slot.captured }

        val result = useCase.execute(assignedTeacher, 1L, SubmitLessonReportRequest(content = "c", fileIds = fileIds))

        assertAll(
            { assertEquals(2, result.fileIds.size) },
            { assertEquals(fileIds.toSet(), result.fileIds.toSet()) },
        )
        verify { lessonReportFileAdaptor.saveAll(match { it.size == 2 }) }
    }

    @Test
    fun `COMPLETED 상태에서 제출 시 complete 다시 호출하지 않음`() {
        val lesson = newLesson(status = LessonStatus.COMPLETED)
        every { lessonAdaptor.findById(1L) } returns lesson
        every { lessonReportAdaptor.findLatestByLesson(1L) } returns null
        every { lessonReportAdaptor.nextVersion(1L) } returns 1
        every { lessonReportAdaptor.save(any()) } answers { firstArg() }

        useCase.execute(assignedTeacher, 1L, SubmitLessonReportRequest(content = "c"))

        assertEquals(LessonStatus.COMPLETED, lesson.status)
    }
}
