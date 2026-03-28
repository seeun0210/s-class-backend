package com.sclass.domain.domains.teacherassignment.repository

import com.sclass.domain.domains.teacherassignment.domain.TeacherAssignment
import com.sclass.domain.domains.user.domain.Platform
import org.springframework.data.jpa.repository.JpaRepository

interface TeacherAssignmentRepository : JpaRepository<TeacherAssignment, Long> {
    fun findByStudentIdAndPlatformAndOrganizationIdAndUnassignedAtIsNull(
        studentId: String,
        platform: Platform,
        organizationId: Long?,
    ): TeacherAssignment?

    fun findAllByStudentIdAndUnassignedAtIsNull(studentId: String): List<TeacherAssignment>

    fun findAllByTeacherIdAndUnassignedAtIsNull(teacherId: String): List<TeacherAssignment>

    fun findAllByTeacherIdAndPlatformAndOrganizationIdAndUnassignedAtIsNull(
        teacherId: String,
        platform: Platform,
        organizationId: Long?,
    ): List<TeacherAssignment>
}
