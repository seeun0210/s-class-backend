package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.backoffice.course.dto.CreateCourseRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCourseUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(request: CreateCourseRequest): CourseResponse {
        productAdaptor.findById(request.productId)
        val course =
            courseAdaptor.save(
                Course(
                    productId = request.productId,
                    teacherUserId = request.teacherUserId,
                    organizationId = request.organizationId,
                    name = request.name,
                    description = request.description,
                ),
            )
        return CourseResponse.from(course)
    }
}
