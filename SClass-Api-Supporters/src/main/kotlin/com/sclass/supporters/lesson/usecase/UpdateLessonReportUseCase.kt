package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.file.exception.FileNotFoundException
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportAdaptor
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportFileAdaptor
import com.sclass.domain.domains.lessonReport.domain.LessonReportFile
import com.sclass.infrastructure.s3.S3Service
import com.sclass.supporters.lesson.dto.LessonReportResponse
import com.sclass.supporters.lesson.dto.UpdateLessonReportRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateLessonReportUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val lessonReportAdaptor: LessonReportAdaptor,
    private val lessonReportFileAdaptor: LessonReportFileAdaptor,
    private val fileAdaptor: FileAdaptor,
    private val s3Service: S3Service,
) {
    @Transactional
    fun execute(
        userId: String,
        lessonId: Long,
        request: UpdateLessonReportRequest,
    ): LessonReportResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        if (!lesson.isTeacher(userId)) throw LessonUnauthorizedAccessException()

        val report = lessonReportAdaptor.findByLesson(lessonId)
        val requestedFiles =
            if (request.fileIds.isEmpty()) {
                emptyList()
            } else {
                fileAdaptor.findAllByIds(request.fileIds).also { files ->
                    if (files.map { it.id }.toSet() != request.fileIds.toSet()) {
                        throw FileNotFoundException()
                    }
                }
            }

        report.resubmit(request.content)
        lessonReportAdaptor.save(report)

        val existingFiles = lessonReportFileAdaptor.findByLessonReportId(report.id).map { it.file }
        val requestedFileIds = request.fileIds.toSet()
        val removedFiles = existingFiles.filter { it.id !in requestedFileIds }

        lessonReportFileAdaptor.deleteAllByLessonReportId(report.id)

        val savedFileIds =
            if (requestedFiles.isEmpty()) {
                emptyList()
            } else {
                val reportFiles = requestedFiles.map { LessonReportFile(lessonReport = report, file = it) }
                lessonReportFileAdaptor.saveAll(reportFiles).map { it.file.id }
            }

        removedFiles.forEach { file ->
            fileAdaptor.delete(file.id)
            s3Service.deleteObject(file.storedFilename)
        }

        return LessonReportResponse.of(report, savedFileIds)
    }
}
