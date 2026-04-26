package com.sclass.domain.domains.lesson.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class LessonGoogleMeet(
    @Column(name = "google_calendar_event_id", length = 256)
    val calendarEventId: String,

    @Column(name = "google_meet_join_url", length = 512)
    val joinUrl: String,

    @Column(name = "google_meet_code", length = 64)
    val code: String? = null,
)
