package com.sclass.backoffice.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.oauth.adaptor.CentralGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.CentralGoogleAccount
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@UseCase
class ConnectCentralGoogleAccountLockedUseCase(
    private val centralGoogleAccountAdaptor: CentralGoogleAccountAdaptor,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    @DistributedLock(prefix = "central-google-account", waitTime = 30)
    @Transactional
    fun execute(
        @LockKey provider: String,
        googleEmail: String,
        encryptedRefreshToken: String,
        scope: String,
        adminUserId: String,
    ): CentralGoogleAccount {
        val existing = centralGoogleAccountAdaptor.findGoogleOrNull()
        val now = LocalDateTime.now(clock)

        return if (existing != null) {
            existing.reconnect(
                newGoogleEmail = googleEmail,
                newEncryptedRefreshToken = encryptedRefreshToken,
                newScope = scope,
                adminUserId = adminUserId,
                at = now,
            )
            centralGoogleAccountAdaptor.save(existing)
        } else {
            centralGoogleAccountAdaptor.save(
                CentralGoogleAccount(
                    provider = provider,
                    googleEmail = googleEmail,
                    encryptedRefreshToken = encryptedRefreshToken,
                    scope = scope,
                    connectedByAdminUserId = adminUserId,
                    connectedAt = now,
                ),
            )
        }
    }
}
