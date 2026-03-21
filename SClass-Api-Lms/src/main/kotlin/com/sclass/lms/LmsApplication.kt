package com.sclass.lms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.sclass"])
@EntityScan("com.sclass.domain")
@EnableJpaRepositories("com.sclass.domain")
class LmsApplication

fun main(args: Array<String>) {
    runApplication<LmsApplication>(*args)
}
