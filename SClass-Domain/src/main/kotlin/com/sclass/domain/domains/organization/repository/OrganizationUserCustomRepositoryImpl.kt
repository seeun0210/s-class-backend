package com.sclass.domain.domains.organization.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.organization.domain.QOrganizationUser.organizationUser
import com.sclass.domain.domains.organization.dto.OrganizationUserInfo
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
}
