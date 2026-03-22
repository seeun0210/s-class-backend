package com.sclass.domain.domains.teacher.repository

import com.sclass.domain.domains.teacher.domain.Teacher
import org.springframework.data.jpa.repository.JpaRepository

interface TeacherRepository : JpaRepository<Teacher, String> {
    fun findByUserId(userId: String): Teacher?

    fun findByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): Teacher?

    fun findByUserIdAndOrganizationIdIsNull(userId: String): Teacher?

    fun findAllByOrganizationId(organizationId: Long): List<Teacher>

    fun existsByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): Boolean

    fun existsByUserIdAndOrganizationIdIsNull(userId: String): Boolean
}
