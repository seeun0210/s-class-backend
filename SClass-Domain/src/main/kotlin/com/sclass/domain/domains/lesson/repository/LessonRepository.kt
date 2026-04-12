package com.sclass.domain.domains.lesson.repository

import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import org.springframework.data.jpa.repository.JpaRepository

interface LessonRepository : JpaRepository<Lesson, Long> {
    fun findAllByEnrollmentIdOrderByLessonNumberAscCreatedAtAsc(enrollmentId: Long): List<Lesson>

    fun findAllBySourceCommissionId(sourceCommissionId: Long): List<Lesson>

    fun findAllByStudentUserId(studentUserId: String): List<Lesson>

    fun findAllByAssignedTeacherUserId(assignedTeacherUserId: String): List<Lesson>

    fun findAllByActualTeacherUserIdAndStatus(
        actualTeacherUserId: String,
        status: LessonStatus,
    ): List<Lesson>

    fun findAllByLessonTypeAndStatus(
        lessonType: LessonType,
        status: LessonStatus,
    ): List<Lesson>
}
