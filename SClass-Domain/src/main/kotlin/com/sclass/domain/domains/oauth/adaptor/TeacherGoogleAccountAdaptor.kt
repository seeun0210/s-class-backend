package com.sclass.domain.domains.oauth.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.oauth.domain.TeacherGoogleAccount
import com.sclass.domain.domains.oauth.exception.TeacherGoogleAccountNotFoundException
import com.sclass.domain.domains.oauth.repository.TeacherGoogleAccountRepository

@Adaptor
class TeacherGoogleAccountAdaptor(
    private val repository: TeacherGoogleAccountRepository,
) {
    fun save(account: TeacherGoogleAccount): TeacherGoogleAccount = repository.save(account)

    fun findByUserId(userId: String): TeacherGoogleAccount =
        repository.findByUserId(userId) ?: throw TeacherGoogleAccountNotFoundException()

    fun findByUserIdOrNull(userId: String): TeacherGoogleAccount? = repository.findByUserId(userId)

    fun existsByUserId(userId: String): Boolean = repository.existsByUserId(userId)

    fun deleteByUserId(userId: String) = repository.deleteByUserId(userId)
}
