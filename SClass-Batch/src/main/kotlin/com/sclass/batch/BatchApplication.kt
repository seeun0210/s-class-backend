package com.sclass.batch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.sclass"])
@EntityScan("com.sclass.domain")
@EnableJpaRepositories("com.sclass.domain")
@EnableScheduling
class BatchApplication

fun main(args: Array<String>) {
    runApplication<BatchApplication>(*args)
}
