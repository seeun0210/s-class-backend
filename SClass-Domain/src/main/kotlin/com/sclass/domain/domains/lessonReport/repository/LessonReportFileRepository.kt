package com.sclass.domain.domains.lessonReport.repository

import com.sclass.domain.domains.lessonReport.domain.LessonReportFile
import org.springframework.data.jpa.repository.JpaRepository

interface LessonReportFileRepository :
    JpaRepository<LessonReportFile, Long>,
    LessonReportFileCustomRepository
