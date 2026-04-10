package com.sclass.domain.domains.lesson.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.exception.LessonNotFoundException
import com.sclass.domain.domains.lesson.repository.LessonRepository
import org.springframework.data.repository.findByIdOrNull

@Adaptor
class LessonAdaptor(
    private val lessonRepository: LessonRepository,
) {
    fun save(lesson: Lesson): Lesson = lessonRepository.save(lesson)

    fun saveAll(lessons: List<Lesson>): List<Lesson> = lessonRepository.saveAll(lessons)

    fun findById(id: Long): Lesson = lessonRepository.findByIdOrNull(id) ?: throw LessonNotFoundException()

    fun findByIdOrNull(id: Long): Lesson? = lessonRepository.findByIdOrNull(id)

    fun findAllByEnrollment(enrollmentId: Long): List<Lesson> = lessonRepository.findAllByEnrollmentId(enrollmentId)

    fun findByCommission(commissionId: Long): Lesson? = lessonRepository.findAllBySourceCommissionId(commissionId).firstOrNull()

    fun findAllByStudent(studentUserId: String): List<Lesson> = lessonRepository.findAllByStudentUserId(studentUserId)

    fun findAllByAssignedTeacher(teacherUserId: String): List<Lesson> = lessonRepository.findAllByAssignedTeacherUserId(teacherUserId)

    fun findCompletedByActualTeacher(teacherUserId: String): List<Lesson> =
        lessonRepository.findAllByActualTeacherUserIdAndStatus(teacherUserId, LessonStatus.COMPLETED)
}
