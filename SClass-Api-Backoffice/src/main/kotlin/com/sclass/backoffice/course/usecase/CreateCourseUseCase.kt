package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.backoffice.course.dto.CreateCourseRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCourseUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(request: CreateCourseRequest): CourseResponse {
        val product =
            productAdaptor.save(
                CourseProduct(
                    name = request.name,
                    priceWon = request.priceWon,
                    totalLessons = request.totalLessons,
                    description = request.description,
                    curriculum = request.curriculum,
                    thumbnailFileId = request.thumbnailFileId,
                ),
            ) as CourseProduct
        val course =
            courseAdaptor.save(
                Course(
                    productId = product.id,
                    teacherUserId = request.teacherUserId,
                    organizationId = request.organizationId,
                    maxEnrollments = request.maxEnrollments,
                    enrollmentStartAt = request.enrollmentStartAt,
                    enrollmentDeadLine = request.enrollmentDeadLine,
                    startAt = request.startAt,
                    endAt = request.endAt,
                ),
            )
        return CourseResponse.from(course, product)
    }
}
