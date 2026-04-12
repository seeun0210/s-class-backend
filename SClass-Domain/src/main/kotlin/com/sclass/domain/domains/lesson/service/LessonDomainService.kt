package com.sclass.domain.domains.lesson.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.course.domain.Course
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
        course: Course,
        totalLessons: Int,
        teacherPayoutPerLessonWon: Int,
    ): List<Lesson> {
        val lessons =
            (1..totalLessons).map { lessonNumber ->
                Lesson(
                    lessonType = LessonType.COURSE,
                    enrollmentId = enrollment.id,
                    studentUserId = enrollment.studentUserId,
                    assignedTeacherUserId = course.teacherUserId,
                    lessonNumber = lessonNumber,
                    name = "${course.name} ${lessonNumber}회차",
                    teacherPayoutAmountWon = teacherPayoutPerLessonWon,
                )
            }
        return lessonAdaptor.saveAll(lessons)
    }
}
