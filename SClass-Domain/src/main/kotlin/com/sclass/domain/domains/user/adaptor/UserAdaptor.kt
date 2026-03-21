package com.sclass.domain.domains.user.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.exception.UserNotFoundException
import com.sclass.domain.domains.user.repository.UserRepository

@Adaptor
class UserAdaptor(
    private val userRepository: UserRepository,
) {
    fun findById(id: String): User = userRepository.findById(id).orElseThrow { UserNotFoundException.EXCEPTION }

    fun findByEmail(email: String): User = userRepository.findByEmail(email) ?: throw UserNotFoundException.EXCEPTION

    fun findByEmailOrNull(email: String): User? = userRepository.findByEmail(email)

    fun findByOauthId(
        oauthId: String,
        provider: AuthProvider,
    ): User? = userRepository.findByOauthIdAndAuthProvider(oauthId, provider)

    fun existsByEmail(email: String): Boolean = userRepository.existsByEmail(email)

    fun save(user: User): User = userRepository.save(user)

    fun delete(id: String) = userRepository.deleteById(id)
}
