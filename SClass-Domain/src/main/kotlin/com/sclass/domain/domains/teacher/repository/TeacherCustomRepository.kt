package com.sclass.domain.domains.teacher.repository

import com.sclass.domain.domains.teacher.dto.TeacherSearchCondition
import com.sclass.domain.domains.teacher.dto.TeacherWithPlatform
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TeacherCustomRepository {
    fun searchTeachers(
        condition: TeacherSearchCondition,
        page: Pageable,
    ): Page<TeacherWithPlatform>
}
