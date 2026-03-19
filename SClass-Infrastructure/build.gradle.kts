tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    implementation(project(":SClass-Common"))
    implementation("org.springframework.boot:spring-boot-starter")

    // AWS S3
    implementation(platform("software.amazon.awssdk:bom:2.31.15"))
    implementation("software.amazon.awssdk:s3")

    // GCP Cloud Storage
    implementation(platform("com.google.cloud:libraries-bom:26.55.0"))
    implementation("com.google.cloud:google-cloud-storage")

    implementation("org.springframework:spring-web")
}
