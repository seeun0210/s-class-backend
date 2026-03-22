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
        return OrganizationUserStatsResponse(
            totalCount = roleCounts.values.sum(),
            adminCount = roleCounts.getOrDefault(Role.ADMIN, 0L),
            teacherCount = roleCounts.getOrDefault(Role.TEACHER, 0L),
            studentCount = roleCounts.getOrDefault(Role.STUDENT, 0L),
        )
    }
}
