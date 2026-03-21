package com.sclass.domain

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.sclass.domain", "com.sclass.common"])
@EntityScan("com.sclass.domain")
@EnableJpaRepositories("com.sclass.domain")
class DomainTestApplication
