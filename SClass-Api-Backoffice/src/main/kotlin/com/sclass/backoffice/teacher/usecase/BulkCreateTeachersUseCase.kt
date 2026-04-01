package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.BulkCreateTeacherResult
import com.sclass.backoffice.teacher.dto.BulkCreateTeachersRequest
import com.sclass.backoffice.teacher.dto.BulkCreateTeachersResponse
import com.sclass.common.annotation.UseCase

@UseCase
class BulkCreateTeachersUseCase(
    private val createTeacherUseCase: CreateTeacherUseCase,
) {
    fun execute(request: BulkCreateTeachersRequest): BulkCreateTeachersResponse {
        val duplicateEmails =
            request.teachers
                .groupingBy { it.email.lowercase() }
                .eachCount()
                .filter { it.value > 1 }
                .keys

        val results =
            request.teachers.mapIndexed { index, teacherRequest ->
                val row = index + 1
                if (teacherRequest.email.lowercase() in duplicateEmails) {
                    BulkCreateTeacherResult(
                        row = row,
                        email = teacherRequest.email,
                        success = false,
                        error = "요청 내 이메일이 중복됩니다",
                    )
                } else {
                    try {
                        val response = createTeacherUseCase.execute(teacherRequest)
                        BulkCreateTeacherResult(
                            row = row,
                            email = teacherRequest.email,
                            success = true,
                            data = response,
                        )
                    } catch (e: Exception) {
                        BulkCreateTeacherResult(
                            row = row,
                            email = teacherRequest.email,
                            success = false,
                            error = e.message ?: "알 수 없는 오류",
                        )
                    }
                }
            }

        val successCount = results.count { it.success }
        return BulkCreateTeachersResponse(
            totalCount = results.size,
            successCount = successCount,
            failureCount = results.size - successCount,
            results = results,
        )
    }
}
