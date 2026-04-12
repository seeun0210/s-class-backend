package com.sclass.domain.domains.commission.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.dto.CommissionWithDetailDto
import com.sclass.domain.domains.commission.exception.CommissionNotFoundException
import com.sclass.domain.domains.commission.repository.CommissionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class CommissionAdaptor(
    private val commissionRepository: CommissionRepository,
) {
    fun findById(id: Long): Commission = commissionRepository.findById(id).orElseThrow { CommissionNotFoundException() }

    fun findByIdOrNull(id: Long): Commission? = commissionRepository.findById(id).orElse(null)

    fun findByStudentUserId(studentUserId: String): List<Commission> = commissionRepository.findByStudentUserId(studentUserId)

    fun findByTeacherUserId(teacherUserId: String): List<Commission> = commissionRepository.findByTeacherUserId(teacherUserId)

    fun save(commission: Commission): Commission = commissionRepository.save(commission)

    fun searchCommissions(
        studentUserId: String?,
        teacherUserId: String?,
        status: CommissionStatus?,
        pageable: Pageable,
    ): Page<CommissionWithDetailDto> = commissionRepository.searchCommissions(studentUserId, teacherUserId, status, pageable)
}
