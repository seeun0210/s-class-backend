package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.BulkCreateStudentResult
import com.sclass.backoffice.student.dto.BulkCreateStudentsRequest
import com.sclass.backoffice.student.dto.BulkCreateStudentsResponse
import com.sclass.common.annotation.UseCase

@UseCase
class BulkCreateStudentsUseCase(
    private val createStudentUseCase: CreateStudentUseCase,
) {
    fun execute(request: BulkCreateStudentsRequest): BulkCreateStudentsResponse {
        val duplicateEmails =
            request.students
                .groupingBy { it.email.lowercase() }
                .eachCount()
                .filter { it.value > 1 }
                .keys

        val results =
            request.students.mapIndexed { index, studentRequest ->
                val row = index + 1
                if (studentRequest.email.lowercase() in duplicateEmails) {
                    BulkCreateStudentResult(
                        row = row,
                        email = studentRequest.email,
                        success = false,
                        error = "요청 내 이메일이 중복됩니다",
                    )
                } else {
                    try {
                        val response = createStudentUseCase.execute(studentRequest)
                        BulkCreateStudentResult(
                            row = row,
                            email = studentRequest.email,
                            success = true,
                            data = response,
                        )
                    } catch (e: Exception) {
                        BulkCreateStudentResult(
                            row = row,
                            email = studentRequest.email,
                            success = false,
                            error = e.message ?: "알 수 없는 오류",
                        )
                    }
                }
            }

        val successCount = results.count { it.success }
        return BulkCreateStudentsResponse(
            totalCount = results.size,
            successCount = successCount,
            failureCount = results.size - successCount,
            results = results,
        )
    }
}
