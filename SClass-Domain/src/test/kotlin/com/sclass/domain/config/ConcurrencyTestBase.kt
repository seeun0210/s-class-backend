package com.sclass.domain.config

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * MySQL Testcontainers 기반 동시성 테스트 베이스.
 * H2 대신 실제 MySQL을 사용하여 잠금/격리 수준 동작을 정확히 검증한다.
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MysqlTestContainerConfig::class, DomainTestConfig::class)
@ComponentScan(basePackages = ["com.sclass.domain"])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConcurrencyTest
