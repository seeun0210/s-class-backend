package com.sclass.backoffice.lesson.controller

import com.sclass.backoffice.lesson.dto.LessonResponse
import com.sclass.backoffice.lesson.dto.UpdateSubstituteTeacherRequest
import com.sclass.backoffice.lesson.usecase.UpdateSubstituteTeacherUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lessons")
class LessonController(
    private val updateSubstituteTeacherUseCase: UpdateSubstituteTeacherUseCase,
) {
    @PatchMapping("/{lessonId}/substitute-teacher")
    fun updateSubstitute(
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: UpdateSubstituteTeacherRequest,
    ): ApiResponse<LessonResponse> = ApiResponse.success(updateSubstituteTeacherUseCase.execute(lessonId, request))
}
