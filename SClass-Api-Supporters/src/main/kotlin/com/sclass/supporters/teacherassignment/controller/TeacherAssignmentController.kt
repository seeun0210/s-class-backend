package com.sclass.supporters.teacherassignment.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.teacherassignment.dto.AssignedStudentResponse
import com.sclass.supporters.teacherassignment.usecase.GetAssignedStudentsUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/teacher-assignments")
class TeacherAssignmentController(
    private val getAssignedStudentsUseCase: GetAssignedStudentsUseCase,
) {
    @GetMapping("/my-students")
    fun getMyAssignedStudents(
        @CurrentUserId currentUserId: String,
    ): ApiResponse<List<AssignedStudentResponse>> = ApiResponse.success(getAssignedStudentsUseCase.execute(currentUserId))
}
