package com.sclass.domain.domains.teacherassignment.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.teacherassignment.adaptor.TeacherAssignmentAdaptor
import com.sclass.domain.domains.teacherassignment.domain.TeacherAssignment
import com.sclass.domain.domains.teacherassignment.exception.InvalidPlatformForAssignmentException
import com.sclass.domain.domains.teacherassignment.exception.OrganizationNotAllowedForSupportersException
import com.sclass.domain.domains.teacherassignment.exception.OrganizationRequiredForLmsException
import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

@DomainService
class TeacherAssignmentDomainService(
    private val teacherAssignmentAdaptor: TeacherAssignmentAdaptor,
) {
    fun assign(
        studentId: String,
        teacherId: String,
        platform: Platform,
        organizationId: Long?,
        assignedBy: String,
    ): TeacherAssignment {
        validatePlatformOrganization(platform, organizationId)

        val now = LocalDateTime.now()

        val existing =
            teacherAssignmentAdaptor.findActiveByStudentIdAndPlatformAndOrganizationIdOrNull(
                studentId,
                platform,
                organizationId,
            )

        existing?.also {
            it.unassign(now)
            teacherAssignmentAdaptor.save(it)
        }

        val assignment =
            TeacherAssignment(
                studentId = studentId,
                teacherId = teacherId,
                platform = platform,
                organizationId = organizationId,
                assignedBy = assignedBy,
                assignedAt = now,
            )
        return teacherAssignmentAdaptor.save(assignment)
    }

    fun unassign(
        studentId: String,
        platform: Platform,
        organizationId: Long?,
    ) {
        validatePlatformOrganization(platform, organizationId)

        val assignment =
            teacherAssignmentAdaptor.findActiveByStudentIdAndPlatformAndOrganizationId(
                studentId,
                platform,
                organizationId,
            )

        assignment.unassign()
        teacherAssignmentAdaptor.save(assignment)
    }

    private fun validatePlatformOrganization(
        platform: Platform,
        organizationId: Long?,
    ) {
        when (platform) {
            Platform.LMS -> if (organizationId == null) throw OrganizationRequiredForLmsException()
            Platform.SUPPORTERS ->
                if (organizationId != null) {
                    throw OrganizationNotAllowedForSupportersException()
                }
            else -> throw InvalidPlatformForAssignmentException()
        }
    }
}
