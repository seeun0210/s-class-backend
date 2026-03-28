package com.sclass.domain.domains.teacher.repository

import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.dto.TeacherSearchCondition
import com.sclass.domain.domains.teacher.dto.TeacherWithPlatform
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TeacherCustomRepository {
    fun searchTeachers(
        condition: TeacherSearchCondition,
        page: Pageable,
    ): Page<TeacherWithPlatform>

    fun findByIdWithUser(id: String): Teacher?

    fun findByUserIdWithUser(userId: String): Teacher?

    fun findByDocumentsWithFileByTeacherId(teacherId: String): List<TeacherDocument>

    fun findOrganizationByUserId(userId: String): List<OrganizationUser>
}
