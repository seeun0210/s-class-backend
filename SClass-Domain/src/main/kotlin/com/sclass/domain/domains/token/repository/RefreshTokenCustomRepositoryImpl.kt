package com.sclass.domain.domains.token.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.token.domain.QRefreshToken.refreshToken
import java.time.LocalDateTime

class RefreshTokenCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : RefreshTokenCustomRepository {
    override fun deleteValidByTokenIdAndUserId(
        tokenId: String,
        userId: String,
        now: LocalDateTime,
    ): Long =
        queryFactory
            .delete(refreshToken)
            .where(
                refreshToken.tokenId.eq(tokenId),
                refreshToken.userId.eq(userId),
                refreshToken.expiresAt.gt(now),
            ).execute()
}
