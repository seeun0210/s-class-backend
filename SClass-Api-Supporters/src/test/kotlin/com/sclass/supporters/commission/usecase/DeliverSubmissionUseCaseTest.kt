package com.sclass.supporters.commission.usecase

import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionFileAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportAdaptor
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportFileAdaptor
import com.sclass.domain.domains.lessonReport.domain.LessonReport
import com.sclass.domain.domains.lessonReport.domain.LessonReportFile
import com.sclass.domain.domains.lessonReport.domain.LessonReportStatus
import com.sclass.domain.domains.lessonReport.exception.LessonReportAlreadyApprovedException
import com.sclass.supporters.commission.dto.DeliverSubmissionRequest
import com.sclass.supporters.commission.scheduler.CommissionReminderScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeliverSubmissionUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var commissionFileAdaptor: CommissionFileAdaptor
    private lateinit var fileAdaptor: FileAdaptor
    private lateinit var commissionReminderScheduler: CommissionReminderScheduler
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var lessonReportAdaptor: LessonReportAdaptor
    private lateinit var lessonReportFileAdaptor: LessonReportFileAdaptor
    private lateinit var useCase: DeliverSubmissionUseCase

    private val commissionId = 1L
    private val lessonId = 100L
    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        commissionFileAdaptor = mockk()
        fileAdaptor = mockk()
        commissionReminderScheduler = mockk(relaxed = true)
        lessonAdaptor = mockk()
        lessonReportAdaptor = mockk()
        lessonReportFileAdaptor = mockk(relaxed = true)
        useCase =
            DeliverSubmissionUseCase(
                commissionAdaptor,
                commissionFileAdaptor,
                fileAdaptor,
                commissionReminderScheduler,
                lessonAdaptor,
                lessonReportAdaptor,
                lessonReportFileAdaptor,
            )
    }

    private fun commission() =
        Commission(
            id = commissionId,
            studentUserId = studentUserId,
            teacherUserId = teacherUserId,
            commissionPolicyId = "policy-id-00000000000001",
            outputFormat = OutputFormat.REPORT,
            activityType = ActivityType.CAREER_EXPLORATION,
            status = CommissionStatus.ACCEPTED,
            guideInfo =
                GuideInfo(
                    subject = "미시경제학",
                    volume = "A4 3매",
                    gradingCriteria = "평가기준",
                    teacherEmphasis = "강조사항",
                ),
            selectedTopicId = 10L,
            acceptedLessonId = lessonId,
        )

    private fun lesson(status: LessonStatus = LessonStatus.SCHEDULED) =
        Lesson(
            id = lessonId,
            lessonType = LessonType.COMMISSION,
            enrollmentId = 20L,
            sourceCommissionId = commissionId,
            studentUserId = studentUserId,
            assignedTeacherUserId = teacherUserId,
            name = "미시경제학",
            status = status,
        )

    private fun file(
        id: String,
        fileType: FileType = FileType.TASK_SUBMISSION,
        uploadedBy: String = teacherUserId,
    ) = File.create(
        id = id,
        originalFilename = "$id.pdf",
        storedFilename = "commissions/$id.pdf",
        mimeType = "application/pdf",
        fileSize = 1024L,
        fileType = fileType,
        uploadedBy = uploadedBy,
    )

    @Test
    fun `최종 전달 파일을 제출하면 수업 리포트를 만들고 기존 제출 파일을 현재 요청 기준으로 치환한다`() {
        val commission = commission()
        val lesson = lesson()
        val originalFile = file("original-file-id-0000000001", FileType.REPORT, studentUserId)
        val oldSubmissionFile = file("old-submit-file-000000001")
        val firstSubmissionFile = file("new-submit-file-000000001")
        val secondSubmissionFile = file("new-submit-file-000000002")
        val originalCommissionFile = CommissionFile(id = 1L, commission = commission, file = originalFile)
        val oldSubmissionCommissionFile = CommissionFile(id = 2L, commission = commission, file = oldSubmissionFile)
        val savedCommissionFiles = slot<List<CommissionFile>>()
        val savedReport = slot<LessonReport>()
        val savedReportFiles = slot<List<LessonReportFile>>()

        every { commissionAdaptor.findById(commissionId) } returns commission
        every { lessonAdaptor.findById(lessonId) } returns lesson
        every { fileAdaptor.findAllByIds(listOf(firstSubmissionFile.id, secondSubmissionFile.id)) } returns
            listOf(secondSubmissionFile, firstSubmissionFile)
        every { lessonAdaptor.save(lesson) } returns lesson
        every { lessonReportAdaptor.findByLessonOrNull(lessonId) } returns null
        every { lessonReportAdaptor.save(capture(savedReport)) } answers { savedReport.captured }
        every { lessonReportFileAdaptor.saveAll(capture(savedReportFiles)) } answers { savedReportFiles.captured }
        every { commissionFileAdaptor.findByCommissionId(commissionId) } returns
            listOf(originalCommissionFile, oldSubmissionCommissionFile)
        every { commissionFileAdaptor.deleteAll(listOf(oldSubmissionCommissionFile)) } returns Unit
        every { commissionFileAdaptor.saveAll(capture(savedCommissionFiles)) } answers { savedCommissionFiles.captured }

        val result =
            useCase.execute(
                teacherUserId,
                commissionId,
                DeliverSubmissionRequest(fileIds = listOf(firstSubmissionFile.id, secondSubmissionFile.id)),
            )

        assertAll(
            { assertEquals(LessonStatus.COMPLETED, lesson.status) },
            { assertEquals(teacherUserId, lesson.actualTeacherUserId) },
            { assertEquals(2, savedCommissionFiles.captured.size) },
            {
                assertEquals(
                    listOf(firstSubmissionFile.id, secondSubmissionFile.id),
                    savedCommissionFiles.captured.map { it.file.id },
                )
            },
            {
                assertEquals(
                    listOf(firstSubmissionFile.id, secondSubmissionFile.id),
                    savedReportFiles.captured.map { it.file.id },
                )
            },
            {
                assertEquals(
                    listOf(originalFile.id, firstSubmissionFile.id, secondSubmissionFile.id),
                    result.commissionFiles.map { it.fileMeta.id },
                )
            },
        )
        verify { commissionFileAdaptor.deleteAll(listOf(oldSubmissionCommissionFile)) }
        verify { commissionReminderScheduler.cancelAllReminders(commissionId) }
    }

    @Test
    fun `반려된 리포트가 있으면 완료된 수업에도 최종 전달 파일을 재제출할 수 있다`() {
        val commission = commission()
        val lesson = lesson(status = LessonStatus.COMPLETED)
        val submissionFile = file("new-submit-file-000000001")
        val report =
            LessonReport(
                id = 200L,
                lessonId = lessonId,
                submittedByUserId = teacherUserId,
                content = "기존 내용",
                status = LessonReportStatus.REJECTED,
                reviewedByUserId = "admin-user-id-000000000001",
                rejectReason = "수정 필요",
            )

        every { commissionAdaptor.findById(commissionId) } returns commission
        every { lessonAdaptor.findById(lessonId) } returns lesson
        every { fileAdaptor.findAllByIds(listOf(submissionFile.id)) } returns listOf(submissionFile)
        every { lessonReportAdaptor.findByLessonOrNull(lessonId) } returns report
        every { lessonReportAdaptor.save(report) } returns report
        every { commissionFileAdaptor.findByCommissionId(commissionId) } returns emptyList()
        every { commissionFileAdaptor.saveAll(any()) } answers { firstArg() }

        val result = useCase.execute(teacherUserId, commissionId, DeliverSubmissionRequest(fileIds = listOf(submissionFile.id)))

        assertAll(
            { assertEquals(LessonStatus.COMPLETED, lesson.status) },
            { assertEquals(LessonReportStatus.PENDING_REVIEW, report.status) },
            { assertEquals(null, report.reviewedByUserId) },
            { assertEquals(null, report.rejectReason) },
            { assertEquals(listOf(submissionFile.id), result.commissionFiles.map { it.fileMeta.id }) },
        )
        verify(exactly = 0) { lessonAdaptor.save(any()) }
    }

    @Test
    fun `승인된 리포트가 있으면 최종 전달 파일을 다시 제출할 수 없다`() {
        val commission = commission()
        val lesson = lesson(status = LessonStatus.COMPLETED)
        val submissionFile = file("new-submit-file-000000001")
        val report =
            LessonReport(
                id = 200L,
                lessonId = lessonId,
                submittedByUserId = teacherUserId,
                content = "승인된 내용",
                status = LessonReportStatus.APPROVED,
            )

        every { commissionAdaptor.findById(commissionId) } returns commission
        every { lessonAdaptor.findById(lessonId) } returns lesson
        every { fileAdaptor.findAllByIds(listOf(submissionFile.id)) } returns listOf(submissionFile)
        every { lessonReportAdaptor.findByLessonOrNull(lessonId) } returns report

        assertThrows<LessonReportAlreadyApprovedException> {
            useCase.execute(teacherUserId, commissionId, DeliverSubmissionRequest(fileIds = listOf(submissionFile.id)))
        }
        verify(exactly = 0) { commissionFileAdaptor.findByCommissionId(any()) }
        verify(exactly = 0) { commissionFileAdaptor.saveAll(any()) }
    }

    @Test
    fun `중복 파일 ID로 제출하면 파일 조회 전에 예외가 발생한다`() {
        val commission = commission()
        val lesson = lesson()
        val fileId = "new-submit-file-000000001"

        every { commissionAdaptor.findById(commissionId) } returns commission
        every { lessonAdaptor.findById(lessonId) } returns lesson

        assertThrows<BusinessException> {
            useCase.execute(teacherUserId, commissionId, DeliverSubmissionRequest(fileIds = listOf(fileId, fileId)))
        }
        verify(exactly = 0) { fileAdaptor.findAllByIds(any()) }
    }
}
