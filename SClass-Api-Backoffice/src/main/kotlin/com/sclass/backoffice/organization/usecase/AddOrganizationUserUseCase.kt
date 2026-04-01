package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.OrganizationUserCreateResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.adaptor.OrganizationUserAdaptor
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.organization.exception.OrganizationUserAlreadyExistsException
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class AddOrganizationUserUseCase(
    private val organizationAdaptor: OrganizationAdaptor,
    private val organizationUserAdaptor: OrganizationUserAdaptor,
    private val userAdaptor: UserAdaptor,
) {
    @Transactional
    fun execute(
        organizationId: Long,
        userId: String,
    ): OrganizationUserCreateResponse {
        val organization = organizationAdaptor.findById(organizationId)

        val user = userAdaptor.findById(userId)

        if (organizationUserAdaptor.existsByUserIdAndOrganizationId(user.id, organizationId)) {
            throw OrganizationUserAlreadyExistsException()
        }

        val organizationUser =
            organizationUserAdaptor.save(
                OrganizationUser(
                    user = user,
                    organization = organization,
                ),
            )

        return OrganizationUserCreateResponse.from(organizationUser)
    }
}
