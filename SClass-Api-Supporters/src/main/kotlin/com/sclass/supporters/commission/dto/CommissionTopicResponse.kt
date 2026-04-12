package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.CommissionTopic

data class CommissionTopicResponse(
    val id: Long,
    val topicId: String?,
    val title: String,
    val description: String?,
    val selected: Boolean,
) {
    companion object {
        fun from(topic: CommissionTopic) =
            CommissionTopicResponse(
                id = topic.id,
                topicId = topic.topicId,
                title = topic.title,
                description = topic.description,
                selected = topic.selected,
            )
    }
}

data class CommissionTopicListResponse(
    val topics: List<CommissionTopicResponse>,
) {
    companion object {
        fun from(topics: List<CommissionTopic>) =
            CommissionTopicListResponse(
                topics = topics.map { CommissionTopicResponse.from(it) },
            )
    }
}
