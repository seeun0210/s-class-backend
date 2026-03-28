package com.sclass.domain.domains.teacherassignment.repository

import com.sclass.domain.domains.teacherassignment.domain.TeacherAssignment
import com.sclass.domain.domains.user.domain.Platform
import org.springframework.data.jpa.repository.JpaRepository

interface TeacherAssignmentRepository :
    JpaRepository<TeacherAssignment, Long>,
    TeacherAssignmentCustomRepository {
    fun findByStudentUserIdAndPlatformAndOrganizationIdAndUnassignedAtIsNull(
        studentUserId: String,
        platform: Platform,
        organizationId: Long?,
    ): TeacherAssignment?

    fun findAllByStudentUserIdAndUnassignedAtIsNull(studentUserId: String): List<TeacherAssignment>

    fun findAllByTeacherUserIdAndUnassignedAtIsNull(teacherUserId: String): List<TeacherAssignment>

    fun findAllByTeacherUserIdAndPlatformAndOrganizationIdAndUnassignedAtIsNull(
        teacherUserId: String,
        platform: Platform,
        organizationId: Long?,
    ): List<TeacherAssignment>
}
