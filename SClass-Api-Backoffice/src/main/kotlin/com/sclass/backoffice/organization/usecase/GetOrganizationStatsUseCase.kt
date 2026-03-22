package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.OrganizationUserStatsResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.service.OrganizationUserDomainService
import com.sclass.domain.domains.user.domain.Role
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetOrganizationStatsUseCase(
    private val organizationUserDomainService: OrganizationUserDomainService,
) {
    @Transactional(readOnly = true)
    fun execute(organizationId: Long): OrganizationUserStatsResponse {
        val roleCounts = organizationUserDomainService.getUserStats(organizationId)
        val adminCount = roleCounts[Role.ADMIN] ?: 0L
        val teacherCount = roleCounts[Role.TEACHER] ?: 0L
        val studentCount = roleCounts[Role.STUDENT] ?: 0L
        return OrganizationUserStatsResponse(
            totalCount = adminCount + teacherCount + studentCount,
            adminCount = adminCount,
            teacherCount = teacherCount,
            studentCount = studentCount,
        )
    }
}
