package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.Message

data class MessageListResponse(
    val messages: List<MessageResponse>,
) {
    companion object {
        fun from(messages: List<Message>) =
            MessageListResponse(
                messages = messages.map { MessageResponse.from(it) },
            )
    }
}
