package com.sclass.domain.domains.user.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.repository.UserRoleRepository

@Adaptor
class UserRoleAdaptor(
    private val userRoleRepository: UserRoleRepository,
) {
    fun findAllByUserId(userId: String): List<UserRole> = userRoleRepository.findAllByUserId(userId)

    fun save(userRole: UserRole): UserRole = userRoleRepository.save(userRole)

    fun deleteAllByUserId(userId: String) = userRoleRepository.deleteAllByUserId(userId)
}
