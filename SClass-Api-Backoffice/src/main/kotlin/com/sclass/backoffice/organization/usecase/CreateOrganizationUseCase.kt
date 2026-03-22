package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.CreateOrganizationRequest
import com.sclass.backoffice.organization.dto.OrganizationResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.service.OrganizationDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateOrganizationUseCase(
    private val organizationDomainService: OrganizationDomainService,
) {
    @Transactional
    fun execute(request: CreateOrganizationRequest): OrganizationResponse {
        val organization =
            organizationDomainService.create(
                name = request.name,
                domain = request.domain,
                logoUrl = request.logoUrl,
            )
        return OrganizationResponse.from(organization)
    }
}
