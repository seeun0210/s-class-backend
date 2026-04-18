package com.sclass.domain.domains.lesson.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonType

@DomainService
class LessonDomainService(
    private val lessonAdaptor: LessonAdaptor,
) {
    fun createLessonsForEnrollment(
        enrollment: Enrollment,
        teacherUserId: String,
        courseName: String,
        totalLessons: Int,
    ): List<Lesson> {
        val lessons =
            (1..totalLessons).map { lessonNumber ->
                Lesson(
                    lessonType = LessonType.COURSE,
                    enrollmentId = enrollment.id,
                    studentUserId = enrollment.studentUserId,
                    assignedTeacherUserId = teacherUserId,
                    lessonNumber = lessonNumber,
                    name = "$courseName ${lessonNumber}회차",
                )
            }
        return lessonAdaptor.saveAll(lessons)
    }
}
