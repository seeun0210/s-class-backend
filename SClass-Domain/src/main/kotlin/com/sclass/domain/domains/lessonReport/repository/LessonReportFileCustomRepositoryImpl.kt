package com.sclass.domain.domains.lessonReport.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.file.domain.QFile.file
import com.sclass.domain.domains.lessonReport.domain.LessonReportFile
import com.sclass.domain.domains.lessonReport.domain.QLessonReportFile.lessonReportFile

class LessonReportFileCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : LessonReportFileCustomRepository {
    override fun findByLessonReportId(lessonReportId: Long): List<LessonReportFile> =
        queryFactory
            .selectFrom(lessonReportFile)
            .join(lessonReportFile.file, file)
            .fetchJoin()
            .where(lessonReportFile.lessonReport.id.eq(lessonReportId))
            .fetch()

    override fun findByLessonReportIds(lessonReportIds: Collection<Long>): List<LessonReportFile> =
        queryFactory
            .selectFrom(lessonReportFile)
            .join(lessonReportFile.file, file)
            .fetchJoin()
            .where(lessonReportFile.lessonReport.id.`in`(lessonReportIds))
            .fetch()
}
