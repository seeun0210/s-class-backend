package com.sclass.domain.domains.commission.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.commission.domain.Message
import com.sclass.domain.domains.commission.repository.MessageRepository

@Adaptor
class MessageAdaptor(
    private val messageRepository: MessageRepository,
) {
    fun findByCommissionId(commissionId: Long): List<Message> = messageRepository.findByCommissionIdOrderByCreatedAtAsc(commissionId)

    fun save(message: Message): Message = messageRepository.save(message)
}
