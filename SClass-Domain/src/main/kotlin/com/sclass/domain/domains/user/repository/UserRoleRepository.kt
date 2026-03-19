package com.sclass.domain.domains.user.repository

import com.sclass.domain.domains.user.domain.UserRole
import org.springframework.data.jpa.repository.JpaRepository

interface UserRoleRepository : JpaRepository<UserRole, String> {
    fun findAllByUserId(userId: String): List<UserRole>

    fun deleteAllByUserId(userId: String)
}
