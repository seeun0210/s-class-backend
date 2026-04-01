package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.Message
import com.sclass.domain.domains.commission.domain.MessageType
import java.time.LocalDateTime

data class MessageResponse(
    val id: Long,
    val senderId: String,
    val type: MessageType,
    val content: String,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(message: Message) =
            MessageResponse(
                id = message.id,
                senderId = message.senderId,
                type = message.type,
                content = message.content,
                createdAt = message.createdAt,
            )
    }
}
