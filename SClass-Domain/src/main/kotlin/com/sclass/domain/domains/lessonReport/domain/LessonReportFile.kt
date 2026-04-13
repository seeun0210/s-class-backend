package com.sclass.domain.domains.lessonReport.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.domains.file.domain.File
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "lesson_report_files",
    indexes = [
        Index(name = "idx_lesson_report_files_report", columnList = "lesson_report_id"),
    ],
)
class LessonReportFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_report_id", nullable = false)
    val lessonReport: LessonReport,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    val file: File,
) : BaseTimeEntity()
