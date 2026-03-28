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

    fun findActiveByStudentIdAndPlatformAndOrganizationIdOrNull(
        studentId: String,
        platform: Platform,
        organizationId: Long?,
    ): TeacherAssignment? =
        teacherAssignmentRepository.findByStudentIdAndPlatformAndOrganizationIdAndUnassignedAtIsNull(
            studentId,
            platform,
            organizationId,
        )

    fun findActiveByStudentIdAndPlatformAndOrganizationId(
        studentId: String,
        platform: Platform,
        organizationId: Long?,
    ): TeacherAssignment =
        findActiveByStudentIdAndPlatformAndOrganizationIdOrNull(studentId, platform, organizationId)
            ?: throw TeacherAssignmentNotFoundException()

    fun findAllActiveByStudentId(studentId: String): List<TeacherAssignment> =
        teacherAssignmentRepository.findAllByStudentIdAndUnassignedAtIsNull(studentId)

    fun findAllActiveByTeacherId(teacherId: String): List<TeacherAssignment> =
        teacherAssignmentRepository.findAllByTeacherIdAndUnassignedAtIsNull(teacherId)

    fun findAllActiveByTeacherIdAndPlatformAndOrganizationId(
        teacherId: String,
        platform: Platform,
        organizationId: Long?,
    ): List<TeacherAssignment> =
        teacherAssignmentRepository.findAllByTeacherIdAndPlatformAndOrganizationIdAndUnassignedAtIsNull(
            teacherId,
            platform,
            organizationId,
        )

    fun findActiveAssignedStudentsByTeacherId(teacherId: String): List<AssignedStudentInfo> =
        teacherAssignmentRepository.findActiveAssignedStudentsByTeacherId(teacherId)

    fun findActiveAssignedTeachersByStudentId(studentId: String): List<AssignedTeacherInfo> =
        teacherAssignmentRepository.findActiveAssignedTeachersByStudentId(studentId)

    fun searchActiveAssignments(
        condition: TeacherAssignmentSearchCondition,
        pageable: Pageable,
    ): Page<TeacherAssignmentListInfo> =
        teacherAssignmentRepository.searchActiveAssignments(
            condition,
            pageable,
        )
}
