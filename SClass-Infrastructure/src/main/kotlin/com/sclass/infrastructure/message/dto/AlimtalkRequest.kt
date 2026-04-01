package com.sclass.infrastructure.message.dto

data class AlimtalkRequest(
    val plusFriendId: String,
    val templateCode: String,
    val messages: List<Message>,
) {
    data class Message(
        val to: String,
        val content: String,
        val buttons: List<Button>? = null,
    )

    data class Button(
        val type: String = "WL",
        val name: String,
        val linkMobile: String,
        val linkPc: String,
    )
}
