package com.sclass.supporters.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.oauth.adaptor.TeacherGoogleAccountAdaptor
import com.sclass.domain.domains.oauth.domain.TeacherGoogleAccount
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@UseCase
class ConnectGoogleAccountLockedUseCase(
    private val accountAdaptor: TeacherGoogleAccountAdaptor,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    @DistributedLock(prefix = "teacher-google-account", waitTime = 30)
    @Transactional
    fun execute(
        @LockKey userId: String,
        googleEmail: String,
        encryptedRefreshToken: String,
        scope: String,
    ): TeacherGoogleAccount {
        val existing = accountAdaptor.findByUserIdOrNull(userId)

        return if (existing != null) {
            existing.reconnect(googleEmail, encryptedRefreshToken, scope)
            accountAdaptor.save(existing)
        } else {
            accountAdaptor.save(
                TeacherGoogleAccount(
                    userId = userId,
                    googleEmail = googleEmail,
                    encryptedRefreshToken = encryptedRefreshToken,
                    scope = scope,
                    connectedAt = LocalDateTime.now(clock),
                ),
            )
        }
    }
}
