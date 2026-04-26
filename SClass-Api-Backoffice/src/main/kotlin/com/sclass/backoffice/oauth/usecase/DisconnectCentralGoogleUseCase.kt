package com.sclass.backoffice.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.infrastructure.oauth.client.GoogleAuthorizationCodeClient
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

@UseCase
class DisconnectCentralGoogleUseCase(
    private val centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor,
    private val googleClient: GoogleAuthorizationCodeClient,
    private val encryptor: AesTokenEncryptor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun execute() {
        val account = centralGoogleAccountAdaptor.findGoogleOrNull() ?: return
        runCatching {
            googleClient.revokeRefreshToken(encryptor.decrypt(account.encryptedRefreshToken))
        }.onFailure {
            log.warn("Central Google token revoke failed (DB delete continues)", it)
        }
        centralGoogleAccountAdaptor.deleteGoogle()
    }
}
