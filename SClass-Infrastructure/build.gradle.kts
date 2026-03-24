tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    api(project(":SClass-Common"))
    api("org.springframework.boot:spring-boot-starter")

    // AWS S3 + SSM Parameter Store
    api(platform("software.amazon.awssdk:bom:2.31.15"))
    api("software.amazon.awssdk:s3")
    api("software.amazon.awssdk:ssm")

    // GCP Cloud Storage
    implementation(platform("com.google.cloud:libraries-bom:26.55.0"))
    implementation("com.google.cloud:google-cloud-storage")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    testImplementation("io.mockk:mockk:1.13.16")
}
