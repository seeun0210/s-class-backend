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
        studentUserId: String,
        teacherUserId: String,
        platform: Platform,
        organizationId: Long?,
        assignedBy: String,
    ): TeacherAssignment {
        validatePlatformOrganization(platform, organizationId)

        val now = LocalDateTime.now()

        val existing =
            teacherAssignmentAdaptor.findActiveByStudentUserIdAndPlatformAndOrganizationIdOrNull(
                studentUserId,
                platform,
                organizationId,
            )

        existing?.also {
            it.unassign(now)
            teacherAssignmentAdaptor.save(it)
        }

        val assignment =
            TeacherAssignment(
                studentUserId = studentUserId,
                teacherUserId = teacherUserId,
                platform = platform,
                organizationId = organizationId,
                assignedBy = assignedBy,
                assignedAt = now,
            )
        return teacherAssignmentAdaptor.save(assignment)
    }

    fun unassign(
        studentUserId: String,
        platform: Platform,
        organizationId: Long?,
    ) {
        validatePlatformOrganization(platform, organizationId)

        val assignment =
            teacherAssignmentAdaptor.findActiveByStudentUserIdAndPlatformAndOrganizationId(
                studentUserId,
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
