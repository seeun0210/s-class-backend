package com.sclass.domain.domains.lesson.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.exception.LessonNotFoundException
import com.sclass.domain.domains.lesson.repository.LessonRepository
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

@Adaptor
class LessonAdaptor(
    private val lessonRepository: LessonRepository,
) {
    fun save(lesson: Lesson): Lesson = lessonRepository.save(lesson)

    fun saveAll(lessons: List<Lesson>): List<Lesson> = lessonRepository.saveAll(lessons)

    fun findById(id: Long): Lesson = lessonRepository.findByIdOrNull(id) ?: throw LessonNotFoundException()

    fun findByIdOrNull(id: Long): Lesson? = lessonRepository.findByIdOrNull(id)

    fun findAllByIds(ids: Collection<Long>): List<Lesson> = lessonRepository.findAllById(ids)

    fun findAllByEnrollment(enrollmentId: Long): List<Lesson> =
        lessonRepository.findAllByEnrollmentIdOrderByLessonNumberAscCreatedAtAsc(enrollmentId)

    fun findByCommission(commissionId: Long): Lesson? = lessonRepository.findAllBySourceCommissionId(commissionId).firstOrNull()

    fun findAllByStudent(studentUserId: String): List<Lesson> = lessonRepository.findAllByStudentUserId(studentUserId)

    fun findAllByTeacher(teacherUserId: String): List<Lesson> = lessonRepository.findAllByEffectiveTeacher(teacherUserId)

    fun findAllBySubstituteTeacher(teacherUserId: String): List<Lesson> = lessonRepository.findAllBySubstituteTeacherUserId(teacherUserId)

    fun findCompletedByActualTeacher(teacherUserId: String): List<Lesson> =
        lessonRepository.findAllByActualTeacherUserIdAndStatus(teacherUserId, LessonStatus.COMPLETED)

    fun existsScheduleConflict(
        studentUserId: String,
        teacherUserId: String,
        scheduledAt: LocalDateTime,
        durationMinutes: Long,
        excludeLessonId: Long,
    ): Boolean =
        lessonRepository.existsScheduleConflict(
            studentUserId = studentUserId,
            teacherUserId = teacherUserId,
            scheduledAt = scheduledAt,
            durationMinutes = durationMinutes,
            excludeLessonId = excludeLessonId,
        )
}
