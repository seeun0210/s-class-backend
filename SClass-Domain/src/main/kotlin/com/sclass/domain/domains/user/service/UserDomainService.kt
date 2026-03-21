package com.sclass.domain.domains.user.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.exception.InvalidPasswordException
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import com.sclass.domain.domains.user.exception.UserAlreadyExistsException
import org.springframework.transaction.annotation.Transactional

@DomainService
class UserDomainService(
    private val userAdaptor: UserAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
    private val passwordService: PasswordService,
) {
    @Transactional
    fun register(
        user: User,
        rawPassword: String,
        platform: Platform,
        role: Role,
    ): User {
        if (userAdaptor.existsByEmail(user.email)) {
            throw UserAlreadyExistsException()
        }

        user.hashedPassword = passwordService.hash(rawPassword)
        val savedUser = userAdaptor.save(user)

        userRoleAdaptor.save(
            UserRole(
                userId = savedUser.id,
                platform = platform,
                role = role,
            ),
        )

        return savedUser
    }

    @Transactional(readOnly = true)
    fun authenticate(
        email: String,
        rawPassword: String,
        platform: Platform,
        role: Role,
    ): User {
        val user = userAdaptor.findByEmail(email)

        val hashedPassword =
            user.hashedPassword
                ?: throw InvalidPasswordException()

        if (!passwordService.matches(rawPassword, hashedPassword)) {
            throw InvalidPasswordException()
        }

        userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, platform, role)
            ?: throw RoleNotFoundException()

        return user
    }
}
