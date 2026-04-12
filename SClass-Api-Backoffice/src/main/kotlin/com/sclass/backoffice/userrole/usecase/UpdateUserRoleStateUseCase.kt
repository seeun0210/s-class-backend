package com.sclass.backoffice.userrole.usecase

import com.sclass.backoffice.userrole.dto.UpdateUserRoleStateRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.UserRoleState
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateUserRoleStateUseCase(
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional
    fun execute(
        userRoleId: String,
        request: UpdateUserRoleStateRequest,
        adminUserId: String,
    ) {
        val userRole = userRoleAdaptor.findById(userRoleId)
        when (request.state) {
            UserRoleState.ACTIVE -> userRole.approve(adminUserId)
            UserRoleState.REJECTED -> userRole.reject(requireNotNull(request.reason))
            else -> error("Unreachable")
        }
    }
}
