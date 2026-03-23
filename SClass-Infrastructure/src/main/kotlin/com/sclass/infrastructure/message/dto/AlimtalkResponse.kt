package com.sclass.infrastructure.message.dto

data class AlimtalkResponse(
    val statusCode: String?,
    val statusName: String?,
    val messages: List<MessageResult>?,
) {
    data class MessageResult(
        val messageId: String?,
        val requestId: String?,
    )
}
