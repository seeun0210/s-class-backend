package com.sclass.domain.domains.course.dto

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.user.domain.User

data class CourseWithTeacherDto(
    val course: Course,
    val teacher: Teacher?,
    val teacherUser: User?,
)
