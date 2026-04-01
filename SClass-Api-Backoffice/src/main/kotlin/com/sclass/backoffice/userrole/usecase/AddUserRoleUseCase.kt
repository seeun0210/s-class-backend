package com.sclass.backoffice.userrole.usecase

import com.sclass.backoffice.userrole.dto.AddUserRoleRequest
import com.sclass.backoffice.userrole.dto.AddUserRoleResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.service.UserDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class AddUserRoleUseCase(
    private val userAdaptor: UserAdaptor,
    private val userDomainService: UserDomainService,
) {
    @Transactional
    fun execute(request: AddUserRoleRequest): AddUserRoleResponse {
        userAdaptor.findById(request.userId)
        val userRole = userDomainService.addUserRole(request.userId, request.platform, request.role)
        return AddUserRoleResponse.from(userRole)
    }
}
