package com.sclass.domain.domains.teacher.repository

import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherVerificationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TeacherRepository : JpaRepository<Teacher, String> {
    fun findByUserId(userId: String): Teacher?

    fun existsByUserId(userId: String): Boolean

    @Query("SELECT t FROM Teacher t WHERE t.verification.verificationStatus = :status")
    fun findAllByStatus(
        @Param("status") status: TeacherVerificationStatus,
        pageable: Pageable,
    ): Page<Teacher>
}
