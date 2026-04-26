package com.sclass.supporters.lesson.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import com.sclass.supporters.lesson.dto.CompleteLessonRequest
import com.sclass.supporters.lesson.dto.CreateLessonInquiryPlanRequest
import com.sclass.supporters.lesson.dto.LessonDetailResponse
import com.sclass.supporters.lesson.dto.LessonReportResponse
import com.sclass.supporters.lesson.dto.LessonResponse
import com.sclass.supporters.lesson.dto.RecordLessonRequest
import com.sclass.supporters.lesson.dto.ScheduleLessonRequest
import com.sclass.supporters.lesson.dto.StartLessonRequest
import com.sclass.supporters.lesson.dto.SubmitLessonReportRequest
import com.sclass.supporters.lesson.dto.UpdateLessonReportRequest
import com.sclass.supporters.lesson.dto.UpdateLessonRequest
import com.sclass.supporters.lesson.usecase.CompleteLessonUseCase
import com.sclass.supporters.lesson.usecase.CreateLessonInquiryPlanUseCase
import com.sclass.supporters.lesson.usecase.CreateLessonScheduleUseCase
import com.sclass.supporters.lesson.usecase.GetLessonDetailUseCase
import com.sclass.supporters.lesson.usecase.GetLessonReportUseCase
import com.sclass.supporters.lesson.usecase.GetMySubstituteLessonsUseCase
import com.sclass.supporters.lesson.usecase.RecordLessonUseCase
import com.sclass.supporters.lesson.usecase.StartLessonUseCase
import com.sclass.supporters.lesson.usecase.SubmitLessonReportUseCase
import com.sclass.supporters.lesson.usecase.UpdateLessonReportUseCase
import com.sclass.supporters.lesson.usecase.UpdateLessonScheduleUseCase
import com.sclass.supporters.lesson.usecase.UpdateLessonUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lessons")
class LessonController(
    private val createLessonInquiryPlanUseCase: CreateLessonInquiryPlanUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase,
    private val updateLessonUseCase: UpdateLessonUseCase,
    private val submitLessonReportUseCase: SubmitLessonReportUseCase,
    private val getLessonReportUseCase: GetLessonReportUseCase,
    private val updateLessonReportUseCase: UpdateLessonReportUseCase,
    private val getMySubstituteLessonsUseCase: GetMySubstituteLessonsUseCase,
    private val startLessonUseCase: StartLessonUseCase,
    private val completeLessonUseCase: CompleteLessonUseCase,
    private val recordLessonUseCase: RecordLessonUseCase,
    private val createLessonScheduleUseCase: CreateLessonScheduleUseCase,
    private val updateLessonScheduleUseCase: UpdateLessonScheduleUseCase,
) {
    @GetMapping("/my/substitutes")
    fun mySubstituteLessons(
        @CurrentUserId userId: String,
        @RequestParam(required = false) status: LessonStatus?,
    ): ApiResponse<List<LessonResponse>> = ApiResponse.success(getMySubstituteLessonsUseCase.execute(userId, status))

    @GetMapping("/{lessonId}")
    fun detail(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
    ): ApiResponse<LessonDetailResponse> = ApiResponse.success(getLessonDetailUseCase.execute(userId, lessonId))

    @Deprecated("Use POST/PUT /api/v1/lessons/{lessonId}/schedule")
    @PatchMapping("/{lessonId}")
    fun update(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: UpdateLessonRequest,
    ): ApiResponse<LessonResponse> = ApiResponse.success(updateLessonUseCase.execute(userId, lessonId, request))

    @PostMapping("/{lessonId}/inquiry-plans")
    fun createInquiryPlan(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: CreateLessonInquiryPlanRequest,
    ): ApiResponse<InquiryPlanResponse> = ApiResponse.success(createLessonInquiryPlanUseCase.execute(userId, lessonId, request))

    @PostMapping("/{lessonId}/report")
    fun submitReport(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: SubmitLessonReportRequest,
    ): ApiResponse<LessonReportResponse> = ApiResponse.success(submitLessonReportUseCase.execute(userId, lessonId, request))

    @GetMapping("/{lessonId}/report")
    fun getReport(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
    ): ApiResponse<LessonReportResponse> = ApiResponse.success(getLessonReportUseCase.execute(userId, lessonId))

    @PutMapping("/{lessonId}/report")
    fun updateReport(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: UpdateLessonReportRequest,
    ): ApiResponse<LessonReportResponse> = ApiResponse.success(updateLessonReportUseCase.execute(userId, lessonId, request))

    @PostMapping("/{lessonId}/start")
    fun start(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody(required = false) request: StartLessonRequest?,
    ): ApiResponse<LessonResponse> = ApiResponse.success(startLessonUseCase.execute(userId, lessonId, request ?: StartLessonRequest()))

    @PostMapping("/{lessonId}/complete")
    fun complete(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody(required = false) request: CompleteLessonRequest?,
    ): ApiResponse<LessonResponse> =
        ApiResponse.success(completeLessonUseCase.execute(userId, lessonId, request ?: CompleteLessonRequest()))

    @PutMapping("/{lessonId}/record")
    fun record(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: RecordLessonRequest,
    ): ApiResponse<LessonResponse> = ApiResponse.success(recordLessonUseCase.execute(userId, lessonId, request))

    @PostMapping("/{lessonId}/schedule")
    fun createSchedule(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: ScheduleLessonRequest,
    ): ApiResponse<LessonResponse> =
        ApiResponse.success(
            createLessonScheduleUseCase.execute(
                userId,
                lessonId,
                request,
            ),
        )

    @PutMapping("/{lessonId}/schedule")
    fun updateSchedule(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: ScheduleLessonRequest,
    ): ApiResponse<LessonResponse> =
        ApiResponse.success(
            updateLessonScheduleUseCase.execute(
                userId,
                lessonId,
                request,
            ),
        )
}
