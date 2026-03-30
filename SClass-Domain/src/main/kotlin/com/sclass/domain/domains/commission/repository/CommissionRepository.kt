package com.sclass.domain.domains.commission.repository

import com.sclass.domain.domains.commission.domain.Commission
import org.springframework.data.jpa.repository.JpaRepository

interface CommissionRepository : JpaRepository<Commission, Long> {
    fun findByStudentUserId(studentUserId: String): List<Commission>

    fun findByTeacherUserId(teacherUserId: String): List<Commission>
}
