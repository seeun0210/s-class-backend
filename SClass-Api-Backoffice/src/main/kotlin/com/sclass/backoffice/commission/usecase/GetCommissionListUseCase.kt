package com.sclass.backoffice.commission.usecase

import com.sclass.backoffice.commission.dto.CommissionListResponse
import com.sclass.backoffice.commission.dto.CommissionPageResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.domain.CommissionStatus
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCommissionListUseCase(
    private val commissionAdaptor: CommissionAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        studentUserId: String?,
        teacherUserId: String?,
        status: CommissionStatus?,
        pageable: Pageable,
    ): CommissionPageResponse {
        val page = commissionAdaptor.searchCommissions(studentUserId, teacherUserId, status, pageable)
        return CommissionPageResponse(
            content = page.content.map { CommissionListResponse.from(it) },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
        )
    }
}
