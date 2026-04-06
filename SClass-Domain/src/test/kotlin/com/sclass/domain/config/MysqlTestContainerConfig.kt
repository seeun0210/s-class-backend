package com.sclass.domain.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MySQLContainer

@TestConfiguration(proxyBeanMethods = false)
class MysqlTestContainerConfig {
    @Bean
    @ServiceConnection
    fun mysql(): MySQLContainer<*> =
        MySQLContainer("mysql:8.0")
            .withDatabaseName("sclass_test")
            .withUsername("test")
            .withPassword("test")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci")
}
