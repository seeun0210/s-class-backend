package com.sclass.domain.domains.coin.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.QCoinLot.coinLot
import jakarta.persistence.LockModeType
import java.time.LocalDateTime

class CoinLotCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CoinLotCustomRepository {
    override fun findActiveForUpdate(
        userId: String,
        now: LocalDateTime,
    ): List<CoinLot> =
        queryFactory
            .selectFrom(coinLot)
            .where(
                coinLot.userId.eq(userId),
                coinLot.remaining.gt(0),
                coinLot.expireAt.isNull.or(coinLot.expireAt.gt(now)),
            ).orderBy(
                coinLot.expireAt.asc().nullsLast(),
                coinLot.createdAt.asc(),
            ).setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetch()

    override fun sumActive(
        userId: String,
        now: LocalDateTime,
    ): Int =
        queryFactory
            .select(coinLot.remaining.sum().coalesce(0))
            .from(coinLot)
            .where(
                coinLot.userId.eq(userId),
                coinLot.remaining.gt(0),
                coinLot.expireAt.isNull.or(coinLot.expireAt.gt(now)),
            ).fetchOne() ?: 0

    override fun findActive(
        userId: String,
        now: LocalDateTime,
    ): List<CoinLot> =
        queryFactory
            .selectFrom(coinLot)
            .where(
                coinLot.userId.eq(userId),
                coinLot.remaining.gt(0),
                coinLot.expireAt.isNull.or(coinLot.expireAt.gt(now)),
            ).orderBy(
                coinLot.expireAt.asc().nullsLast(),
                coinLot.createdAt.asc(),
            ).fetch()

    override fun findExpiringBefore(
        now: LocalDateTime,
        limit: Int,
    ): List<CoinLot> =
        queryFactory
            .selectFrom(coinLot)
            .where(
                coinLot.expireAt.isNotNull,
                coinLot.expireAt.loe(now),
                coinLot.remaining.gt(0),
            ).limit(limit.toLong())
            .fetch()
}
