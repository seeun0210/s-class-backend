package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.common.exception.GlobalErrorCode
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionFileAdaptor
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportAdaptor
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportFileAdaptor
import com.sclass.domain.domains.lessonReport.domain.LessonReport
import com.sclass.domain.domains.lessonReport.domain.LessonReportFile
import com.sclass.domain.domains.lessonReport.domain.LessonReportStatus
import com.sclass.domain.domains.lessonReport.exception.LessonReportAlreadyApprovedException
import com.sclass.supporters.commission.dto.CommissionResponse
import com.sclass.supporters.commission.dto.DeliverSubmissionRequest
import com.sclass.supporters.commission.scheduler.CommissionReminderScheduler
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeliverSubmissionUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionFileAdaptor: CommissionFileAdaptor,
    private val fileAdaptor: FileAdaptor,
    private val commissionReminderScheduler: CommissionReminderScheduler,
    private val lessonAdaptor: LessonAdaptor,
    private val lessonReportAdaptor: LessonReportAdaptor,
    private val lessonReportFileAdaptor: LessonReportFileAdaptor,
) {
    @Transactional
    fun execute(
        teacherUserId: String,
        commissionId: Long,
        request: DeliverSubmissionRequest,
    ): CommissionResponse {
        val commission = commissionAdaptor.findById(commissionId)
        if (commission.teacherUserId != teacherUserId) {
            throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
        }
        val lesson = resolveDeliverableLesson(teacherUserId, commission.acceptedLessonId)
        val files = findDeliverableFiles(teacherUserId, request.fileIds)

        submitLessonReportForReview(
            teacherUserId = teacherUserId,
            lesson = lesson,
            commissionId = commission.id,
            subject = commission.guideInfo.subject,
            files = files,
        )

        val commissionFiles = replaceSubmissionCommissionFiles(commission, files)
        commissionReminderScheduler.cancelAllReminders(commissionId)

        return CommissionResponse.from(commission, commissionFiles)
    }

    private fun findDeliverableFiles(
        teacherUserId: String,
        fileIds: List<String>,
    ): List<File> {
        if (fileIds.size != fileIds.toSet().size) {
            throw BusinessException(GlobalErrorCode.INVALID_INPUT)
        }

        val files = fileAdaptor.findAllByIds(fileIds)
        if (files.size != fileIds.size) {
            throw BusinessException(GlobalErrorCode.INVALID_INPUT)
        }

        val filesById = files.associateBy { it.id }
        val orderedFiles =
            fileIds.map { fileId ->
                filesById[fileId] ?: throw BusinessException(GlobalErrorCode.INVALID_INPUT)
            }
        if (orderedFiles.any { it.uploadedBy != teacherUserId || it.fileType != FileType.TASK_SUBMISSION }) {
            throw BusinessException(GlobalErrorCode.INVALID_INPUT)
        }

        return orderedFiles
    }

    private fun replaceSubmissionCommissionFiles(
        commission: Commission,
        files: List<File>,
    ): List<CommissionFile> {
        val existingCommissionFiles = commissionFileAdaptor.findByCommissionId(commission.id)
        val existingSubmissionFiles = existingCommissionFiles.filter { it.file.fileType == FileType.TASK_SUBMISSION }
        val existingSubmissionFilesByFileId = existingSubmissionFiles.groupBy { it.file.id }
        val requestedFileIds = files.map { it.id }.toSet()
        val retainedSubmissionFilesByFileId =
            existingSubmissionFilesByFileId
                .filterKeys { it in requestedFileIds }
                .mapValues { (_, commissionFiles) -> commissionFiles.first() }

        val obsoleteSubmissionFiles =
            existingSubmissionFiles.filterNot { it.file.id in requestedFileIds } +
                existingSubmissionFilesByFileId
                    .filterKeys { it in requestedFileIds }
                    .values
                    .flatMap { commissionFiles -> commissionFiles.drop(1) }
        if (obsoleteSubmissionFiles.isNotEmpty()) {
            commissionFileAdaptor.deleteAll(obsoleteSubmissionFiles)
        }

        val newFiles = files.filterNot { it.id in retainedSubmissionFilesByFileId }
        val savedSubmissionFiles =
            if (newFiles.isEmpty()) {
                emptyList()
            } else {
                commissionFileAdaptor.saveAll(
                    newFiles.map { file -> CommissionFile(commission = commission, file = file) },
                )
            }

        val savedSubmissionFilesByFileId = savedSubmissionFiles.associateBy { it.file.id }
        val orderedSubmissionFiles =
            files.map { file ->
                retainedSubmissionFilesByFileId[file.id]
                    ?: savedSubmissionFilesByFileId[file.id]
                    ?: error("Saved commission file is missing for fileId=${file.id}")
            }

        return existingCommissionFiles.filter { it.file.fileType != FileType.TASK_SUBMISSION } + orderedSubmissionFiles
    }

    private fun resolveDeliverableLesson(
        teacherUserId: String,
        lessonId: Long?,
    ): Lesson {
        val lesson =
            lessonId?.let { lessonAdaptor.findById(it) }
                ?: throw BusinessException(CommissionErrorCode.INVALID_STATUS_TRANSITION)
        if (!lesson.isTeacher(teacherUserId)) throw LessonUnauthorizedAccessException()

        when (lesson.status) {
            LessonStatus.SCHEDULED, LessonStatus.IN_PROGRESS, LessonStatus.COMPLETED -> Unit
            LessonStatus.CANCELLED -> throw LessonInvalidStatusTransitionException()
        }
        return lesson
    }

    private fun submitLessonReportForReview(
        teacherUserId: String,
        lesson: Lesson,
        commissionId: Long,
        subject: String,
        files: List<File>,
    ) {
        when (lesson.status) {
            LessonStatus.SCHEDULED, LessonStatus.IN_PROGRESS -> {
                lesson.complete(teacherUserId)
                lessonAdaptor.save(lesson)
            }
            LessonStatus.COMPLETED -> Unit
            LessonStatus.CANCELLED -> throw LessonInvalidStatusTransitionException()
        }

        val content =
            buildAutoLessonReportContent(
                commissionId = commissionId,
                subject = subject,
                files = files,
            )
        val report = lessonReportAdaptor.findByLessonOrNull(lesson.id)

        when (report?.status) {
            null -> {
                val savedReport =
                    lessonReportAdaptor.save(
                        LessonReport(
                            lessonId = lesson.id,
                            submittedByUserId = teacherUserId,
                            content = content,
                        ),
                    )
                replaceLessonReportFiles(savedReport, files)
            }
            LessonReportStatus.PENDING_REVIEW -> {
                report.content = content
                lessonReportAdaptor.save(report)
                replaceLessonReportFiles(report, files)
            }
            LessonReportStatus.REJECTED -> {
                report.resubmit(content)
                lessonReportAdaptor.save(report)
                replaceLessonReportFiles(report, files)
            }
            LessonReportStatus.APPROVED -> throw LessonReportAlreadyApprovedException()
        }
    }

    private fun replaceLessonReportFiles(
        report: LessonReport,
        files: List<File>,
    ) {
        lessonReportFileAdaptor.deleteAllByLessonReportId(report.id)
        if (files.isNotEmpty()) {
            lessonReportFileAdaptor.saveAll(
                files.map { file -> LessonReportFile(lessonReport = report, file = file) },
            )
        }
    }

    private fun buildAutoLessonReportContent(
        commissionId: Long,
        subject: String,
        files: List<File>,
    ): String {
        val fileLines =
            files.joinToString("\n") { file ->
                "- ${file.originalFilename}"
            }
        return """
            수행평가 수업이 완료되어 자동 생성된 수업 완료 기록입니다.

            의뢰 ID: $commissionId
            과목: $subject
            최종 전달 파일:
            $fileLines
            """.trimIndent()
    }
}
