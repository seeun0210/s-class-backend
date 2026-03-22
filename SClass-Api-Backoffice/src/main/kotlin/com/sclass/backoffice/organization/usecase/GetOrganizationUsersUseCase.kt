package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.OrganizationUserPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.service.OrganizationUserDomainService
import com.sclass.domain.domains.user.domain.Role
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetOrganizationUsersUseCase(
    private val organizationUserDomainService: OrganizationUserDomainService,
) {
    @Transactional(readOnly = true)
    fun execute(
        organizationId: Long,
        role: Role,
        pageable: Pageable,
    ): OrganizationUserPageResponse {
        val page = organizationUserDomainService.getUsersByRole(organizationId, role, pageable)
        return OrganizationUserPageResponse.from(page)
    }
}
