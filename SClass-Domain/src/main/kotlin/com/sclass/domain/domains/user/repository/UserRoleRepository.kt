package com.sclass.domain.domains.user.repository

import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.UserRole
import org.springframework.data.jpa.repository.JpaRepository

interface UserRoleRepository : JpaRepository<UserRole, String> {
    fun findAllByUserId(userId: String): List<UserRole>

    fun deleteAllByUserId(userId: String)

    fun findByUserIdAndPlatformAndRole(
        userId: String,
        platform: Platform,
        role: Role,
    ): UserRole?
}
