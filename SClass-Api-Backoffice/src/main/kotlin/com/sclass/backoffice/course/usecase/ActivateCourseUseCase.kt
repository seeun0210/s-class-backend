package com.sclass.backoffice.course.usecase

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.exception.ProductTypeMismatchException
import org.springframework.transaction.annotation.Transactional

@UseCase
class ActivateCourseUseCase(
    private val courseAdaptor: CourseAdaptor,
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(courseId: Long): CourseResponse {
        val course = courseAdaptor.findById(courseId)
        course.activate()
        val saved = courseAdaptor.save(course)
        val product =
            productAdaptor.findById(saved.productId) as? CourseProduct
                ?: throw ProductTypeMismatchException()
        return CourseResponse.from(saved, product)
    }
}
