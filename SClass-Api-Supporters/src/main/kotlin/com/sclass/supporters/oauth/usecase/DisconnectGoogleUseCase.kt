package com.sclass.supporters.oauth.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.jwt.AesTokenEncryptor
import com.sclass.domain.domains.oauth.adaptor.TeacherGoogleAccountAdaptor
import com.sclass.infrastructure.oauth.client.GoogleAuthorizationCodeClient
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

@UseCase
class DisconnectGoogleUseCase(
    private val googleClient: GoogleAuthorizationCodeClient,
    private val accountAdaptor: TeacherGoogleAccountAdaptor,
    private val encryptor: AesTokenEncryptor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun execute(userId: String) {
        val account = accountAdaptor.findByUserIdOrNull(userId) ?: return

        runCatching {
            val refreshToken = encryptor.decrypt(account.encryptedRefreshToken)
            googleClient.revokeRefreshToken(refreshToken)
        }.onFailure {
            log.warn("Google token revoke 실패(DB 삭제는 진행)", it)
        }

        accountAdaptor.deleteByUserId(userId)
    }
}
