package com.sclass.domain.domains.lessonReport.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.lessonReport.domain.LessonReportFile
import com.sclass.domain.domains.lessonReport.repository.LessonReportFileRepository

@Adaptor
class LessonReportFileAdaptor(
    private val lessonReportFileRepository: LessonReportFileRepository,
) {
    fun findByLessonReportId(lessonReportId: Long): List<LessonReportFile> = lessonReportFileRepository.findByLessonReportId(lessonReportId)

    fun findByLessonReportIds(lessonReportIds: Collection<Long>): List<LessonReportFile> =
        lessonReportFileRepository.findByLessonReportIds(lessonReportIds)

    fun saveAll(files: List<LessonReportFile>): List<LessonReportFile> = lessonReportFileRepository.saveAll(files)

    fun deleteAllByLessonReportId(lessonReportId: Long) = lessonReportFileRepository.deleteAllByLessonReportId(lessonReportId)

    fun existsByFileId(fileId: String): Boolean = lessonReportFileRepository.existsByFileId(fileId)
}
