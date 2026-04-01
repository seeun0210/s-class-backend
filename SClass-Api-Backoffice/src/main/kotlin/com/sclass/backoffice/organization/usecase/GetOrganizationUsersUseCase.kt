package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.OrganizationUserPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.adaptor.OrganizationUserAdaptor
import com.sclass.domain.domains.organization.dto.OrganizationUserSearchCondition
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
        condition: OrganizationUserSearchCondition,
        pageable: Pageable,
    ): OrganizationUserPageResponse {
        organizationAdaptor.findById(organizationId)
        val page = organizationUserAdaptor.searchByOrganizationId(organizationId, condition, pageable)
        return OrganizationUserPageResponse.from(page)
    }
}
