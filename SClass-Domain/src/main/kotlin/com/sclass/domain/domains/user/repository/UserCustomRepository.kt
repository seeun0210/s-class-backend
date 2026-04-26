package com.sclass.domain.domains.user.repository

import com.sclass.domain.domains.user.domain.User

interface UserCustomRepository {
    fun findByEmailWithRoles(email: String): User?

    fun findAllByIdsForUpdate(ids: Collection<String>): List<User>
}
