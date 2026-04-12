package com.sclass.domain.domains.user.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import com.sclass.domain.domains.user.repository.UserRoleRepository

@Adaptor
class UserRoleAdaptor(
    private val userRoleRepository: UserRoleRepository,
) {
    fun findById(id: String): UserRole = userRoleRepository.findById(id).orElseThrow { RoleNotFoundException() }

    fun findAllByUserId(userId: String): List<UserRole> = userRoleRepository.findAllByUserId(userId)

    fun save(userRole: UserRole): UserRole = userRoleRepository.save(userRole)

    fun existsById(id: String): Boolean = userRoleRepository.existsById(id)

    fun delete(id: String) = userRoleRepository.deleteById(id)

    fun deleteAllByUserId(userId: String) = userRoleRepository.deleteAllByUserId(userId)

    fun findByUserIdAndPlatformAndRole(
        userId: String,
        platform: Platform,
        role: Role,
    ): UserRole? = userRoleRepository.findByUserIdAndPlatformAndRole(userId, platform, role)

    fun findAllByUserIdAndRole(
        userId: String,
        role: Role,
    ): List<UserRole> = userRoleRepository.findAllByUserIdAndRole(userId, role)
}
