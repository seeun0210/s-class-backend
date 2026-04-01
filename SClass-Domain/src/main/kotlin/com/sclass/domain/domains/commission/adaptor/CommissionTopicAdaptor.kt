package com.sclass.domain.domains.commission.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.commission.domain.CommissionTopic
import com.sclass.domain.domains.commission.exception.CommissionTopicNotFoundException
import com.sclass.domain.domains.commission.repository.CommissionTopicRepository

@Adaptor
class CommissionTopicAdaptor(
    private val commissionTopicRepository: CommissionTopicRepository,
) {
    fun findById(id: Long): CommissionTopic = commissionTopicRepository.findById(id).orElseThrow { CommissionTopicNotFoundException() }

    fun findByCommissionId(commissionId: Long): List<CommissionTopic> = commissionTopicRepository.findByCommissionId(commissionId)

    fun saveAll(topics: List<CommissionTopic>): List<CommissionTopic> = commissionTopicRepository.saveAll(topics)
}
