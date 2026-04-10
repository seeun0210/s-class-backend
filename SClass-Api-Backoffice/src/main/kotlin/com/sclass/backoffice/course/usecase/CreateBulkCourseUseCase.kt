package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.backoffice.course.dto.CreateBulkCourseRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateBulkCourseUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(request: CreateBulkCourseRequest): List<CourseResponse> {
        productAdaptor.findById(request.productId)
        val courses =
            request.teacherUserIds.map { teacherUserId ->
                Course(
                    productId = request.productId,
                    teacherUserId = teacherUserId,
                    organizationId = request.organizationId,
                    name = request.name,
                    description = request.description,
                )
            }
        return courseAdaptor.saveAll(courses).map { CourseResponse.from(it) }
    }
}
