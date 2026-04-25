package com.sclass.domain.domains.token.repository

import java.time.LocalDateTime

interface RefreshTokenCustomRepository {
    fun deleteValidByTokenIdAndUserId(
        tokenId: String,
        userId: String,
        now: LocalDateTime,
    ): Long
}
