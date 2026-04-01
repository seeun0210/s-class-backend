package com.sclass.domain.domains.commission.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.exception.CommissionNotFoundException
import com.sclass.domain.domains.commission.repository.CommissionRepository

@Adaptor
class CommissionAdaptor(
    private val commissionRepository: CommissionRepository,
) {
    fun findById(id: Long): Commission = commissionRepository.findById(id).orElseThrow { CommissionNotFoundException() }

    fun findByIdOrNull(id: Long): Commission? = commissionRepository.findById(id).orElse(null)

    fun findByStudentUserId(studentUserId: String): List<Commission> = commissionRepository.findByStudentUserId(studentUserId)

    fun findByTeacherUserId(teacherUserId: String): List<Commission> = commissionRepository.findByTeacherUserId(teacherUserId)

    fun save(commission: Commission): Commission = commissionRepository.save(commission)
}
