package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.OrganizationResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetOrganizationUseCase(
    private val organizationAdaptor: OrganizationAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(organizationId: Long): OrganizationResponse {
        val organization = organizationAdaptor.findById(organizationId)
        return OrganizationResponse.from(organization)
    }
}
