package com.sclass.domain.domains.oauth.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import com.sclass.domain.domains.oauth.exception.CentralGoogleAccountNotFoundException
import com.sclass.domain.domains.oauth.repository.CentralGoogleAccountRepository
import org.springframework.data.repository.findByIdOrNull

@Adaptor
class CentralGoogleAccountAdaptor(
    private val repository: CentralGoogleAccountRepository,
) {
    fun save(account: CentralGoogleAccount): CentralGoogleAccount = repository.save(account)

    fun findGoogle(): CentralGoogleAccount = findGoogleOrNull() ?: throw CentralGoogleAccountNotFoundException()

    fun findGoogleOrNull(): CentralGoogleAccount? = repository.findByIdOrNull(CentralGoogleAccount.PROVIDER_GOOGLE)

    fun deleteGoogle() = repository.deleteById(CentralGoogleAccount.PROVIDER_GOOGLE)
}
