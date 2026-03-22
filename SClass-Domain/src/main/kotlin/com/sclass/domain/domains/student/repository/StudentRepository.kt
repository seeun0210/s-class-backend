package com.sclass.domain.domains.student.repository

import com.sclass.domain.domains.student.domain.Student
import org.springframework.data.jpa.repository.JpaRepository

interface StudentRepository : JpaRepository<Student, String> {
    fun findAllByUserId(userId: String): List<Student>

    fun findByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): Student?

    fun findByUserIdAndOrganizationIdIsNull(userId: String): Student?

    fun findAllByOrganizationId(organizationId: Long): List<Student>

    fun existsByUserIdAndOrganizationId(
        userId: String,
        organizationId: Long,
    ): Boolean

    fun existsByUserIdAndOrganizationIdIsNull(userId: String): Boolean
}
