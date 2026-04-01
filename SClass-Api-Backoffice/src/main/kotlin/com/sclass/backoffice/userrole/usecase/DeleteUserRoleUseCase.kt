package com.sclass.backoffice.userrole.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeleteUserRoleUseCase(
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional
    fun execute(userRoleId: String) {
        if (!userRoleAdaptor.existsById(userRoleId)) {
            throw RoleNotFoundException()
        }
        userRoleAdaptor.delete(userRoleId)
    }
}
