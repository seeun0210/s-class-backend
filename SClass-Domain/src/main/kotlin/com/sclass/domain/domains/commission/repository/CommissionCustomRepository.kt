package com.sclass.domain.domains.commission.repository

import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.dto.CommissionWithDetailDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CommissionCustomRepository {
    fun searchCommissions(
        studentUserId: String?,
        teacherUserId: String?,
        status: CommissionStatus?,
        pageable: Pageable,
    ): Page<CommissionWithDetailDto>
}
