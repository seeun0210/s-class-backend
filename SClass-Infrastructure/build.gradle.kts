tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    api(project(":SClass-Common"))
    api("org.springframework.boot:spring-boot-starter")

    // AWS S3 + SSM Parameter Store + CloudWatch
    api(platform("software.amazon.awssdk:bom:2.31.15"))
    api("software.amazon.awssdk:s3")
    api("software.amazon.awssdk:ssm")
    api("software.amazon.awssdk:cloudwatch")

    // Micrometer CloudWatch Registry
    api("io.micrometer:micrometer-registry-cloudwatch2")

    api("org.springframework.boot:spring-boot-starter-quartz")

    // GCP Cloud Storage
    implementation(platform("com.google.cloud:libraries-bom:26.55.0"))
    implementation("com.google.cloud:google-cloud-storage")
    implementation("com.google.api-client:google-api-client")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")

    // Redis (Redisson - 분산 락)
    api("org.redisson:redisson-spring-boot-starter:3.40.2")

    testImplementation("io.mockk:mockk:1.13.16")
}
