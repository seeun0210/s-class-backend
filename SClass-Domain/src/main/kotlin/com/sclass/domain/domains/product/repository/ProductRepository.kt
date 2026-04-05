package com.sclass.domain.domains.product.repository

import com.sclass.domain.domains.product.domain.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository :
    JpaRepository<Product, String>,
    ProductCustomRepository {
    fun findAllByActiveTrue(): List<Product>
}
