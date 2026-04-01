package com.sclass.infrastructure.message.dto

data class AlimtalkTemplate(
    val templateCode: String,
    val content: String,
    val buttons: List<AlimtalkRequest.Button>? = null,
)
