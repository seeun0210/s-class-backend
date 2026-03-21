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

    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
}

afterEvaluate {
    tasks
        .matching { it.name == "kaptTestKotlin" || it.name == "kaptGenerateStubsTestKotlin" }
        .configureEach { enabled = false }
}
