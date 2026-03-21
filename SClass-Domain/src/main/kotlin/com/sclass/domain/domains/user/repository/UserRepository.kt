package com.sclass.domain.domains.user.repository

import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository :
    JpaRepository<User, String>,
    UserCustomRepository {
    fun findByEmail(email: String): User?

    fun existsByEmail(email: String): Boolean

    fun findByOauthIdAndAuthProvider(
        oauthId: String,
        authProvider: AuthProvider,
    ): User?
}
