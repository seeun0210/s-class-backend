package com.sclass.backoffice.organization.usecase

import com.sclass.backoffice.organization.dto.CreateOrganizationUserRequest
import com.sclass.backoffice.organization.dto.OrganizationUserCreateResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.organization.adaptor.OrganizationUserAdaptor
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.service.UserDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateOrganizationUserUseCase(
    private val organizationAdaptor: OrganizationAdaptor,
    private val organizationUserAdaptor: OrganizationUserAdaptor,
    private val userDomainService: UserDomainService,
) {
    @Transactional
    fun execute(
        organizationId: Long,
        request: CreateOrganizationUserRequest,
    ): OrganizationUserCreateResponse {
        val organization = organizationAdaptor.findById(organizationId)

        val user =
            userDomainService.register(
                user =
                    User(
                        email = request.email,
                        name = request.name,
                        authProvider = AuthProvider.EMAIL,
                        phoneNumber = request.phoneNumber?.let { User.formatPhoneNumber(it) },
                    ),
                rawPassword = DEFAULT_PASSWORD,
                platform = request.platform,
                role = request.role,
            )

        val organizationUser =
            organizationUserAdaptor.save(
                OrganizationUser(
                    user = user,
                    organization = organization,
                ),
            )

        return OrganizationUserCreateResponse.from(organizationUser)
    }

    companion object {
        private const val DEFAULT_PASSWORD = "12345678"
    }
}
