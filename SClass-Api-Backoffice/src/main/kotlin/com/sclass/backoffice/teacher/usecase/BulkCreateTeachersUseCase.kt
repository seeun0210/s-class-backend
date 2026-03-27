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
            request.teachers
                .chunked(BATCH_SIZE)
                .flatMapIndexed { chunkIndex, chunk ->
                    chunk.mapIndexed { indexInChunk, teacherRequest ->
                        val row = chunkIndex * BATCH_SIZE + indexInChunk + 1
                        processOne(row, teacherRequest, duplicateEmails)
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

    private fun processOne(
        row: Int,
        request: com.sclass.backoffice.teacher.dto.CreateTeacherRequest,
        duplicateEmails: Set<String>,
    ): BulkCreateTeacherResult {
        if (request.email.lowercase() in duplicateEmails) {
            return BulkCreateTeacherResult(
                row = row,
                email = request.email,
                success = false,
                error = "요청 내 이메일이 중복됩니다",
            )
        }

        return try {
            val response = createTeacherUseCase.execute(request)
            BulkCreateTeacherResult(
                row = row,
                email = request.email,
                success = true,
                data = response,
            )
        } catch (e: Exception) {
            BulkCreateTeacherResult(
                row = row,
                email = request.email,
                success = false,
                error = e.message ?: "알 수 없는 오류",
            )
        }
    }

    companion object {
        private const val BATCH_SIZE = 200
    }
}
