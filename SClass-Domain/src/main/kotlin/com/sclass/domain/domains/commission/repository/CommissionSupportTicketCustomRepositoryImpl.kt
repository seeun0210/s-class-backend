package com.sclass.domain.domains.commission.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.commission.domain.QCommission.commission
import com.sclass.domain.domains.commission.domain.QCommissionSupportTicket.commissionSupportTicket
import com.sclass.domain.domains.commission.domain.TicketStatus
import com.sclass.domain.domains.commission.dto.SupportTicketWithUsers
import com.sclass.domain.domains.user.domain.QUser

class CommissionSupportTicketCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CommissionSupportTicketCustomRepository {
    override fun findByStatusWithUsers(status: TicketStatus): List<SupportTicketWithUsers> {
        val teacherUser = QUser("teacherUser")
        val studentUser = QUser("studentUser")

        val results =
            queryFactory
                .select(commissionSupportTicket, teacherUser, studentUser)
                .from(commissionSupportTicket)
                .join(commissionSupportTicket.commission, commission)
                .fetchJoin()
                .join(teacherUser)
                .on(commission.teacherUserId.eq(teacherUser.id))
                .join(studentUser)
                .on(commission.studentUserId.eq(studentUser.id))
                .where(commissionSupportTicket.status.eq(status))
                .orderBy(commissionSupportTicket.createdAt.desc())
                .fetch()

        return results.map { tuple ->
            SupportTicketWithUsers(
                ticket = tuple.get(commissionSupportTicket)!!,
                teacherUser = tuple.get(teacherUser)!!,
                studentUser = tuple.get(studentUser)!!,
            )
        }
    }
}
