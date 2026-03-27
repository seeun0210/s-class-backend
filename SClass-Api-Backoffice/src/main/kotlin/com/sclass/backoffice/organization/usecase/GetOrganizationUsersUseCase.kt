package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.OrganizationUserPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.adaptor.OrganizationUserAdaptor
import com.sclass.domain.domains.user.domain.Role
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetOrganizationUsersUseCase(
    private val organizationAdaptor: OrganizationAdaptor,
    private val organizationUserAdaptor: OrganizationUserAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        organizationId: Long,
        role: Role,
        pageable: Pageable,
    ): OrganizationUserPageResponse {
        organizationAdaptor.findById(organizationId)
        val page = organizationUserAdaptor.findUsersByOrganizationIdAndRole(organizationId, role, pageable)
        return OrganizationUserPageResponse.from(page)
    }
}
