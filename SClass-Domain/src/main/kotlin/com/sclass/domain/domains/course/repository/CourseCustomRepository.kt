package com.sclass.domain.domains.course.repository

import com.sclass.domain.domains.course.dto.CourseWithTeacherDto

interface CourseCustomRepository {
    fun findAllActiveWithTeacher(): List<CourseWithTeacherDto>
}
