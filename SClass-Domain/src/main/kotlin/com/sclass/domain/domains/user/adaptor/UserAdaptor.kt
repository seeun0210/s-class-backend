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
    fun findById(id: String): User = userRepository.findById(id).orElseThrow { UserNotFoundException() }

    fun lockByIdsForUpdate(ids: Collection<String>) {
        val distinctIds = ids.distinct()
        val users = userRepository.findAllByIdsForUpdate(distinctIds)
        if (users.size != distinctIds.size) throw UserNotFoundException()
    }

    fun findByEmail(email: String): User = userRepository.findByEmail(email) ?: throw UserNotFoundException()

    fun findByEmailOrNull(email: String): User? = userRepository.findByEmail(email)

    fun findByOauthId(
        oauthId: String,
        provider: AuthProvider,
    ): User? = userRepository.findByOauthIdAndAuthProvider(oauthId, provider)

    fun existsByEmail(email: String): Boolean = userRepository.existsByEmail(email)

    fun save(user: User): User = userRepository.save(user)

    fun delete(id: String) = userRepository.deleteById(id)
}
