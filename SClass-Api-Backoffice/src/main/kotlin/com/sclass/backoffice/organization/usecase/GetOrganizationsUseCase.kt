package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.OrganizationPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetOrganizationsUseCase(
    private val organizationAdaptor: OrganizationAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(pageable: Pageable): OrganizationPageResponse {
        val page = organizationAdaptor.findAll(pageable)
        return OrganizationPageResponse.from(page)
    }
}
