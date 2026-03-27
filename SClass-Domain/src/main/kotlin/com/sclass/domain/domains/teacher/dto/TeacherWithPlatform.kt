package com.sclass.domain.domains.teacher.dto

import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.user.domain.Platform

data class TeacherWithPlatform(
    val teacher: Teacher,
    val platform: Platform,
)
