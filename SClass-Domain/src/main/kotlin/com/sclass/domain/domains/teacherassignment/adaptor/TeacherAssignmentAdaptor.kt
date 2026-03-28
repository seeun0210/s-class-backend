package com.sclass.domain.domains.teacherassignment.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.teacherassignment.domain.TeacherAssignment
import com.sclass.domain.domains.teacherassignment.dto.AssignedStudentInfo
import com.sclass.domain.domains.teacherassignment.dto.AssignedTeacherInfo
import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentListInfo
import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentSearchCondition
import com.sclass.domain.domains.teacherassignment.exception.TeacherAssignmentNotFoundException
import com.sclass.domain.domains.teacherassignment.repository.TeacherAssignmentRepository
import com.sclass.domain.domains.user.domain.Platform
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class TeacherAssignmentAdaptor(
    private val teacherAssignmentRepository: TeacherAssignmentRepository,
) {
    fun save(teacherAssignment: TeacherAssignment): TeacherAssignment = teacherAssignmentRepository.save(teacherAssignment)

    fun findActiveByStudentUserIdAndPlatformAndOrganizationIdOrNull(
        studentUserId: String,
        platform: Platform,
        organizationId: Long?,
    ): TeacherAssignment? =
        teacherAssignmentRepository.findByStudentUserIdAndPlatformAndOrganizationIdAndUnassignedAtIsNull(
            studentUserId,
            platform,
            organizationId,
        )

    fun findActiveByStudentUserIdAndPlatformAndOrganizationId(
        studentUserId: String,
        platform: Platform,
        organizationId: Long?,
    ): TeacherAssignment =
        findActiveByStudentUserIdAndPlatformAndOrganizationIdOrNull(studentUserId, platform, organizationId)
            ?: throw TeacherAssignmentNotFoundException()

    fun findActiveAssignedStudentsByTeacherUserId(
        teacherUserId: String,
        platform: Platform? = null,
    ): List<AssignedStudentInfo> = teacherAssignmentRepository.findActiveAssignedStudentsByTeacherUserId(teacherUserId, platform)

    fun findAllActiveByStudentUserId(studentUserId: String): List<TeacherAssignment> =
        teacherAssignmentRepository.findAllByStudentUserIdAndUnassignedAtIsNull(studentUserId)

    fun findAllActiveByTeacherUserId(teacherUserId: String): List<TeacherAssignment> =
        teacherAssignmentRepository.findAllByTeacherUserIdAndUnassignedAtIsNull(teacherUserId)

    fun findAllActiveByTeacherUserIdAndPlatformAndOrganizationId(
        teacherUserId: String,
        platform: Platform,
        organizationId: Long?,
    ): List<TeacherAssignment> =
        teacherAssignmentRepository.findAllByTeacherUserIdAndPlatformAndOrganizationIdAndUnassignedAtIsNull(
            teacherUserId,
            platform,
            organizationId,
        )

    fun findActiveAssignedStudentsByTeacherUserId(teacherUserId: String): List<AssignedStudentInfo> =
        teacherAssignmentRepository.findActiveAssignedStudentsByTeacherUserId(teacherUserId)

    fun findActiveAssignedTeachersByStudentUserId(studentUserId: String): List<AssignedTeacherInfo> =
        teacherAssignmentRepository.findActiveAssignedTeachersByStudentUserId(studentUserId)

    fun searchActiveAssignments(
        condition: TeacherAssignmentSearchCondition,
        pageable: Pageable,
    ): Page<TeacherAssignmentListInfo> = teacherAssignmentRepository.searchActiveAssignments(condition, pageable)
}
