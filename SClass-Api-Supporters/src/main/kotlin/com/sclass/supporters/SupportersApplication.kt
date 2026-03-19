package com.sclass.supporters

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.sclass"])
@EntityScan("com.sclass.domain")
@EnableJpaRepositories("com.sclass.domain")
class SupportersApplication

fun main(args: Array<String>) {
    runApplication<SupportersApplication>(*args)
}
