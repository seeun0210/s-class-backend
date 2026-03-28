package com.sclass.domain.domains.organization.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.organization.domain.QOrganizationAttribution.organizationAttribution
import com.sclass.domain.domains.organization.domain.QOrganizationUser.organizationUser
import com.sclass.domain.domains.organization.dto.OrganizationUserInfo
import com.sclass.domain.domains.organization.dto.OrganizationUserSearchCondition
import com.sclass.domain.domains.user.domain.QUser.user
import com.sclass.domain.domains.user.domain.QUserRole.userRole
import com.sclass.domain.domains.user.domain.Role
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class OrganizationUserCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : OrganizationUserCustomRepository {
    override fun findUsersByOrganizationIdAndRole(
        organizationId: Long,
        role: Role,
        pageable: Pageable,
    ): Page<OrganizationUserInfo> {
        val content =
            queryFactory
                .select(
                    Projections.constructor(
                        OrganizationUserInfo::class.java,
                        user.id,
                        user.name,
                        user.email,
                        user.profileImageUrl,
                        userRole.role,
                        organizationUser.createdAt,
                    ),
                ).from(organizationUser)
                .join(organizationUser.user, user)
                .join(userRole)
                .on(user.id.eq(userRole.userId).and(userRole.role.eq(role)))
                .where(organizationUser.organization.id.eq(organizationId))
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .orderBy(organizationUser.createdAt.desc())
                .fetch()

        val total =
            queryFactory
                .select(organizationUser.count())
                .from(organizationUser)
                .join(userRole)
                .on(
                    organizationUser.user.id
                        .eq(userRole.userId)
                        .and(userRole.role.eq(role)),
                ).where(organizationUser.organization.id.eq(organizationId))
                .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun searchByOrganizationId(
        organizationId: Long,
        condition: OrganizationUserSearchCondition,
        pageable: Pageable,
    ): Page<OrganizationUserInfo> {
        val content =
            queryFactory
                .select(
                    Projections.constructor(
                        OrganizationUserInfo::class.java,
                        user.id,
                        user.name,
                        user.email,
                        user.profileImageUrl,
                        userRole.role,
                        organizationUser.createdAt,
                        organizationAttribution.source,
                        organizationAttribution.originService,
                    ),
                ).from(organizationUser)
                .join(organizationUser.user, user)
                .join(userRole)
                .on(user.id.eq(userRole.userId).and(roleEq(condition.role)))
                .leftJoin(organizationAttribution)
                .on(
                    organizationAttribution.studentId
                        .eq(user.id)
                        .and(organizationAttribution.organizationId.eq(organizationId)),
                ).where(
                    organizationUser.organization.id.eq(organizationId),
                    nameContains(condition.name),
                    emailContains(condition.email),
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .orderBy(organizationUser.createdAt.desc())
                .fetch()

        val total =
            queryFactory
                .select(organizationUser.count())
                .from(organizationUser)
                .join(organizationUser.user, user)
                .join(userRole)
                .on(user.id.eq(userRole.userId).and(roleEq(condition.role)))
                .where(
                    organizationUser.organization.id.eq(organizationId),
                    nameContains(condition.name),
                    emailContains(condition.email),
                ).fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun countByOrganizationIdGroupByRole(organizationId: Long): Map<Role, Long> =
        queryFactory
            .select(userRole.role, organizationUser.count())
            .from(organizationUser)
            .join(userRole)
            .on(organizationUser.user.id.eq(userRole.userId))
            .where(organizationUser.organization.id.eq(organizationId))
            .groupBy(userRole.role)
            .fetch()
            .associate { it.get(userRole.role)!! to it.get(organizationUser.count())!! }

    private fun nameContains(name: String?): BooleanExpression? = name?.let { user.name.contains(it) }

    private fun emailContains(email: String?): BooleanExpression? = email?.let { user.email.contains(it) }

    private fun roleEq(role: Role?): BooleanExpression = role?.let { userRole.role.eq(it) } ?: userRole.role.isNotNull
}
