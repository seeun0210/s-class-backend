package com.sclass.infrastructure.message.dto

data class AlimtalkRequest(
    val plusFriendId: String,
    val templateCode: String,
    val messages: List<Message>,
) {
    data class Message(
        val to: String,
        val content: String,
    )
}
