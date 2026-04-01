package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.exception.CommissionErrorCode
import com.sclass.domain.domains.user.domain.Role
import com.sclass.supporters.commission.dto.CommissionListResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCommissionListUseCase(
    private val commissionAdaptor: CommissionAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        role: Role,
    ): CommissionListResponse {
        val commissions =
            when (role) {
                Role.STUDENT -> commissionAdaptor.findByStudentUserId(userId)
                Role.TEACHER -> commissionAdaptor.findByTeacherUserId(userId)
                else -> throw BusinessException(CommissionErrorCode.UNAUTHORIZED_ACCESS)
            }
        return CommissionListResponse.from(commissions)
    }
}
