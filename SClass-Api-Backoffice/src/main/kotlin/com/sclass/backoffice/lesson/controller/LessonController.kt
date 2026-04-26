package com.sclass.backoffice.lesson.controller

import com.sclass.backoffice.lesson.dto.LessonResponse
import com.sclass.backoffice.lesson.dto.ScheduleLessonRequest
import com.sclass.backoffice.lesson.dto.UpdateSubstituteTeacherRequest
import com.sclass.backoffice.lesson.usecase.CreateLessonScheduleUseCase
import com.sclass.backoffice.lesson.usecase.DeleteLessonScheduleUseCase
import com.sclass.backoffice.lesson.usecase.GetLessonDetailUseCase
import com.sclass.backoffice.lesson.usecase.UpdateLessonScheduleUseCase
import com.sclass.backoffice.lesson.usecase.UpdateSubstituteTeacherUseCase
import com.sclass.backoffice.lessonReport.dto.LessonReportResponse
import com.sclass.backoffice.lessonReport.usecase.GetLessonReportUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lessons")
class LessonController(
    private val updateSubstituteTeacherUseCase: UpdateSubstituteTeacherUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase,
    private val getLessonReportUseCase: GetLessonReportUseCase,
    private val createLessonScheduleUseCase: CreateLessonScheduleUseCase,
    private val updateLessonScheduleUseCase: UpdateLessonScheduleUseCase,
    private val deleteLessonScheduleUseCase: DeleteLessonScheduleUseCase,
) {
    @GetMapping("/{lessonId}")
    fun getDetail(
        @PathVariable lessonId: Long,
    ): ApiResponse<LessonResponse> = ApiResponse.success(getLessonDetailUseCase.execute(lessonId))

    @PatchMapping("/{lessonId}/substitute-teacher")
    fun updateSubstitute(
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: UpdateSubstituteTeacherRequest,
    ): ApiResponse<LessonResponse> = ApiResponse.success(updateSubstituteTeacherUseCase.execute(lessonId, request))

    @GetMapping("/{lessonId}/report")
    fun getReport(
        @PathVariable lessonId: Long,
    ): ApiResponse<LessonReportResponse> = ApiResponse.success(getLessonReportUseCase.execute(lessonId))

    @PostMapping("/{lessonId}/schedule")
    fun createSchedule(
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: ScheduleLessonRequest,
    ): ApiResponse<LessonResponse> =
        ApiResponse.success(
            createLessonScheduleUseCase.execute(
                lessonId,
                request,
            ),
        )

    @PutMapping("/{lessonId}/schedule")
    fun updateSchedule(
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: ScheduleLessonRequest,
    ): ApiResponse<LessonResponse> =
        ApiResponse.success(
            updateLessonScheduleUseCase.execute(
                lessonId,
                request,
            ),
        )

    @DeleteMapping("/{lessonId}/schedule")
    fun deleteSchedule(
        @PathVariable lessonId: Long,
    ): ApiResponse<LessonResponse> =
        ApiResponse.success(
            deleteLessonScheduleUseCase.execute(
                lessonId,
            ),
        )
}
