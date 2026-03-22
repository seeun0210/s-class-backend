package com.sclass.common.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class OrganizationId(
    val required: Boolean = true,
)
