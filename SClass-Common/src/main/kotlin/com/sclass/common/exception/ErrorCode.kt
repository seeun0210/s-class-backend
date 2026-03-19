package com.sclass.common.exception

interface ErrorCode {
    val code: String
    val message: String
    val httpStatus: Int
}
