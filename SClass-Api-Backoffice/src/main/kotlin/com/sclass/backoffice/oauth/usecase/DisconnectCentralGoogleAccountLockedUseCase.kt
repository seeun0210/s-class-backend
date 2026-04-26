package com.sclass.backoffice.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.infrastructure.oauth.client.CentralGoogleAuthorizationCodeClient
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

@UseCase
class DisconnectCentralGoogleAccountLockedUseCase(
    private val centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor,
    private val googleClient: CentralGoogleAuthorizationCodeClient,
    private val encryptor: AesTokenEncryptor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @DistributedLock(prefix = "central-google-account", waitTime = 30)
    @Transactional
    fun execute(
        @LockKey provider: String,
    ) {
        val account = centralGoogleAccountAdaptor.findGoogleOrNull() ?: return

        runCatching {
            googleClient.revokeRefreshToken(encryptor.decrypt(account.encryptedRefreshToken))
        }.onFailure {
            log.warn("Central Google token revoke failed (DB delete continues)", it)
        }

        centralGoogleAccountAdaptor.deleteGoogle()
    }
}
