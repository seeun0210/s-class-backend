package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.OrganizationResponse
import com.sclass.backoffice.organization.dto.UpdateOrganizationSettingsRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.domain.OrganizationSettings
import com.sclass.domain.domains.organization.service.OrganizationDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateOrganizationSettingsUseCase(
    private val organizationDomainService: OrganizationDomainService,
) {
    @Transactional
    fun execute(
        organizationId: Long,
        request: UpdateOrganizationSettingsRequest,
    ): OrganizationResponse {
        val organization =
            organizationDomainService.updateSettings(
                id = organizationId,
                settings =
                    OrganizationSettings(
                        useSupporters = request.useSupporters,
                        useLms = request.useLms,
                    ),
            )
        return OrganizationResponse.from(organization)
    }
}
