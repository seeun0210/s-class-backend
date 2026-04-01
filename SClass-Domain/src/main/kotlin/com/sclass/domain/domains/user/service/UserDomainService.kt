package com.sclass.domain.domains.user.service

import com.sclass.common.annotation.DomainService
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import com.sclass.domain.domains.user.exception.ConflictingRoleException
import com.sclass.domain.domains.user.exception.DuplicateUserRoleException
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
                state = initialStateFor(role),
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

    fun findByOAuthOrNull(
        oauthId: String,
        authProvider: AuthProvider,
    ): User? = userAdaptor.findByOauthId(oauthId, authProvider)

    @Transactional
    fun linkOAuthAndEnsureRole(
        email: String,
        oauthId: String,
        platform: Platform,
        role: Role,
    ): User? {
        val user = userAdaptor.findByEmailOrNull(email) ?: return null
        user.oauthId = oauthId
        ensureUserRole(user.id, platform, role)
        return userAdaptor.save(user)
    }

    @Transactional
    fun registerWithOAuth(
        oauthId: String,
        authProvider: AuthProvider,
        email: String,
        name: String,
        phoneNumber: String,
        profileImageUrl: String?,
        platform: Platform,
        role: Role,
    ): User {
        if (userAdaptor.existsByEmail(email)) {
            throw UserAlreadyExistsException()
        }

        val user =
            User(
                email = email,
                name = name,
                authProvider = authProvider,
                oauthId = oauthId,
                phoneNumber = User.formatPhoneNumber(phoneNumber),
                profileImageUrl = profileImageUrl,
                emailVerified = true,
            )
        val savedUser = userAdaptor.save(user)

        userRoleAdaptor.save(
            UserRole(
                userId = savedUser.id,
                platform = platform,
                role = role,
                state = initialStateFor(role),
            ),
        )

        return savedUser
    }

    fun ensureUserRole(
        userId: String,
        platform: Platform,
        role: Role,
    ) {
        if (userRoleAdaptor.findByUserIdAndPlatformAndRole(userId, platform, role) == null) {
            validateNoConflictingRole(userId, platform, role)
            userRoleAdaptor.save(
                UserRole(
                    userId = userId,
                    platform = platform,
                    role = role,
                    state = initialStateFor(role),
                ),
            )
        }
    }

    fun addUserRole(
        userId: String,
        platform: Platform,
        role: Role,
    ): UserRole {
        if (userRoleAdaptor.findByUserIdAndPlatformAndRole(userId, platform, role) != null) {
            throw DuplicateUserRoleException()
        }
        validateNoConflictingRole(userId, platform, role)
        return userRoleAdaptor.save(
            UserRole(
                userId = userId,
                platform = platform,
                role = role,
                state = initialStateFor(role),
            ),
        )
    }

    private fun validateNoConflictingRole(
        userId: String,
        platform: Platform,
        role: Role,
    ) {
        val conflicting = CONFLICTING_ROLES[role] ?: return
        if (userRoleAdaptor.findByUserIdAndPlatformAndRole(userId, platform, conflicting) != null) {
            throw ConflictingRoleException()
        }
    }

    companion object {
        private val CONFLICTING_ROLES =
            mapOf(
                Role.STUDENT to Role.TEACHER,
                Role.TEACHER to Role.STUDENT,
            )
    }

    fun activateIfApproved(
        userId: String,
        platform: Platform,
        role: Role,
    ) {
        val userRole = userRoleAdaptor.findByUserIdAndPlatformAndRole(userId, platform, role) ?: return
        if (userRole.state == UserRoleState.APPROVED) {
            userRole.changeStateTo(UserRoleState.NORMAL)
            userRoleAdaptor.save(userRole)
        }
    }

    private fun initialStateFor(role: Role): UserRoleState = if (role == Role.TEACHER) UserRoleState.DRAFT else UserRoleState.NORMAL
}
