package com.sclass.domain.domains.lesson.repository

import com.sclass.domain.domains.lesson.domain.Lesson

interface LessonCustomRepository {
    fun findAllByEffectiveTeacher(teacherUserId: String): List<Lesson>
}
