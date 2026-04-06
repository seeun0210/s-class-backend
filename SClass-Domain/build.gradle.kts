plugins {
    id("org.hibernate.orm") version "7.2.4.Final"
}

hibernate {
    enhancement {
        enableLazyInitialization.set(true)
        classNames.add("com.sclass.domain.domains.diagnosis.domain.Diagnosis")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    // Common
    api(project(":SClass-Common"))

    // JPA & MySQL
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")

    // ULID
    implementation("com.github.f4b6a3:ulid-creator:5.2.3")

    implementation("org.springframework.security:spring-security-crypto")

    // Web (compile-only: 런타임은 Api 모듈이 제공)
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")

    // Testcontainers (MySQL)
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:mysql:1.20.4")
}

afterEvaluate {
    tasks
        .matching { it.name == "kaptTestKotlin" || it.name == "kaptGenerateStubsTestKotlin" }
        .configureEach { enabled = false }
}
