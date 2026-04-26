package com.sclass.domain.domains.user.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.user.domain.QUser.user
import com.sclass.domain.domains.user.domain.QUserRole.userRole
import com.sclass.domain.domains.user.domain.User
import jakarta.persistence.LockModeType

class UserCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : UserCustomRepository {
    override fun findByEmailWithRoles(email: String): User? =
        queryFactory
            .selectFrom(user)
            .leftJoin(userRole)
            .on(user.id.eq(userRole.userId))
            .fetchOne()

    override fun findAllByIdsForUpdate(ids: Collection<String>): List<User> =
        queryFactory
            .selectFrom(user)
            .where(user.id.`in`(ids.distinct().sorted()))
            .orderBy(user.id.asc())
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetch()
}
